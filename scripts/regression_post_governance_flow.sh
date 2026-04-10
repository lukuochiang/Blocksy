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

echo "[2] query community"
COMMUNITY_ID=$(curl -sS "${BASE_URL}/api/communities" | jq -r '.data[0].id // empty')
if [ -z "${COMMUNITY_ID}" ]; then
  echo "community not found"
  exit 1
fi
echo "communityId=${COMMUNITY_ID}"

echo "[3] create post"
POST_CONTENT="post-governance-smoke-$(date +%s)"
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

echo "[4] verify user post page query"
POST_PAGE_RES=$(curl -sS "${BASE_URL}/api/posts?communityId=${COMMUNITY_ID}&keyword=${POST_CONTENT}&page=1&pageSize=5")
MATCH_COUNT=$(echo "${POST_PAGE_RES}" | jq '.data.items | map(select(.id == '"${POST_ID}"')) | length')
if [ "${MATCH_COUNT}" -lt 1 ]; then
  echo "post not found in public paged query: ${POST_PAGE_RES}"
  exit 1
fi

echo "[5] verify admin post page query"
ADMIN_PAGE_RES=$(curl -sS "${BASE_URL}/api/admin/posts?communityId=${COMMUNITY_ID}&keyword=${POST_CONTENT}&page=1&pageSize=10" \
  -H "${AUTH_HEADER}")
ADMIN_MATCH=$(echo "${ADMIN_PAGE_RES}" | jq '.data.items | map(select(.id == '"${POST_ID}"')) | length')
if [ "${ADMIN_MATCH}" -lt 1 ]; then
  echo "post not found in admin paged query: ${ADMIN_PAGE_RES}"
  exit 1
fi

echo "[6] admin reject post"
curl -sS -X POST "${BASE_URL}/api/admin/posts/${POST_ID}/review" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{"action":"REJECT"}' | jq '.'

echo "[7] verify post hidden from public query"
HIDDEN_COUNT=$(curl -sS "${BASE_URL}/api/posts?communityId=${COMMUNITY_ID}&keyword=${POST_CONTENT}&page=1&pageSize=5" | jq '.data.items | map(select(.id == '"${POST_ID}"')) | length')
if [ "${HIDDEN_COUNT}" -ne 0 ]; then
  echo "rejected post should not appear in public list"
  exit 1
fi

echo "[8] admin approve post"
curl -sS -X POST "${BASE_URL}/api/admin/posts/${POST_ID}/review" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{"action":"APPROVE"}' | jq '.'

echo "[9] verify post recovered"
RECOVERED_COUNT=$(curl -sS "${BASE_URL}/api/posts?communityId=${COMMUNITY_ID}&keyword=${POST_CONTENT}&page=1&pageSize=5" | jq '.data.items | map(select(.id == '"${POST_ID}"')) | length')
if [ "${RECOVERED_COUNT}" -lt 1 ]; then
  echo "approved post should appear in public list"
  exit 1
fi

echo "post governance regression flow done."
