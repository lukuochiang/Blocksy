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
echo "token acquired."

echo "[2] fetch communities..."
COMMUNITY_RES=$(curl -sS "${BASE_URL}/api/communities")
COMMUNITY_ID=$(echo "${COMMUNITY_RES}" | jq -r '.data[0].id // empty')
if [ -z "${COMMUNITY_ID}" ]; then
  echo "no community found: ${COMMUNITY_RES}"
  exit 1
fi
echo "communityId=${COMMUNITY_ID}"

echo "[3] create listing..."
LISTING_TITLE="MVP Listing $(date +%s)"
CREATE_LISTING_RES=$(curl -sS -X POST "${BASE_URL}/api/listings" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"communityId\": ${COMMUNITY_ID},
    \"category\": \"SECOND_HAND\",
    \"title\": \"${LISTING_TITLE}\",
    \"content\": \"九成新，支持当面看货\",
    \"price\": 199,
    \"contact\": \"13800000000\"
  }")
LISTING_ID=$(echo "${CREATE_LISTING_RES}" | jq -r '.data.id // empty')
if [ -z "${LISTING_ID}" ]; then
  echo "create listing failed: ${CREATE_LISTING_RES}"
  exit 1
fi
echo "listing created, id=${LISTING_ID}"

echo "[4] list my listings..."
curl -sS "${BASE_URL}/api/listings/mine?communityId=${COMMUNITY_ID}" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'

echo "[5] create event..."
NOW_TS=$(date +%s)
START_TIME=$(date -u -r $((NOW_TS + 3600)) +"%Y-%m-%dT%H:%M:%S")
END_TIME=$(date -u -r $((NOW_TS + 7200)) +"%Y-%m-%dT%H:%M:%S")
EVENT_TITLE="MVP Event ${NOW_TS}"
CREATE_EVENT_RES=$(curl -sS -X POST "${BASE_URL}/api/events" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"communityId\": ${COMMUNITY_ID},
    \"title\": \"${EVENT_TITLE}\",
    \"content\": \"周末活动测试\",
    \"location\": \"社区广场\",
    \"startTime\": \"${START_TIME}\",
    \"endTime\": \"${END_TIME}\",
    \"signupLimit\": 20
  }")
EVENT_ID=$(echo "${CREATE_EVENT_RES}" | jq -r '.data.id // empty')
if [ -z "${EVENT_ID}" ]; then
  echo "create event failed: ${CREATE_EVENT_RES}"
  exit 1
fi
echo "event created, id=${EVENT_ID}"

echo "[6] signup event..."
SIGNUP_RES=$(curl -sS -X POST "${BASE_URL}/api/events/${EVENT_ID}/signup" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"remark\":\"脚本自动报名\"}")
echo "${SIGNUP_RES}" | jq '.'

echo "[7] list my signups..."
curl -sS "${BASE_URL}/api/events/my-signups" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'

echo "[8] notification unread-count..."
curl -sS "${BASE_URL}/api/notifications/unread-count" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'

echo "[9] mark all notifications read..."
curl -sS -X POST "${BASE_URL}/api/notifications/read-all" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'

echo "phase 1/2/3 closure flow done."

