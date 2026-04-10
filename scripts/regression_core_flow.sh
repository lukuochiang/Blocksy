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

echo "[2] me"
curl -sS "${BASE_URL}/api/users/me" -H "${AUTH_HEADER}" | jq '.'

echo "[3] community list"
COMMUNITY_ID=$(curl -sS "${BASE_URL}/api/communities" | jq -r '.data[0].id // empty')
if [ -z "${COMMUNITY_ID}" ]; then
  echo "community not found"
  exit 1
fi
echo "communityId=${COMMUNITY_ID}"

echo "[4] create post"
POST_CONTENT="回归测试帖子 $(date +%s)"
POST_RES=$(curl -sS -X POST "${BASE_URL}/api/posts" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d "{
    \"communityId\": ${COMMUNITY_ID},
    \"content\": \"${POST_CONTENT}\",
    \"media\": []
  }")
POST_ID=$(echo "${POST_RES}" | jq -r '.data.id // empty')
if [ -z "${POST_ID}" ]; then
  echo "create post failed: ${POST_RES}"
  exit 1
fi
echo "postId=${POST_ID}"

echo "[5] list posts"
curl -sS "${BASE_URL}/api/posts?communityId=${COMMUNITY_ID}" | jq '.data | length'

echo "[6] comment post"
COMMENT_RES=$(curl -sS -X POST "${BASE_URL}/api/comments" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d "{
    \"postId\": ${POST_ID},
    \"content\": \"回归测试评论\"
  }")
COMMENT_ID=$(echo "${COMMENT_RES}" | jq -r '.data.id // empty')
if [ -z "${COMMENT_ID}" ]; then
  echo "create comment failed: ${COMMENT_RES}"
  exit 1
fi
echo "commentId=${COMMENT_ID}"

echo "[7] get comments"
curl -sS "${BASE_URL}/api/comments?postId=${POST_ID}" | jq '.data | length'

echo "[8] create report"
REPORT_RES=$(curl -sS -X POST "${BASE_URL}/api/reports" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d "{
    \"targetType\": \"POST\",
    \"targetId\": ${POST_ID},
    \"reason\": \"测试举报\",
    \"description\": \"自动化回归验证\"
  }")
REPORT_ID=$(echo "${REPORT_RES}" | jq -r '.data.id // empty')
if [ -z "${REPORT_ID}" ]; then
  echo "create report failed: ${REPORT_RES}"
  exit 1
fi
echo "reportId=${REPORT_ID}"

echo "[9] admin handle report"
curl -sS -X POST "${BASE_URL}/api/admin/reports/${REPORT_ID}/handle" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d "{
    \"action\": \"DISMISS\",
    \"handlerNote\": \"脚本回归自动处理\",
    \"banTargetUser\": false,
    \"banReason\": null,
    \"banDurationHours\": null
  }" | jq '.'

echo "core regression flow done."

