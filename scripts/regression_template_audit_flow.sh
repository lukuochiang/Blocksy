#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-demo}"
PASSWORD="${PASSWORD:-blocksy123}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required. Please install jq first."
  exit 1
fi

echo "[1] login..."
LOGIN_RES=$(curl -sS -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}")
TOKEN=$(echo "${LOGIN_RES}" | jq -r '.data.token // empty')
if [ -z "${TOKEN}" ]; then
  echo "login failed: ${LOGIN_RES}"
  exit 1
fi
AUTH_HEADER="Authorization: Bearer ${TOKEN}"

echo "[2] check template permission..."
PERM_RES=$(curl -sS "${BASE_URL}/api/admin/operation-note-templates/permission" -H "${AUTH_HEADER}")
CAN_MANAGE=$(echo "${PERM_RES}" | jq -r '.data // false')
echo "canManage=${CAN_MANAGE}"
if [ "${CAN_MANAGE}" != "true" ]; then
  echo "current user has no template manage permission."
  exit 1
fi

echo "[3] create template..."
CREATE_RES=$(curl -sS -X POST "${BASE_URL}/api/admin/operation-note-templates" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{
    "module":"EVENT",
    "action":"OFFLINE",
    "content":"脚本新增模板",
    "sortNo":90,
    "status":1
  }')
TEMPLATE_ID=$(echo "${CREATE_RES}" | jq -r '.data.id // empty')
if [ -z "${TEMPLATE_ID}" ]; then
  echo "create template failed: ${CREATE_RES}"
  exit 1
fi
echo "templateId=${TEMPLATE_ID}"

echo "[4] disable template..."
curl -sS -X POST "${BASE_URL}/api/admin/operation-note-templates/${TEMPLATE_ID}/status?status=0" \
  -H "${AUTH_HEADER}" | jq '.'

echo "[5] query template logs..."
LOG_RES=$(curl -sS "${BASE_URL}/api/admin/operation-note-templates/logs?templateId=${TEMPLATE_ID}" \
  -H "${AUTH_HEADER}")
echo "${LOG_RES}" | jq '.'
LOG_COUNT=$(echo "${LOG_RES}" | jq -r '.data | length')
if [ "${LOG_COUNT}" -lt 2 ]; then
  echo "expected at least 2 logs (CREATE + DISABLE), actual=${LOG_COUNT}"
  exit 1
fi

echo "template audit regression done."

