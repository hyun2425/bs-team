package bs_team.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class OcrService {
    
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${openai.api.key:}")
    private String apiKey;
    
    @Value("${openai.model:gpt-4o}")
    private String model;
    
    public OcrService(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://api.openai.com/v1")
                .build();
    }
    
    /**
     * OpenAI Responses API를 사용하여 이미지에서 한글 이름만 추출
     */
    public List<String> extractKoreanNames(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("image 파일이 비어있습니다.");
        }
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("OpenAI API 키가 설정되지 않았습니다. application.properties에 openai.api.key를 설정해주세요.");
        }
        
        String contentType = Optional.ofNullable(file.getContentType()).orElse("image/png");
        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다. contentType=" + contentType);
        }
        
        String dataUrl = toDataUrl(file.getBytes(), contentType);
        
        // Chat Completions API payload 구성
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        
        List<Map<String, Object>> content = new ArrayList<>();
        
        // 텍스트 프롬프트
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text", """
                너는 이미지에서 사람 이름만 추출하는 엔진이다.
                규칙:
                - 한글 이름만 추출 (예: '현상주', '김민성')
                - 숫자/영문/시간/상단 상태바/기타 UI 문구 제외
                - 중복 제거
                - 결과는 JSON 객체로 반환하되, "names" 필드에 이름 배열을 넣어주세요.
                """.strip());
        content.add(textPart);
        
        // 이미지
        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("type", "image_url");
        Map<String, String> imageUrl = new HashMap<>();
        imageUrl.put("url", dataUrl);
        imagePart.put("image_url", imageUrl);
        content.add(imagePart);
        
        userMessage.put("content", content);
        messages.add(userMessage);
        payload.put("messages", messages);
        
        // Structured Outputs (JSON Schema - object 타입만 허용)
        Map<String, Object> responseFormat = new HashMap<>();
        responseFormat.put("type", "json_schema");
        
        Map<String, Object> jsonSchema = new HashMap<>();
        jsonSchema.put("name", "korean_names_only");
        
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> namesProperty = new HashMap<>();
        namesProperty.put("type", "array");
        Map<String, Object> items = new HashMap<>();
        items.put("type", "string");
        namesProperty.put("items", items);
        namesProperty.put("description", "한글 이름 배열");
        properties.put("names", namesProperty);
        
        schema.put("properties", properties);
        schema.put("required", Arrays.asList("names"));
        schema.put("additionalProperties", false);
        
        jsonSchema.put("schema", schema);
        responseFormat.put("json_schema", jsonSchema);
        payload.put("response_format", responseFormat);
        
        try {
            String raw = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
            
            if (raw == null || raw.isBlank()) {
                throw new IllegalStateException("OpenAI 응답이 비어있습니다.");
            }
            
            // 응답에서 structured output 추출
            String outputText = extractStructuredOutput(raw);
            
            // outputText는 JSON 객체로 오고, "names" 필드에 배열이 있음
            List<String> names = parseNamesFromObject(outputText);
            
            // 마지막 방어: 한글이름만 필터 + 중복제거
            return names.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> s.matches("^[가-힣]{2,4}$")) // 보통 한국 이름 2~4글자
                    .distinct()
                    .toList();
                    
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // HTTP 오류 응답 파싱
            String responseBody = e.getResponseBodyAsString();
            System.err.println("=== OpenAI API 오류 응답 ===");
            System.err.println("HTTP Status: " + e.getStatusCode());
            System.err.println("Response Body: " + responseBody);
            System.err.println("===========================");
            
            String errorMessage = parseOpenAIError(responseBody);
            
            // 할당량 초과 오류 확인
            if (e.getStatusCode().value() == 429) {
                if (errorMessage.contains("quota") || errorMessage.contains("insufficient_quota") || 
                    responseBody.contains("insufficient_quota")) {
                    throw new IOException("OpenAI API 할당량이 초과되었습니다. 계정의 결제 정보와 사용량을 확인해주세요. (https://platform.openai.com/usage)", e);
                } else {
                    throw new IOException("OpenAI API 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요. (Rate Limit)", e);
                }
            }
            
            throw new IOException("OpenAI API 오류: " + errorMessage, e);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && (message.contains("quota") || message.contains("insufficient_quota"))) {
                throw new IOException("OpenAI API 할당량이 초과되었습니다. 계정의 결제 정보와 사용량을 확인해주세요.", e);
            }
            throw new IOException("OpenAI API 호출 중 오류가 발생했습니다: " + message, e);
        }
    }
    
    /**
     * 이미지에서 텍스트 추출 (기존 메서드 호환성 유지)
     */
    public String extractTextFromImage(MultipartFile file) throws IOException {
        List<String> names = extractKoreanNames(file);
        return String.join("\n", names);
    }
    
    private String toDataUrl(byte[] bytes, String contentType) {
        String b64 = Base64.getEncoder().encodeToString(bytes);
        return "data:" + contentType + ";base64," + b64;
    }
    
    private String extractStructuredOutput(String rawJson) throws Exception {
        JsonNode root = objectMapper.readTree(rawJson);
        
        // Chat Completions API 응답 구조: choices[0].message.refusal 또는 choices[0].message.content
        JsonNode choices = root.get("choices");
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode message = choices.get(0).get("message");
            if (message != null) {
                // Structured output은 보통 refusal 필드에 있거나 content에 JSON 문자열로 있음
                JsonNode refusal = message.get("refusal");
                if (refusal != null && refusal.isTextual()) {
                    return refusal.asText();
                }
                
                JsonNode content = message.get("content");
                if (content != null && content.isTextual()) {
                    String contentText = content.asText();
                    // JSON 객체인지 확인
                    if (contentText.trim().startsWith("{")) {
                        return contentText;
                    }
                }
            }
        }
        
        throw new IllegalStateException("structured output을 찾지 못했습니다. raw=" + rawJson);
    }
    
    private List<String> parseNamesFromObject(String jsonObjectText) throws Exception {
        JsonNode node = objectMapper.readTree(jsonObjectText);
        if (!node.isObject()) {
            throw new IllegalStateException("JSON 객체가 아닙니다: " + jsonObjectText);
        }
        
        JsonNode namesNode = node.get("names");
        if (namesNode == null || !namesNode.isArray()) {
            throw new IllegalStateException("'names' 배열을 찾을 수 없습니다: " + jsonObjectText);
        }
        
        List<String> out = new ArrayList<>();
        for (JsonNode n : namesNode) {
            if (n.isTextual()) out.add(n.asText());
        }
        return out;
    }
    
    private String parseOpenAIError(String errorResponse) {
        if (errorResponse == null || errorResponse.isBlank()) {
            return "알 수 없는 오류";
        }
        
        try {
            JsonNode root = objectMapper.readTree(errorResponse);
            JsonNode error = root.get("error");
            if (error != null) {
                JsonNode message = error.get("message");
                JsonNode code = error.get("code");
                JsonNode type = error.get("type");
                
                StringBuilder sb = new StringBuilder();
                if (message != null && message.isTextual()) {
                    sb.append(message.asText());
                }
                if (code != null && code.isTextual()) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append("[코드: ").append(code.asText()).append("]");
                }
                if (type != null && type.isTextual()) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append("[타입: ").append(type.asText()).append("]");
                }
                
                if (sb.length() > 0) {
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            System.err.println("에러 응답 파싱 실패: " + e.getMessage());
            // JSON 파싱 실패 시 원본 반환
        }
        
        return errorResponse;
    }
}
