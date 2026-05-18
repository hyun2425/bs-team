#!/usr/bin/env bash
set -euo pipefail

APP_DIR="${APP_DIR:-/app}"
JAR_NAME="${JAR_NAME:-bs-team.jar}"
JAVA_BIN="${JAVA_BIN:-/app/openlogic-openjdk-17.0.11+9-linux-x64/bin/java}"
LOG_FILE="${LOG_FILE:-${APP_DIR}/bs-team.log}"
PID_FILE="${PID_FILE:-${APP_DIR}/bs-team.pid}"

cd "$APP_DIR"

if [ ! -x "$JAVA_BIN" ]; then
  echo "Java executable not found or not executable: $JAVA_BIN" >&2
  exit 1
fi

if [ ! -f "$JAR_NAME" ]; then
  echo "Jar file not found: ${APP_DIR}/${JAR_NAME}" >&2
  exit 1
fi

echo "Stopping ${JAR_NAME}..."

PIDS=""
if [ -f "$PID_FILE" ]; then
  OLD_PID="$(cat "$PID_FILE" || true)"
  if [ -n "$OLD_PID" ] && kill -0 "$OLD_PID" 2>/dev/null; then
    PIDS="$OLD_PID"
  fi
fi

MATCHED_PIDS="$(pgrep -f "java .*${JAR_NAME}" || true)"
if [ -n "$MATCHED_PIDS" ]; then
  PIDS="$(printf "%s\n%s\n" "$PIDS" "$MATCHED_PIDS" | awk 'NF && !seen[$0]++')"
fi

if [ -n "$PIDS" ]; then
  echo "$PIDS" | xargs -r kill
  for _ in $(seq 1 15); do
    RUNNING=""
    while IFS= read -r PID; do
      if [ -n "$PID" ] && kill -0 "$PID" 2>/dev/null; then
        RUNNING=1
      fi
    done <<< "$PIDS"

    if [ -z "$RUNNING" ]; then
      break
    fi
    sleep 1
  done

  STILL_RUNNING=""
  while IFS= read -r PID; do
    if [ -n "$PID" ] && kill -0 "$PID" 2>/dev/null; then
      STILL_RUNNING="${STILL_RUNNING}${PID} "
    fi
  done <<< "$PIDS"

  if [ -n "$STILL_RUNNING" ]; then
    echo "Force stopping: $STILL_RUNNING"
    echo "$STILL_RUNNING" | xargs -r kill -9
  fi
else
  echo "No running process found."
fi

echo "Starting ${JAR_NAME}..."
nohup "$JAVA_BIN" -jar "$JAR_NAME" > "$LOG_FILE" 2>&1 &
NEW_PID="$!"
echo "$NEW_PID" > "$PID_FILE"

echo "Started ${JAR_NAME} with PID ${NEW_PID}"
echo "Log: ${LOG_FILE}"
