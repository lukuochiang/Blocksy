#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-demo}"
PASSWORD="${PASSWORD:-blocksy123}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required. Please install jq first."
  exit 1
fi

echo "[1] login"
LOGIN_RES=$(curl -sS -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}")
TOKEN=$(echo "${LOGIN_RES}" | jq -r '.data.token // empty')
if [ -z "${TOKEN}" ]; then
  echo "login failed: ${LOGIN_RES}"
  exit 1
fi
AUTH_HEADER="Authorization: Bearer ${TOKEN}"

echo "[2] batch5 - audit comments"
curl -sS "${BASE_URL}/api/admin/comments" -H "${AUTH_HEADER}" | jq '.code'

echo "[3] batch5 - audit users(verification)"
curl -sS "${BASE_URL}/api/admin/verifications?page=1&pageSize=5" -H "${AUTH_HEADER}" | jq '.code'

echo "[4] batch5 - audit media"
curl -sS "${BASE_URL}/api/admin/content/media/posts?page=1&pageSize=5" -H "${AUTH_HEADER}" | jq '.code'

echo "[5] batch6 - create and send push task"
TASK_RES=$(curl -sS -X POST "${BASE_URL}/api/admin/notifications/push/tasks" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{
    "title":"batch5-9 smoke push",
    "content":"batch5-9 smoke content",
    "targetType":"ALL"
  }')
TASK_ID=$(echo "${TASK_RES}" | jq -r '.data.id // empty')
if [ -z "${TASK_ID}" ]; then
  echo "create push task failed: ${TASK_RES}"
  exit 1
fi
echo "pushTaskId=${TASK_ID}"
curl -sS -X POST "${BASE_URL}/api/admin/notifications/push/tasks/${TASK_ID}/send" -H "${AUTH_HEADER}" | jq '.data'
curl -sS "${BASE_URL}/api/admin/notifications/push/records?taskId=${TASK_ID}&page=1&pageSize=5" -H "${AUTH_HEADER}" | jq '.code'

echo "[6] batch6 - templates"
curl -sS "${BASE_URL}/api/admin/notifications/templates?page=1&pageSize=10" -H "${AUTH_HEADER}" | jq '.code'

echo "[7] batch7 - analytics"
curl -sS "${BASE_URL}/api/admin/analytics/growth?days=7" -H "${AUTH_HEADER}" | jq '.code'
curl -sS "${BASE_URL}/api/admin/analytics/community?limit=10" -H "${AUTH_HEADER}" | jq '.code'
curl -sS "${BASE_URL}/api/admin/analytics/content?days=7" -H "${AUTH_HEADER}" | jq '.code'
curl -sS "${BASE_URL}/api/admin/analytics/moderation" -H "${AUTH_HEADER}" | jq '.code'
curl -sS "${BASE_URL}/api/admin/analytics/retention?days=7" -H "${AUTH_HEADER}" | jq '.code'
curl -sS "${BASE_URL}/api/admin/analytics/ranking?limit=10" -H "${AUTH_HEADER}" | jq '.code'

echo "[8] batch8 - settings"
SAVE_SETTING_RES=$(curl -sS -X POST "${BASE_URL}/api/admin/settings/items/save" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{
    "settingGroup":"BASIC",
    "settingKey":"smoke_key",
    "settingValue":"smoke_value",
    "description":"smoke"
  }')
echo "${SAVE_SETTING_RES}" | jq '.code'
curl -sS "${BASE_URL}/api/admin/settings/items?settingGroup=BASIC" -H "${AUTH_HEADER}" | jq '.code'

echo "[9] batch8 - policies"
VERSION="smoke-$(date +%s)"
POLICY_RES=$(curl -sS -X POST "${BASE_URL}/api/admin/settings/policies/save" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d "{
    \"policyType\":\"USER_AGREEMENT\",
    \"version\":\"${VERSION}\",
    \"title\":\"Smoke User Agreement\",
    \"content\":\"Smoke content\"
  }")
POLICY_ID=$(echo "${POLICY_RES}" | jq -r '.data.id // empty')
if [ -z "${POLICY_ID}" ]; then
  echo "save policy failed: ${POLICY_RES}"
  exit 1
fi
curl -sS -X POST "${BASE_URL}/api/admin/settings/policies/${POLICY_ID}/activate" -H "${AUTH_HEADER}" | jq '.code'
curl -sS "${BASE_URL}/api/admin/settings/policies?policyType=USER_AGREEMENT" -H "${AUTH_HEADER}" | jq '.code'

echo "batch5-9 regression flow done."
