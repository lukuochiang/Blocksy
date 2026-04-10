#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-demo}"
PASSWORD="${PASSWORD:-blocksy123}"
EXPORT_FILE="${EXPORT_FILE:-/tmp/event-handle-logs.csv}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required. Please install jq first."
  exit 1
fi

echo "[1] login admin..."
LOGIN_RES=$(curl -sS -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}")
TOKEN=$(echo "${LOGIN_RES}" | jq -r '.data.token // empty')
if [ -z "${TOKEN}" ]; then
  echo "login failed: ${LOGIN_RES}"
  exit 1
fi
AUTH_HEADER="Authorization: Bearer ${TOKEN}"

echo "[2] fetch community..."
COMMUNITY_ID=$(curl -sS "${BASE_URL}/api/communities" | jq -r '.data[0].id // empty')
if [ -z "${COMMUNITY_ID}" ]; then
  echo "community not found"
  exit 1
fi

echo "[3] create two events..."
NOW_TS=$(date +%s)
START_TIME=$(date -u -r $((NOW_TS + 3600)) +"%Y-%m-%dT%H:%M:%S")
END_TIME=$(date -u -r $((NOW_TS + 7200)) +"%Y-%m-%dT%H:%M:%S")

create_event() {
  local title="$1"
  local response
  response=$(curl -sS -X POST "${BASE_URL}/api/events" \
    -H "${AUTH_HEADER}" \
    -H "Content-Type: application/json" \
    -d "{
      \"communityId\": ${COMMUNITY_ID},
      \"title\": \"${title}\",
      \"content\": \"回归测试活动\",
      \"location\": \"社区广场\",
      \"startTime\": \"${START_TIME}\",
      \"endTime\": \"${END_TIME}\",
      \"signupLimit\": 10
    }")
  echo "${response}" | jq -r '.data.id // empty'
}

EVENT_ID_1=$(create_event "Batch Event A ${NOW_TS}")
EVENT_ID_2=$(create_event "Batch Event B ${NOW_TS}")
if [ -z "${EVENT_ID_1}" ] || [ -z "${EVENT_ID_2}" ]; then
  echo "create event failed"
  exit 1
fi
echo "eventIds=${EVENT_ID_1},${EVENT_ID_2}"

echo "[4] batch offline events..."
BATCH_RES=$(curl -sS -X POST "${BASE_URL}/api/admin/events/batch-handle" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d "{
    \"eventIds\": [${EVENT_ID_1}, ${EVENT_ID_2}],
    \"action\": \"OFFLINE\",
    \"note\": \"脚本批量下架\"
  }")
echo "${BATCH_RES}" | jq '.'

SUCCESS_COUNT=$(echo "${BATCH_RES}" | jq -r '.data.successCount // 0')
if [ "${SUCCESS_COUNT}" -lt 2 ]; then
  echo "batch handle did not process 2 events"
  exit 1
fi

echo "[5] query handle logs with filters..."
LOGS_RES=$(curl -sS "${BASE_URL}/api/admin/events/handle-logs?eventId=${EVENT_ID_1}&action=OFFLINE" \
  -H "${AUTH_HEADER}")
echo "${LOGS_RES}" | jq '.'
LOG_COUNT=$(echo "${LOGS_RES}" | jq -r '.data | length')
if [ "${LOG_COUNT}" -lt 1 ]; then
  echo "expected logs not found"
  exit 1
fi

echo "[6] export logs csv..."
curl -sS "${BASE_URL}/api/admin/events/handle-logs/export?eventId=${EVENT_ID_1}&action=OFFLINE" \
  -H "${AUTH_HEADER}" \
  -o "${EXPORT_FILE}"
if [ ! -s "${EXPORT_FILE}" ]; then
  echo "export file is empty"
  exit 1
fi
head -n 5 "${EXPORT_FILE}"

if ! grep -q "log_id,event_id,operator_user_id,action,note,created_at" "${EXPORT_FILE}"; then
  echo "csv header missing"
  exit 1
fi

echo "event batch + export regression done. file=${EXPORT_FILE}"

