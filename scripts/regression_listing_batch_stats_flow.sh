#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-demo}"
PASSWORD="${PASSWORD:-blocksy123}"

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

NOW_TS=$(date +%s)
create_listing() {
  local title="$1"
  local category="$2"
  local response
  response=$(curl -sS -X POST "${BASE_URL}/api/listings" \
    -H "${AUTH_HEADER}" \
    -H "Content-Type: application/json" \
    -d "{
      \"communityId\": ${COMMUNITY_ID},
      \"category\": \"${category}\",
      \"title\": \"${title}\",
      \"content\": \"批量审核回归测试\",
      \"price\": 99,
      \"contact\": \"13800000000\"
    }")
  echo "${response}" | jq -r '.data.id // empty'
}

echo "[3] create pending listings..."
LISTING_ID_1=$(create_listing "Batch Listing A ${NOW_TS}" "SECOND_HAND")
LISTING_ID_2=$(create_listing "Batch Listing B ${NOW_TS}" "LOST_FOUND")
if [ -z "${LISTING_ID_1}" ] || [ -z "${LISTING_ID_2}" ]; then
  echo "create listing failed"
  exit 1
fi
echo "listingIds=${LISTING_ID_1},${LISTING_ID_2}"

echo "[4] batch approve listings..."
BATCH_RES=$(curl -sS -X POST "${BASE_URL}/api/admin/listings/batch-handle" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d "{
    \"listingIds\": [${LISTING_ID_1}, ${LISTING_ID_2}],
    \"action\": \"APPROVE\",
    \"note\": \"脚本批量通过\"
  }")
echo "${BATCH_RES}" | jq '.'

SUCCESS_COUNT=$(echo "${BATCH_RES}" | jq -r '.data.successCount // 0')
if [ "${SUCCESS_COUNT}" -lt 2 ]; then
  echo "batch approve did not process 2 listings"
  exit 1
fi

echo "[5] query category stats..."
STATS_RES=$(curl -sS "${BASE_URL}/api/admin/listings/stats/category?communityId=${COMMUNITY_ID}" \
  -H "${AUTH_HEADER}")
echo "${STATS_RES}" | jq '.'

TOTAL_COUNT=$(echo "${STATS_RES}" | jq -r '[.data[] | .totalCount] | add // 0')
if [ "${TOTAL_COUNT}" -lt 2 ]; then
  echo "stats total seems incorrect: ${TOTAL_COUNT}"
  exit 1
fi

echo "[6] verify public listing filter/sort..."
PUBLIC_RES=$(curl -sS "${BASE_URL}/api/listings?communityId=${COMMUNITY_ID}&category=SECOND_HAND&sortBy=PRICE&sortOrder=ASC&minPrice=1&maxPrice=9999")
echo "${PUBLIC_RES}" | jq '.data[0]'

echo "listing batch + stats regression done."
