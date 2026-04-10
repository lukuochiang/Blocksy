#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-demo}"
PASSWORD="${PASSWORD:-blocksy123}"
EXPORT_DIR="${EXPORT_DIR:-/tmp}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required. Please install jq first."
  exit 1
fi

mkdir -p "${EXPORT_DIR}"

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

echo "[2] retry failed event batch and export..."
EVENT_RETRY_RES=$(curl -sS -X POST "${BASE_URL}/api/admin/events/batch-retry" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{"failedEventIds":[-1],"action":"OFFLINE","note":"script retry"}')
echo "${EVENT_RETRY_RES}" | jq '.'

curl -sS -X POST "${BASE_URL}/api/admin/events/batch-retry/export" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{"failedEventIds":[-1],"action":"OFFLINE","note":"script retry export"}' \
  -o "${EXPORT_DIR}/event-batch-retry-result.csv"

if [ ! -s "${EXPORT_DIR}/event-batch-retry-result.csv" ]; then
  echo "event retry export csv empty"
  exit 1
fi

echo "[3] retry failed report batch and export..."
REPORT_RETRY_RES=$(curl -sS -X POST "${BASE_URL}/api/admin/reports/batch-retry" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{"failedReportIds":[-1],"action":"REJECTED","note":"script retry","banTargetUser":false}')
echo "${REPORT_RETRY_RES}" | jq '.'

curl -sS -X POST "${BASE_URL}/api/admin/reports/batch-retry/export" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{"failedReportIds":[-1],"action":"REJECTED","note":"script retry export","banTargetUser":false}' \
  -o "${EXPORT_DIR}/report-batch-retry-result.csv"

if [ ! -s "${EXPORT_DIR}/report-batch-retry-result.csv" ]; then
  echo "report retry export csv empty"
  exit 1
fi

echo "[4] publish system announcement..."
ANNOUNCE_RES=$(curl -sS -X POST "${BASE_URL}/api/admin/notifications/system-announcement" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"系统公告回归 $(date +%s)\",\"content\":\"请关注社区规则\"}")
echo "${ANNOUNCE_RES}" | jq '.'

echo "[5] fetch notification stats..."
NOTIF_STATS=$(curl -sS "${BASE_URL}/api/admin/notifications/stats" -H "${AUTH_HEADER}")
echo "${NOTIF_STATS}" | jq '.'
if [ "$(echo "${NOTIF_STATS}" | jq -r '.code')" != "0" ]; then
  echo "fetch notification stats failed"
  exit 1
fi

echo "[6] fetch governance stats..."
GOV_STATS=$(curl -sS "${BASE_URL}/api/admin/notifications/governance-stats" -H "${AUTH_HEADER}")
echo "${GOV_STATS}" | jq '.'
if [ "$(echo "${GOV_STATS}" | jq -r '.code')" != "0" ]; then
  echo "fetch governance stats failed"
  exit 1
fi

echo "v3.5/v4/v4.5 regression flow done. csv in ${EXPORT_DIR}"
