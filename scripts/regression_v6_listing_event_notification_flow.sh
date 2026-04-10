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

echo "[2] pick community"
COMMUNITY_ID=$(curl -sS "${BASE_URL}/api/communities" | jq -r '.data[0].id // empty')
if [ -z "${COMMUNITY_ID}" ]; then
  echo "community not found"
  exit 1
fi
echo "communityId=${COMMUNITY_ID}"

echo "[3] create listing"
LISTING_TITLE="v6-listing-$(date +%s)"
LISTING_RES=$(curl -sS -X POST "${BASE_URL}/api/listings" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d "{
    \"communityId\": ${COMMUNITY_ID},
    \"category\": \"SECOND_HAND\",
    \"title\": \"${LISTING_TITLE}\",
    \"content\": \"v6 smoke listing content\",
    \"price\": 88.8,
    \"contact\": \"demo-contact\"
  }")
LISTING_ID=$(echo "${LISTING_RES}" | jq -r '.data.id // empty')
if [ -z "${LISTING_ID}" ]; then
  echo "create listing failed: ${LISTING_RES}"
  exit 1
fi
echo "listingId=${LISTING_ID}"

echo "[4] admin approve listing"
curl -sS -X POST "${BASE_URL}/api/admin/listings/${LISTING_ID}/handle" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{"action":"APPROVE","note":"v6 smoke approve"}' | jq '.code'

echo "[5] verify listing visible in public list"
PUBLIC_MATCH=$(curl -sS "${BASE_URL}/api/listings?communityId=${COMMUNITY_ID}&keyword=${LISTING_TITLE}" | jq '.data | map(select(.id == '"${LISTING_ID}"')) | length')
if [ "${PUBLIC_MATCH}" -lt 1 ]; then
  echo "listing should be visible after approve"
  exit 1
fi

echo "[6] create event"
NOW_TS=$(date +%s)
START_TIME=$(date -u -r $((NOW_TS + 3600)) +"%Y-%m-%dT%H:%M:%S")
END_TIME=$(date -u -r $((NOW_TS + 7200)) +"%Y-%m-%dT%H:%M:%S")
EVENT_TITLE="v6-event-${NOW_TS}"
EVENT_RES=$(curl -sS -X POST "${BASE_URL}/api/events" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d "{
    \"communityId\": ${COMMUNITY_ID},
    \"title\": \"${EVENT_TITLE}\",
    \"content\": \"v6 smoke event\",
    \"location\": \"smoke-place\",
    \"startTime\": \"${START_TIME}\",
    \"endTime\": \"${END_TIME}\",
    \"signupLimit\": 10
  }")
EVENT_ID=$(echo "${EVENT_RES}" | jq -r '.data.id // empty')
if [ -z "${EVENT_ID}" ]; then
  echo "create event failed: ${EVENT_RES}"
  exit 1
fi
echo "eventId=${EVENT_ID}"

echo "[7] admin offline event"
curl -sS -X POST "${BASE_URL}/api/admin/events/${EVENT_ID}/handle" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{"action":"OFFLINE","note":"v6 smoke offline"}' | jq '.code'

echo "[8] verify notifications"
NOTIFS=$(curl -sS "${BASE_URL}/api/notifications" -H "${AUTH_HEADER}")
LISTING_NOTIFY=$(echo "${NOTIFS}" | jq '[.data[] | select(.relatedType=="LISTING")] | length')
EVENT_NOTIFY=$(echo "${NOTIFS}" | jq '[.data[] | select(.relatedType=="EVENT")] | length')
if [ "${LISTING_NOTIFY}" -lt 1 ]; then
  echo "listing review notification not found"
  exit 1
fi
if [ "${EVENT_NOTIFY}" -lt 1 ]; then
  echo "event governance notification not found"
  exit 1
fi
echo "notification check passed (listing=${LISTING_NOTIFY}, event=${EVENT_NOTIFY})"

echo "v6 listing-event-notification regression flow done."
