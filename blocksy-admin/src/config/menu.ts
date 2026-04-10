export interface MenuChildItem {
  label: string;
  path: string;
}

export interface MenuGroupItem {
  label: string;
  children: MenuChildItem[];
}

export const menuGroups: MenuGroupItem[] = [
  {
    label: "总览",
    children: [
      { label: "仪表盘", path: "/dashboard" }
    ]
  },
  {
    label: "用户中心",
    children: [
      { label: "用户管理", path: "/users" },
      { label: "黑名单管理", path: "/users/blacklist" },
      { label: "认证审核", path: "/users/verification" },
      { label: "用户行为日志", path: "/users/behaviors" }
    ]
  },
  {
    label: "社区管理",
    children: [
      { label: "社区基础信息", path: "/communities" },
      { label: "社区公告", path: "/communities/notices" },
      { label: "社区成员活跃", path: "/communities/engagement" }
    ]
  },
  {
    label: "内容管理",
    children: [
      { label: "帖子管理", path: "/posts" },
      { label: "分类信息管理", path: "/listings" },
      { label: "评论管理", path: "/comments" },
      { label: "分类管理", path: "/categories" },
      { label: "媒体管理", path: "/media" }
    ]
  },
  {
    label: "审核中心",
    children: [
      { label: "帖子审核", path: "/audit/posts" },
      { label: "评论审核", path: "/audit/comments" },
      { label: "用户审核", path: "/audit/users" },
      { label: "媒体审核", path: "/audit/media" }
    ]
  },
  {
    label: "举报与风控",
    children: [
      { label: "举报工单", path: "/reports" },
      { label: "敏感词管理", path: "/risk/sensitive-words" },
      { label: "异常行为监控", path: "/risk/anomaly" },
      { label: "申诉处理", path: "/risk/appeals" }
    ]
  },
  {
    label: "活动与公告",
    children: [
      { label: "活动管理", path: "/events" },
      { label: "活动报名记录", path: "/events/signups" },
      { label: "平台公告", path: "/announcements" }
    ]
  },
  {
    label: "消息与推送",
    children: [
      { label: "备注模板管理", path: "/messages/templates" },
      { label: "系统通知", path: "/messages/system" },
      { label: "Push 推送", path: "/messages/push" },
      { label: "推送记录", path: "/messages/records" }
    ]
  },
  {
    label: "数据分析",
    children: [
      { label: "用户增长", path: "/analytics/growth" },
      { label: "社区活跃分析", path: "/analytics/community" },
      { label: "内容活跃分析", path: "/analytics/content" },
      { label: "审核与举报分析", path: "/analytics/moderation" },
      { label: "留存转化分析", path: "/analytics/retention" },
      { label: "热门社区排行", path: "/analytics/ranking" }
    ]
  },
  {
    label: "权限与角色",
    children: [
      { label: "角色管理", path: "/permissions/roles" },
      { label: "菜单权限", path: "/permissions/menus" },
      { label: "登录日志", path: "/permissions/login-logs" },
      { label: "数据权限", path: "/permissions/data" },
      { label: "操作日志", path: "/permissions/logs" }
    ]
  },
  {
    label: "系统设置",
    children: [
      { label: "平台基础配置", path: "/settings/basic" },
      { label: "品牌信息配置", path: "/settings/brand" },
      { label: "协议政策配置", path: "/settings/legal" },
      { label: "上传配置", path: "/settings/upload" },
      { label: "三方集成配置", path: "/settings/integrations" }
    ]
  }
];
