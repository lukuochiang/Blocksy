import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";

const LoginView = () => import("../views/login/index.vue");
const DashboardView = () => import("../views/dashboard/index.vue");
const UserView = () => import("../views/user/index.vue");
const PostView = () => import("../views/post/index.vue");
const ListingView = () => import("../views/listing/index.vue");
const EventView = () => import("../views/event/index.vue");
const ReportView = () => import("../views/report/index.vue");
const SystemView = () => import("../views/system/index.vue");
const ModulePlaceholderView = () => import("../views/_shared/module-placeholder.vue");
const CommentView = () => import("../views/comment/index.vue");
const AuditPostView = () => import("../views/audit-post/index.vue");
const RiskSensitiveWordView = () => import("../views/risk-sensitive-word/index.vue");
const PermissionRoleView = () => import("../views/permission-role/index.vue");
const OperationNoteTemplateView = () => import("../views/operation-note-template/index.vue");
const AnnouncementView = () => import("../views/announcement/index.vue");
const RiskAnomalyView = () => import("../views/risk-anomaly/index.vue");
const RiskAppealsView = () => import("../views/risk-appeals/index.vue");
const PermissionMenuView = () => import("../views/permission-menu/index.vue");
const PermissionLoginLogView = () => import("../views/permission-login-log/index.vue");
const PermissionDataView = () => import("../views/permission-data/index.vue");
const PermissionOperationLogView = () => import("../views/permission-operation-log/index.vue");
const UserBehaviorsView = () => import("../views/user-behaviors/index.vue");
const UserVerificationView = () => import("../views/user-verification/index.vue");
const CommunityNoticeView = () => import("../views/community-notice/index.vue");
const EventSignupsView = () => import("../views/event-signups/index.vue");
const CommunityEngagementView = () => import("../views/community-engagement/index.vue");
const CategoryManageView = () => import("../views/category-manage/index.vue");
const MediaManageView = () => import("../views/media-manage/index.vue");
const MessagePushView = () => import("../views/message-push/index.vue");
const MessageRecordView = () => import("../views/message-record/index.vue");
const AnalyticsGrowthView = () => import("../views/analytics-growth/index.vue");
const AnalyticsCommunityView = () => import("../views/analytics-community/index.vue");
const AnalyticsContentView = () => import("../views/analytics-content/index.vue");
const AnalyticsModerationView = () => import("../views/analytics-moderation/index.vue");
const AnalyticsRetentionView = () => import("../views/analytics-retention/index.vue");
const AnalyticsRankingView = () => import("../views/analytics-ranking/index.vue");
const SettingsBasicView = () => import("../views/settings-basic/index.vue");
const SettingsBrandView = () => import("../views/settings-brand/index.vue");
const SettingsUploadView = () => import("../views/settings-upload/index.vue");
const SettingsIntegrationsView = () => import("../views/settings-integrations/index.vue");
const SettingsLegalView = () => import("../views/settings-legal/index.vue");
const UserBlacklistView = () => import("../views/user-blacklist/index.vue");

function placeholderRoute(
  path: string,
  title: string,
  description: string,
  features: Array<{ name: string; usage: string; action: string; role: string }>
): RouteRecordRaw {
  return {
    path,
    component: ModulePlaceholderView,
    meta: {
      title,
      description,
      features
    }
  };
}

const routes: RouteRecordRaw[] = [
  { path: "/login", name: "login", component: LoginView, meta: { public: true, hideShell: true } },
  { path: "/", redirect: "/dashboard" },
  { path: "/dashboard", name: "dashboard", component: DashboardView, meta: { title: "仪表盘" } },
  { path: "/users", name: "users", component: UserView, meta: { title: "用户管理" } },
  { path: "/posts", name: "posts", component: PostView, meta: { title: "帖子管理" } },
  { path: "/listings", name: "listings", component: ListingView, meta: { title: "分类信息管理" } },
  { path: "/events", name: "events", component: EventView, meta: { title: "活动管理" } },
  { path: "/reports", name: "reports", component: ReportView, meta: { title: "举报工单" } },
  { path: "/communities", name: "communities", component: SystemView, meta: { title: "社区管理" } },
  { path: "/comments", name: "comments", component: CommentView, meta: { title: "评论管理" } },
  { path: "/audit/posts", name: "audit-posts", component: AuditPostView, meta: { title: "帖子审核" } },
  { path: "/risk/sensitive-words", name: "risk-sensitive-words", component: RiskSensitiveWordView, meta: { title: "敏感词管理" } },
  { path: "/permissions/roles", name: "permission-roles", component: PermissionRoleView, meta: { title: "角色管理" } },

  { path: "/users/blacklist", name: "user-blacklist", component: UserBlacklistView, meta: { title: "黑名单管理" } },
  { path: "/users/verification", name: "users-verification", component: UserVerificationView, meta: { title: "认证审核" } },
  { path: "/users/behaviors", name: "users-behaviors", component: UserBehaviorsView, meta: { title: "用户行为日志" } },

  { path: "/communities/notices", name: "community-notices", component: CommunityNoticeView, meta: { title: "社区公告" } },
  { path: "/communities/engagement", name: "community-engagement", component: CommunityEngagementView, meta: { title: "社区成员活跃" } },

  { path: "/categories", name: "categories", component: CategoryManageView, meta: { title: "分类管理" } },
  { path: "/media", name: "media", component: MediaManageView, meta: { title: "媒体管理" } },

  { path: "/audit/comments", name: "audit-comments", component: CommentView, meta: { title: "评论审核" } },
  { path: "/audit/users", name: "audit-users", component: UserVerificationView, meta: { title: "用户审核" } },
  { path: "/audit/media", name: "audit-media", component: MediaManageView, meta: { title: "媒体审核" } },

  { path: "/risk/anomaly", name: "risk-anomaly", component: RiskAnomalyView, meta: { title: "异常行为监控" } },
  { path: "/risk/appeals", name: "risk-appeals", component: RiskAppealsView, meta: { title: "申诉处理" } },

  { path: "/events/signups", name: "event-signups", component: EventSignupsView, meta: { title: "活动报名记录" } },
  { path: "/announcements", name: "announcements", component: AnnouncementView, meta: { title: "平台公告" } },

  { path: "/messages/templates", name: "operation-note-templates", component: OperationNoteTemplateView, meta: { title: "备注模板管理" } },
  { path: "/messages/system", name: "message-system", component: AnnouncementView, meta: { title: "系统通知" } },
  { path: "/messages/push", name: "message-push", component: MessagePushView, meta: { title: "Push 推送" } },
  { path: "/messages/records", name: "message-records", component: MessageRecordView, meta: { title: "推送记录" } },

  { path: "/analytics/growth", name: "analytics-growth", component: AnalyticsGrowthView, meta: { title: "用户增长分析" } },
  { path: "/analytics/community", name: "analytics-community", component: AnalyticsCommunityView, meta: { title: "社区活跃分析" } },
  { path: "/analytics/content", name: "analytics-content", component: AnalyticsContentView, meta: { title: "内容活跃分析" } },
  { path: "/analytics/moderation", name: "analytics-moderation", component: AnalyticsModerationView, meta: { title: "审核与举报分析" } },
  { path: "/analytics/retention", name: "analytics-retention", component: AnalyticsRetentionView, meta: { title: "留存转化分析" } },
  { path: "/analytics/ranking", name: "analytics-ranking", component: AnalyticsRankingView, meta: { title: "热门社区排行" } },

  { path: "/permissions/menus", name: "permission-menus", component: PermissionMenuView, meta: { title: "菜单权限" } },
  { path: "/permissions/login-logs", name: "permission-login-logs", component: PermissionLoginLogView, meta: { title: "登录日志" } },
  { path: "/permissions/data", name: "permission-data", component: PermissionDataView, meta: { title: "数据权限" } },
  { path: "/permissions/logs", name: "permission-logs", component: PermissionOperationLogView, meta: { title: "操作日志" } },

  { path: "/settings/basic", name: "settings-basic", component: SettingsBasicView, meta: { title: "平台基础配置" } },
  { path: "/settings/brand", name: "settings-brand", component: SettingsBrandView, meta: { title: "品牌信息配置" } },
  { path: "/settings/legal", name: "settings-legal", component: SettingsLegalView, meta: { title: "协议政策配置" } },
  { path: "/settings/upload", name: "settings-upload", component: SettingsUploadView, meta: { title: "上传配置" } },
  { path: "/settings/integrations", name: "settings-integrations", component: SettingsIntegrationsView, meta: { title: "三方集成配置" } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const token = localStorage.getItem("blocksy_admin_token");
  if (to.meta.public) {
    if (to.path === "/login" && token) {
      return "/dashboard";
    }
    return true;
  }
  if (!token) {
    return "/login";
  }
  return true;
});

export default router;
