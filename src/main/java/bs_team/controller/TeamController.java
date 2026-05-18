package bs_team.controller;

import bs_team.model.Player;
import bs_team.model.Team;
import bs_team.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Controller
public class TeamController {

    private List<Player> players = new ArrayList<>();
    private List<Team> teams = new ArrayList<>();
    private List<String> selectedPlayers = new ArrayList<>();

    @Autowired
    private OcrService ocrService;

    public TeamController() {
        // 하드코딩된 선수 데이터 초기화
        initializePlayers();
    }

    private void initializePlayers() {
        players.add(new Player("김민곤", 180, "센터", 9, 7, false));
        players.add(new Player("김민성", 180, "가드", 8, 6, false));
        players.add(new Player("김승준", 180, "포워드", 10, 9, false));
        players.add(new Player("김현석", 180, "센터", 7, 6, false));
        players.add(new Player("김현철", 180, "가드", 5, 3, false));
        players.add(new Player("김종석", 180, "가드", 5, 3, false));
        players.add(new Player("박기선", 170, "가드", 7, 7, false));
        players.add(new Player("박준규", 180, "센터", 7, 7, false));
        players.add(new Player("신동주", 175, "가드", 6, 6, false));
        players.add(new Player("신재섭", 180, "가드", 9, 7, false));
        players.add(new Player("심승현", 180, "포워드", 10, 9, false));
        players.add(new Player("심우석", 180, "포워드", 7, 8, false));
        players.add(new Player("왕진운", 170, "가드", 5, 3, false));
        players.add(new Player("윤민상", 180, "가드", 6, 10, true));
        players.add(new Player("윤수현", 175, "가드", 4, 9, false));
        players.add(new Player("이민규", 180, "가드", 8, 7, true));
        players.add(new Player("이조영", 180, "센터", 7, 7, false));
        players.add(new Player("장하림", 180, "포워드", 8, 10, false));
        players.add(new Player("전형욱", 180, "센터", 8, 8, false));
        players.add(new Player("정영호", 180, "포워드", 7, 6, false));
        players.add(new Player("조태희", 170, "가드", 4, 8, true));
        players.add(new Player("최상혁", 180, "포워드", 4, 6, false));
        players.add(new Player("최유준", 180, "포워드", 8, 6, false));
        players.add(new Player("한동엽", 180, "센터", 4, 10, false));
        players.add(new Player("현상주", 175, "포워드", 9, 7, true));

        players.add(new Player("게스트-1", 180, "가드", 7, 7, false));
        players.add(new Player("게스트-2", 180, "가드", 7, 7, false));
        players.add(new Player("게스트-3", 180, "가드", 7, 7, false));
        players.add(new Player("게스트-4", 180, "가드", 7, 7, false));
        players.add(new Player("게스트-5", 180, "가드", 7, 7, false));
        players.add(new Player("게스트-6", 180, "가드", 7, 7, false));
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("players", players);
        model.addAttribute("teams", teams);
        model.addAttribute("selectedPlayers", selectedPlayers);
        return "index";
    }

    @GetMapping("/create-teams")
    public String createTeamsGet(Model model) {
        return "redirect:/";
    }

    @PostMapping("/create-teams")
    public String createTeams(@RequestParam(value = "selectedPlayers", required = false) List<String> selectedPlayers,
                              RedirectAttributes redirectAttributes) {
        teams.clear();

        // 선택된 선수가 없거나 2명 미만인 경우
        if (selectedPlayers == null || selectedPlayers.size() < 8) {
            this.selectedPlayers = selectedPlayers != null ? new ArrayList<>(selectedPlayers) : new ArrayList<>();
            redirectAttributes.addFlashAttribute("error", "최소 8명의 선수를 선택해주세요.");
            return "redirect:/";
        }

        // 선택된 선수들을 저장
        this.selectedPlayers = new ArrayList<>(selectedPlayers);
        System.out.println("선택된 선수들: " + this.selectedPlayers);

        // 선택된 선수들만 필터링
        List<Player> selectedPlayerObjects = players.stream()
                .filter(p -> selectedPlayers.contains(p.getName()))
                .collect(Collectors.toList());

        System.out.println("총 선택된 선수 수: " + selectedPlayerObjects.size());

        // 2팀으로 고정
        teams.add(new Team("흰색팀", new ArrayList<>()));
        teams.add(new Team("검은색팀", new ArrayList<>()));

        // 전체 선수 수 계산
        int totalPlayers = selectedPlayerObjects.size();
        int targetPerTeam = totalPlayers / 2;
        int remainingPlayers = totalPlayers % 2; // 홀수일 경우 한 명 더

        System.out.println("총 선수 수: " + totalPlayers + ", 팀당 목표: " + targetPerTeam + ", 남은 선수: " + remainingPlayers);

        // 포지션별로 선수들을 분류하고 총 능력치 순으로 정렬
        List<Player> centers = selectedPlayerObjects.stream()
                .filter(p -> "센터".equals(p.getPosition()))
                .sorted((p1, p2) -> Integer.compare(p2.getTotalPower(), p1.getTotalPower()))
                .collect(Collectors.toList());
        List<Player> forwards = selectedPlayerObjects.stream()
                .filter(p -> "포워드".equals(p.getPosition()))
                .sorted((p1, p2) -> Integer.compare(p2.getTotalPower(), p1.getTotalPower()))
                .collect(Collectors.toList());
        List<Player> guards = selectedPlayerObjects.stream()
                .filter(p -> "가드".equals(p.getPosition()))
                .sorted((p1, p2) -> Integer.compare(p2.getTotalPower(), p1.getTotalPower()))
                .collect(Collectors.toList());

        System.out.println("센터: " + centers.size() + "명, 포워드: " + forwards.size() + "명, 가드: " + guards.size() + "명");

        // 모든 포지션의 선수들을 하나의 리스트로 합치고 총 능력치 순으로 정렬
        List<Player> allPlayers = new ArrayList<>();
        allPlayers.addAll(centers);
        allPlayers.addAll(forwards);
        allPlayers.addAll(guards);
        allPlayers.sort((p1, p2) -> Integer.compare(p2.getTotalPower(), p1.getTotalPower()));

        // 번갈아가며 배정하여 균형잡힌 팀 구성
        for (int i = 0; i < allPlayers.size(); i++) {
            Player player = allPlayers.get(i);
            int selectedTeam = i % 2; // 번갈아가며 배정
            teams.get(selectedTeam).getPlayers().add(player);
            System.out.println("선수 " + player.getName() + "(" + player.getPosition() + ") -> 팀 " + selectedTeam +
                             " (팀0: " + teams.get(0).getPlayers().size() + "명, 공:" + teams.get(0).getTotalAttackPower() +
                             " 수:" + teams.get(0).getTotalDefensePower() +
                             " | 팀1: " + teams.get(1).getPlayers().size() + "명, 공:" + teams.get(1).getTotalAttackPower() +
                             " 수:" + teams.get(1).getTotalDefensePower() + ")");
        }

        // 각 팀의 선수들을 포지션 순서대로 정렬 (센터-포워드-가드)
        teams.forEach(team -> {
            team.getPlayers().sort((p1, p2) -> {
                int pos1 = getPositionOrder(p1.getPosition());
                int pos2 = getPositionOrder(p2.getPosition());
                return Integer.compare(pos1, pos2);
            });
        });

        System.out.println("최종 팀 분배 - 팀0: " + teams.get(0).getPlayers().size() + "명, 공:" + teams.get(0).getTotalAttackPower() +
                          " 수:" + teams.get(0).getTotalDefensePower() +
                          " | 팀1: " + teams.get(1).getPlayers().size() + "명, 공:" + teams.get(1).getTotalAttackPower() +
                          " 수:" + teams.get(1).getTotalDefensePower());

        System.out.println("모델에 전달할 선택된 선수들: " + this.selectedPlayers);
        System.out.println("모델에 전달할 선택된 선수들 크기: " + this.selectedPlayers.size());

        return "redirect:/";
    }

    @PostMapping("/move-player")
    public String movePlayer(@RequestParam String playerName,
                           @RequestParam int teamIndex,
                           @RequestParam(value = "selectedPlayers", required = false) List<String> selectedPlayers) {
        // 드래그 앤 드롭으로 선수를 다른 팀으로 이동
        Player selectedPlayer = players.stream()
                .filter(p -> p.getName().equals(playerName))
                .findFirst()
                .orElse(null);

        if (selectedPlayer != null && teamIndex < teams.size()) {
            // 기존 팀에서 제거
            teams.forEach(team -> team.getPlayers().remove(selectedPlayer));
            // 새 팀에 추가
            teams.get(teamIndex).getPlayers().add(selectedPlayer);
        }

        // 선택된 선수들 업데이트
        if (selectedPlayers != null) {
            this.selectedPlayers = new ArrayList<>(selectedPlayers);
        }

        return "redirect:/";
    }

    @PostMapping("/reset")
    public String reset() {
        // 모든 팀과 선택된 선수 초기화
        teams.clear();
        selectedPlayers.clear();

        return "redirect:/";
    }

    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestPart("image") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            String extractedText = ocrService.extractTextFromImage(file);
            List<String> extractedNames = ocrService.extractKoreanNames(file);

            List<String> matchedPlayers = new ArrayList<>();
            for (String extractedName : extractedNames) {
                for (Player player : players) {
                    String playerName = player.getName();
                    if (playerName.equals(extractedName) ||
                        playerName.contains(extractedName) ||
                        extractedName.contains(playerName)) {
                        if (!matchedPlayers.contains(playerName)) {
                            matchedPlayers.add(playerName);
                        }
                    }
                }
            }

            response.put("success", true);
            response.put("extractedText", extractedText);
            response.put("extractedNames", extractedNames);
            response.put("matchedPlayers", matchedPlayers);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "이미지 처리 중 오류가 발생했습니다.";
            }

            if (errorMessage.contains("API 키") || errorMessage.contains("설정되지 않았습니다")) {
                errorMessage += " OPENAI_API_KEY 환경변수를 설정해주세요.";
            } else if (errorMessage.contains("429")) {
                errorMessage = "OpenAI API 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.";
            }

            response.put("error", errorMessage);
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }
    // 포지션 순서를 반환하는 헬퍼 메서드
    private int getPositionOrder(String position) {
        return switch (position) {
            case "센터" -> 1;
            case "포워드" -> 2;
            case "가드" -> 3;
            default -> 4;
        };
    }

    // 정확한 반반 분배를 보장하는 팀 선택 메서드 (반반 분배가 최우선)
    private int getExactHalfDistribution(Player player, List<Team> teams, int targetPerTeam, int remainingPlayers) {
        // 각 팀의 현재 선수 수
        int team0Size = teams.get(0).getPlayers().size();
        int team1Size = teams.get(1).getPlayers().size();

        // 반반 분배가 최우선 - 목표 인원에 맞춰 배정
        if (team0Size < targetPerTeam) {
            return 0; // 첫 번째 팀이 목표 인원보다 적으면 첫 번째 팀에 배정
        } else if (team1Size < targetPerTeam) {
            return 1; // 두 번째 팀이 목표 인원보다 적으면 두 번째 팀에 배정
        } else if (remainingPlayers > 0 && team0Size == targetPerTeam && team1Size == targetPerTeam) {
            // 홀수일 경우 한 팀에 한 명 더 배정 (밸런스 고려)
            int team0Total = teams.get(0).getTotalPower();
            int team1Total = teams.get(1).getTotalPower();
            return (team0Total <= team1Total) ? 0 : 1;
        }

        // 이미 반반 분배가 완료된 경우 밸런스 고려
        int team0Total = teams.get(0).getTotalPower();
        int team1Total = teams.get(1).getTotalPower();
        return (team0Total <= team1Total) ? 0 : 1;
    }

    // 포지션별 균등 분배와 공수 밸런스를 고려한 팀 선택 메서드
    private int getBalancedTeamWithPositionAndStats(Player player, List<Team> teams, int targetPerTeam, int remainingPlayers, int currentIndex) {
        // 각 팀의 현재 선수 수
        int team0Size = teams.get(0).getPlayers().size();
        int team1Size = teams.get(1).getPlayers().size();

        // 각 팀의 현재 공격력과 수비력 계산
        int team0Attack = teams.get(0).getTotalAttackPower();
        int team0Defense = teams.get(0).getTotalDefensePower();
        int team1Attack = teams.get(1).getTotalAttackPower();
        int team1Defense = teams.get(1).getTotalDefensePower();

        // 현재 선수의 능력치
        int playerAttack = player.getAttackPower();
        int playerDefense = player.getDefensePower();

        // 각 팀에 배정했을 때의 공격력과 수비력
        int team0AttackWithPlayer = team0Attack + playerAttack;
        int team0DefenseWithPlayer = team0Defense + playerDefense;
        int team1AttackWithPlayer = team1Attack + playerAttack;
        int team1DefenseWithPlayer = team1Defense + playerDefense;

        // 각 팀에 배정했을 때의 총 능력치
        int team0TotalWithPlayer = team0AttackWithPlayer + team0DefenseWithPlayer;
        int team1TotalWithPlayer = team1AttackWithPlayer + team1DefenseWithPlayer;

        // 반반 분배가 최우선 - 목표 인원에 맞춰 배정
        if (team0Size < targetPerTeam) {
            return 0; // 첫 번째 팀이 목표 인원보다 적으면 첫 번째 팀에 배정
        } else if (team1Size < targetPerTeam) {
            return 1; // 두 번째 팀이 목표 인원보다 적으면 두 번째 팀에 배정
        } else if (remainingPlayers > 0 && team0Size == targetPerTeam && team1Size == targetPerTeam) {
            // 홀수일 경우 한 팀에 한 명 더 배정 (밸런스 고려)
            return getBetterBalancedTeam(team0AttackWithPlayer, team0DefenseWithPlayer,
                                       team1AttackWithPlayer, team1DefenseWithPlayer);
        }

        // 밸런스 점수 계산 (공격력 차이 + 수비력 차이 + 총 능력치 차이)
        int attackDiff0 = Math.abs(team0AttackWithPlayer - team1AttackWithPlayer);
        int defenseDiff0 = Math.abs(team0DefenseWithPlayer - team1DefenseWithPlayer);
        int totalDiff0 = Math.abs(team0TotalWithPlayer - team1TotalWithPlayer);

        int attackDiff1 = Math.abs(team1AttackWithPlayer - team0AttackWithPlayer);
        int defenseDiff1 = Math.abs(team1DefenseWithPlayer - team0DefenseWithPlayer);
        int totalDiff1 = Math.abs(team1TotalWithPlayer - team0TotalWithPlayer);

        // 팀0에 배정했을 때의 밸런스 점수 (낮을수록 좋음)
        int balance0 = attackDiff0 + defenseDiff0 + totalDiff0;
        // 팀1에 배정했을 때의 밸런스 점수 (낮을수록 좋음)
        int balance1 = attackDiff1 + defenseDiff1 + totalDiff1;

        // 밸런스 차이가 매우 작을 때만 번갈아가며 배정 (3점 이하)
        if (Math.abs(balance0 - balance1) <= 3) {
            return currentIndex % 2;
        }

        // 밸런스 차이가 크면 더 균형잡힌 팀에 배정
        if (balance0 < balance1) {
            return 0;
        } else if (balance1 < balance0) {
            return 1;
        } else {
            // 밸런스가 같으면 총 능력치가 낮은 팀에 배정
            return (team0TotalWithPlayer <= team1TotalWithPlayer) ? 0 : 1;
        }
    }

    // 더 균형잡힌 팀을 선택하는 헬퍼 메서드
    private int getBetterBalancedTeam(int team0Attack, int team0Defense, int team1Attack, int team1Defense) {
        // 공격력과 수비력의 차이를 계산
        int attackDiff0 = Math.abs(team0Attack - team1Attack);
        int defenseDiff0 = Math.abs(team0Defense - team1Defense);
        int totalDiff0 = Math.abs((team0Attack + team0Defense) - (team1Attack + team1Defense));

        int attackDiff1 = Math.abs(team1Attack - team0Attack);
        int defenseDiff1 = Math.abs(team1Defense - team0Defense);
        int totalDiff1 = Math.abs((team1Attack + team1Defense) - (team0Attack + team0Defense));

        // 팀0에 배정했을 때의 밸런스 점수
        int balance0 = attackDiff0 + defenseDiff0 + totalDiff0;
        // 팀1에 배정했을 때의 밸런스 점수
        int balance1 = attackDiff1 + defenseDiff1 + totalDiff1;

        return (balance0 <= balance1) ? 0 : 1;
    }

    // 강력한 밸런스 조절을 위한 배정 메서드
    private int getStrongBalancedTeam(Player player, List<Team> teams, int targetPerTeam, int remainingPlayers) {
        // 각 팀의 현재 선수 수
        int team0Size = teams.get(0).getPlayers().size();
        int team1Size = teams.get(1).getPlayers().size();

        // 각 팀의 현재 공격력과 수비력 계산
        int team0Attack = teams.get(0).getTotalAttackPower();
        int team0Defense = teams.get(0).getTotalDefensePower();
        int team1Attack = teams.get(1).getTotalAttackPower();
        int team1Defense = teams.get(1).getTotalDefensePower();

        // 현재 선수의 능력치
        int playerAttack = player.getAttackPower();
        int playerDefense = player.getDefensePower();

        // 각 팀에 배정했을 때의 공격력과 수비력
        int team0AttackWithPlayer = team0Attack + playerAttack;
        int team0DefenseWithPlayer = team0Defense + playerDefense;
        int team1AttackWithPlayer = team1Attack + playerAttack;
        int team1DefenseWithPlayer = team1Defense + playerDefense;

        // 각 팀에 배정했을 때의 총 능력치
        int team0TotalWithPlayer = team0AttackWithPlayer + team0DefenseWithPlayer;
        int team1TotalWithPlayer = team1AttackWithPlayer + team1DefenseWithPlayer;

        // 반반 분배가 최우선 - 목표 인원에 맞춰 배정
        if (team0Size < targetPerTeam) {
            return 0; // 첫 번째 팀이 목표 인원보다 적으면 첫 번째 팀에 배정
        } else if (team1Size < targetPerTeam) {
            return 1; // 두 번째 팀이 목표 인원보다 적으면 두 번째 팀에 배정
        } else if (remainingPlayers > 0 && team0Size == targetPerTeam && team1Size == targetPerTeam) {
            // 홀수일 경우 한 팀에 한 명 더 배정 (밸런스 고려)
            return getBetterBalancedTeam(team0AttackWithPlayer, team0DefenseWithPlayer,
                                       team1AttackWithPlayer, team1DefenseWithPlayer);
        }

        // 밸런스 점수 계산 (공격력 차이 + 수비력 차이 + 총 능력치 차이)
        int attackDiff0 = Math.abs(team0AttackWithPlayer - team1AttackWithPlayer);
        int defenseDiff0 = Math.abs(team0DefenseWithPlayer - team1DefenseWithPlayer);
        int totalDiff0 = Math.abs(team0TotalWithPlayer - team1TotalWithPlayer);

        int attackDiff1 = Math.abs(team1AttackWithPlayer - team0AttackWithPlayer);
        int defenseDiff1 = Math.abs(team1DefenseWithPlayer - team0DefenseWithPlayer);
        int totalDiff1 = Math.abs(team1TotalWithPlayer - team0TotalWithPlayer);

        // 팀0에 배정했을 때의 밸런스 점수 (낮을수록 좋음)
        int balance0 = attackDiff0 + defenseDiff0 + totalDiff0;
        // 팀1에 배정했을 때의 밸런스 점수 (낮을수록 좋음)
        int balance1 = attackDiff1 + defenseDiff1 + totalDiff1;

        // 밸런스 차이가 매우 작을 때만 번갈아가며 배정 (3점 이하)
        if (Math.abs(balance0 - balance1) <= 3) {
            return getBetterBalancedTeam(team0AttackWithPlayer, team0DefenseWithPlayer,
                                       team1AttackWithPlayer, team1DefenseWithPlayer);
        }

        // 밸런스 차이가 크면 더 균형잡힌 팀에 배정
        if (balance0 < balance1) {
            return 0;
        } else if (balance1 < balance0) {
            return 1;
        } else {
            // 밸런스가 같으면 총 능력치가 낮은 팀에 배정
            return (team0TotalWithPlayer <= team1TotalWithPlayer) ? 0 : 1;
        }
    }

    // 번갈아가며 배정하되, 밸런스가 나쁘면 즉시 조정하는 방식
    private int getAlternatingBalancedTeam(Player player, List<Team> teams, int targetPerTeam, int remainingPlayers, int currentIndex) {
        // 각 팀의 현재 선수 수
        int team0Size = teams.get(0).getPlayers().size();
        int team1Size = teams.get(1).getPlayers().size();

        // 각 팀의 현재 공격력과 수비력 계산
        int team0Attack = teams.get(0).getTotalAttackPower();
        int team0Defense = teams.get(0).getTotalDefensePower();
        int team1Attack = teams.get(1).getTotalAttackPower();
        int team1Defense = teams.get(1).getTotalDefensePower();

        // 현재 선수의 능력치
        int playerAttack = player.getAttackPower();
        int playerDefense = player.getDefensePower();

        // 각 팀에 배정했을 때의 공격력과 수비력
        int team0AttackWithPlayer = team0Attack + playerAttack;
        int team0DefenseWithPlayer = team0Defense + playerDefense;
        int team1AttackWithPlayer = team1Attack + playerAttack;
        int team1DefenseWithPlayer = team1Defense + playerDefense;

        // 각 팀에 배정했을 때의 총 능력치
        int team0TotalWithPlayer = team0AttackWithPlayer + team0DefenseWithPlayer;
        int team1TotalWithPlayer = team1AttackWithPlayer + team1DefenseWithPlayer;

        System.out.println("=== 선수 " + player.getName() + " 배정 결정 ===");
        System.out.println("현재 팀 상태 - 팀0: " + team0Size + "명(공:" + team0Attack + " 수:" + team0Defense +
                          "), 팀1: " + team1Size + "명(공:" + team1Attack + " 수:" + team1Defense + ")");
        System.out.println("목표 인원: " + targetPerTeam + ", 남은 선수: " + remainingPlayers);
        System.out.println("선수 능력치 - 공격:" + playerAttack + ", 수비:" + playerDefense);
        System.out.println("팀0에 배정 시 - 공격:" + team0AttackWithPlayer + ", 수비:" + team0DefenseWithPlayer);
        System.out.println("팀1에 배정 시 - 공격:" + team1AttackWithPlayer + ", 수비:" + team1DefenseWithPlayer);

        // 반반 분배가 최우선 - 목표 인원에 맞춰 배정
        if (team0Size < targetPerTeam) {
            System.out.println("조건1: 팀0이 목표 인원보다 적음 -> 팀0 배정");
            return 0; // 첫 번째 팀이 목표 인원보다 적으면 첫 번째 팀에 배정
        } else if (team1Size < targetPerTeam) {
            System.out.println("조건2: 팀1이 목표 인원보다 적음 -> 팀1 배정");
            return 1; // 두 번째 팀이 목표 인원보다 적으면 두 번째 팀에 배정
        } else if (remainingPlayers > 0 && team0Size == targetPerTeam && team1Size == targetPerTeam) {
            // 홀수일 경우 한 팀에 한 명 더 배정 (밸런스 고려)
            int result = getBetterBalancedTeam(team0AttackWithPlayer, team0DefenseWithPlayer,
                                       team1AttackWithPlayer, team1DefenseWithPlayer);
            System.out.println("조건3: 홀수일 경우 -> 밸런스에 따라 팀" + result + " 배정");
            return result;
        }

        // 이미 반반 분배가 완료된 경우 번갈아가며 배정
        int result = currentIndex % 2;
        System.out.println("조건4: 이미 반반 분배 완료 -> 번갈아가며 팀" + result + " 배정");
        return result;
    }
}
