#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-demo}"
PASSWORD="${PASSWORD:-blocksy123}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required. Please install jq first."
  exit 1
fi

echo "[1] login admin"
LOGIN_RES=$(curl -sS -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}")
TOKEN=$(echo "${LOGIN_RES}" | jq -r '.data.token // empty')
if [ -z "${TOKEN}" ]; then
  echo "login failed: ${LOGIN_RES}"
  exit 1
fi
AUTH_HEADER="Authorization: Bearer ${TOKEN}"

COMMUNITY_ID=$(curl -sS "${BASE_URL}/api/communities" | jq -r '.data[0].id // empty')
if [ -z "${COMMUNITY_ID}" ]; then
  echo "community not found"
  exit 1
fi

echo "[2] create listing"
LISTING_TITLE="后台处理测试 $(date +%s)"
CREATE_RES=$(curl -sS -X POST "${BASE_URL}/api/listings" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d "{
    \"communityId\": ${COMMUNITY_ID},
    \"category\": \"SECOND_HAND\",
    \"title\": \"${LISTING_TITLE}\",
    \"content\": \"回归测试分类信息\",
    \"price\": 88,
    \"contact\": \"13800000000\"
  }")
LISTING_ID=$(echo "${CREATE_RES}" | jq -r '.data.id // empty')
if [ -z "${LISTING_ID}" ]; then
  echo "create listing failed: ${CREATE_RES}"
  exit 1
fi
echo "listingId=${LISTING_ID}"

echo "[3] admin list listings"
curl -sS "${BASE_URL}/api/admin/listings?communityId=${COMMUNITY_ID}" -H "${AUTH_HEADER}" | jq '.data | length'

echo "[4] admin offline listing"
curl -sS -X POST "${BASE_URL}/api/admin/listings/${LISTING_ID}/handle" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d "{
    \"action\": \"OFFLINE\",
    \"note\": \"自动脚本下架\"
  }" | jq '.'

echo "[5] verify public list does not contain listing"
COUNT=$(curl -sS "${BASE_URL}/api/listings?communityId=${COMMUNITY_ID}" | jq "[.data[] | select(.id == ${LISTING_ID})] | length")
if [ "${COUNT}" != "0" ]; then
  echo "expect listing removed from public list, actual count=${COUNT}"
  exit 1
fi
echo "public visibility check passed."

echo "[6] admin restore listing"
curl -sS -X POST "${BASE_URL}/api/admin/listings/${LISTING_ID}/handle" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d "{
    \"action\": \"RESTORE\",
    \"note\": \"自动脚本恢复\"
  }" | jq '.'

echo "[7] fetch operation logs"
curl -sS "${BASE_URL}/api/admin/listings/${LISTING_ID}/logs" -H "${AUTH_HEADER}" | jq '.'

echo "listing admin regression flow done."

