#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080/api}"
ADMIN_NAME="${ADMIN_NAME:-demo}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-blocksy123}"
USER_NAME="${USER_NAME:-demo}"
USER_PASSWORD="${USER_PASSWORD:-blocksy123}"
BAN_DURATION_HOURS="${BAN_DURATION_HOURS:-24}"
BAN_REASON="${BAN_REASON:-脚本联调封禁}"

json_read() {
  local expr="$1"
  python3 -c "import json,sys; d=json.load(sys.stdin); print($expr if $expr is not None else '')"
}

api_post_json() {
  local url="$1"
  local body="$2"
  local token="${3:-}"
  if [[ -n "$token" ]]; then
    curl -sS -X POST "$url" -H "Authorization: Bearer $token" -H "Content-Type: application/json" -d "$body"
  else
    curl -sS -X POST "$url" -H "Content-Type: application/json" -d "$body"
  fi
}

api_get() {
  local url="$1"
  local token="${2:-}"
  if [[ -n "$token" ]]; then
    curl -sS "$url" -H "Authorization: Bearer $token"
  else
    curl -sS "$url"
  fi
}

echo "== 1) 管理员登录 =="
ADMIN_LOGIN_RESP="$(api_post_json "$BASE_URL/auth/login" "{\"username\":\"$ADMIN_NAME\",\"password\":\"$ADMIN_PASSWORD\"}")"
ADMIN_TOKEN="$(printf '%s' "$ADMIN_LOGIN_RESP" | json_read "d.get('data',{}).get('token')")"
if [[ -z "$ADMIN_TOKEN" ]]; then
  echo "管理员登录失败: $ADMIN_LOGIN_RESP"
  exit 1
fi
echo "ADMIN_TOKEN acquired"

echo "== 2) 用户登录（获取 userId 和 user token） =="
USER_LOGIN_RESP="$(api_post_json "$BASE_URL/auth/login" "{\"username\":\"$USER_NAME\",\"password\":\"$USER_PASSWORD\"}")"
USER_TOKEN="$(printf '%s' "$USER_LOGIN_RESP" | json_read "d.get('data',{}).get('token')")"
USER_ID="$(printf '%s' "$USER_LOGIN_RESP" | json_read "d.get('data',{}).get('userId')")"
if [[ -z "$USER_TOKEN" || -z "$USER_ID" ]]; then
  echo "用户登录失败: $USER_LOGIN_RESP"
  exit 1
fi
echo "USER_ID=$USER_ID"

echo "== 3) 管理员封禁用户（原因 + 时长） =="
BAN_RESP="$(api_post_json "$BASE_URL/admin/users/$USER_ID/ban" "{\"reason\":\"$BAN_REASON\",\"durationHours\":$BAN_DURATION_HOURS}" "$ADMIN_TOKEN")"
echo "$BAN_RESP"

echo "== 4) 验证用户 token 是否被拦截（预期 401 或业务失败） =="
VERIFY_BAN_RESP="$(curl -sS -i "$BASE_URL/users/me" -H "Authorization: Bearer $USER_TOKEN")"
echo "$VERIFY_BAN_RESP"

echo "== 5) 查询用户处罚日志 =="
PUNISH_LOGS_RESP="$(api_get "$BASE_URL/admin/users/$USER_ID/punish-logs" "$ADMIN_TOKEN")"
echo "$PUNISH_LOGS_RESP"

echo "== 6) 解封用户 =="
UNBAN_RESP="$(api_post_json "$BASE_URL/admin/users/$USER_ID/unban" "{\"reason\":\"脚本联调解封\"}" "$ADMIN_TOKEN")"
echo "$UNBAN_RESP"

echo "== 7) 重新登录用户并验证 /users/me =="
USER_LOGIN_RESP_AFTER="$(api_post_json "$BASE_URL/auth/login" "{\"username\":\"$USER_NAME\",\"password\":\"$USER_PASSWORD\"}")"
USER_TOKEN_AFTER="$(printf '%s' "$USER_LOGIN_RESP_AFTER" | json_read "d.get('data',{}).get('token')")"
if [[ -z "$USER_TOKEN_AFTER" ]]; then
  echo "解封后用户登录失败: $USER_LOGIN_RESP_AFTER"
  exit 1
fi
ME_AFTER_RESP="$(api_get "$BASE_URL/users/me" "$USER_TOKEN_AFTER")"
echo "$ME_AFTER_RESP"

echo "== 完成 =="
