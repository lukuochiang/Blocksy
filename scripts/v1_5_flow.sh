#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080/api}"
USER_NAME="${USER_NAME:-demo}"
USER_PASSWORD="${USER_PASSWORD:-blocksy123}"
ADMIN_NAME="${ADMIN_NAME:-demo}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-blocksy123}"
COMMUNITY_ID="${COMMUNITY_ID:-}"
BAN_TARGET_USER="${BAN_TARGET_USER:-true}"

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

api_put_json() {
  local url="$1"
  local body="$2"
  local token="$3"
  curl -sS -X PUT "$url" -H "Authorization: Bearer $token" -H "Content-Type: application/json" -d "$body"
}

echo "== 1) 用户登录 =="
USER_LOGIN_RESP="$(api_post_json "$BASE_URL/auth/login" "{\"username\":\"$USER_NAME\",\"password\":\"$USER_PASSWORD\"}")"
USER_TOKEN="$(printf '%s' "$USER_LOGIN_RESP" | json_read "d.get('data',{}).get('token')")"
if [[ -z "$USER_TOKEN" ]]; then
  echo "用户登录失败: $USER_LOGIN_RESP"
  exit 1
fi
echo "USER_TOKEN acquired"

echo "== 2) 管理员登录 =="
ADMIN_LOGIN_RESP="$(api_post_json "$BASE_URL/auth/login" "{\"username\":\"$ADMIN_NAME\",\"password\":\"$ADMIN_PASSWORD\"}")"
ADMIN_TOKEN="$(printf '%s' "$ADMIN_LOGIN_RESP" | json_read "d.get('data',{}).get('token')")"
if [[ -z "$ADMIN_TOKEN" ]]; then
  echo "管理员登录失败: $ADMIN_LOGIN_RESP"
  exit 1
fi
echo "ADMIN_TOKEN acquired"

echo "== 3) 获取用户信息和社区 =="
ME_RESP="$(api_get "$BASE_URL/users/me" "$USER_TOKEN")"
DEFAULT_COMMUNITY_ID="$(printf '%s' "$ME_RESP" | json_read "d.get('data',{}).get('defaultCommunityId')")"

if [[ -z "$COMMUNITY_ID" ]]; then
  COMMUNITY_ID="$DEFAULT_COMMUNITY_ID"
fi
if [[ -z "$COMMUNITY_ID" ]]; then
  COMMUNITIES_RESP="$(api_get "$BASE_URL/users/communities" "$USER_TOKEN")"
  COMMUNITY_ID="$(printf '%s' "$COMMUNITIES_RESP" | json_read "(d.get('data') or [{}])[0].get('communityId')")"
fi
if [[ -z "$COMMUNITY_ID" ]]; then
  echo "无法获取可用 community_id，请先初始化社区关系"
  exit 1
fi
echo "Use COMMUNITY_ID=$COMMUNITY_ID"

echo "== 4) 切换社区 =="
SELECT_COMMUNITY_RESP="$(api_put_json "$BASE_URL/users/community" "{\"communityId\":$COMMUNITY_ID}" "$USER_TOKEN")"
echo "$SELECT_COMMUNITY_RESP"

echo "== 5) 发帖 =="
POST_CONTENT="V1.5 flow post $(date +%s)"
CREATE_POST_RESP="$(api_post_json "$BASE_URL/posts" "{\"communityId\":$COMMUNITY_ID,\"content\":\"$POST_CONTENT\",\"media\":[]}" "$USER_TOKEN")"
POST_ID="$(printf '%s' "$CREATE_POST_RESP" | json_read "d.get('data',{}).get('id')")"
if [[ -z "$POST_ID" ]]; then
  echo "发帖失败: $CREATE_POST_RESP"
  exit 1
fi
echo "POST_ID=$POST_ID"

echo "== 6) 举报帖子 =="
CREATE_REPORT_RESP="$(api_post_json "$BASE_URL/reports" "{\"targetType\":\"POST\",\"targetId\":$POST_ID,\"reason\":\"自动化联调举报\",\"description\":\"v1.5 flow\"}" "$USER_TOKEN")"
REPORT_ID="$(printf '%s' "$CREATE_REPORT_RESP" | json_read "d.get('data',{}).get('id')")"
if [[ -z "$REPORT_ID" ]]; then
  echo "举报失败: $CREATE_REPORT_RESP"
  exit 1
fi
echo "REPORT_ID=$REPORT_ID"

echo "== 7) 后台处理举报（可选封禁） =="
HANDLE_REPORT_RESP="$(api_post_json "$BASE_URL/admin/reports/$REPORT_ID/handle" "{\"action\":\"RESOLVED\",\"banTargetUser\":$BAN_TARGET_USER,\"note\":\"flow auto handle\"}" "$ADMIN_TOKEN")"
echo "$HANDLE_REPORT_RESP"

if [[ "$BAN_TARGET_USER" == "true" ]]; then
  echo "== 8) 封禁验证：用户 token 访问 /users/me（预期 401 或 code!=0） =="
  VERIFY_RESP="$(curl -sS -i "$BASE_URL/users/me" -H "Authorization: Bearer $USER_TOKEN")"
  echo "$VERIFY_RESP"
  echo
  echo "若当前被封禁用户是 demo，可用以下 SQL 恢复："
  echo "UPDATE users SET status=1, updated_at=NOW() WHERE username='demo';"
else
  echo "跳过封禁验证（BAN_TARGET_USER=$BAN_TARGET_USER）"
fi

echo "== 完成 =="
