import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import LoginView from "../views/login/index.vue";
import DashboardView from "../views/dashboard/index.vue";
import UserView from "../views/user/index.vue";
import PostView from "../views/post/index.vue";
import ListingView from "../views/listing/index.vue";
import EventView from "../views/event/index.vue";
import ReportView from "../views/report/index.vue";
import SystemView from "../views/system/index.vue";
import ModulePlaceholderView from "../views/_shared/module-placeholder.vue";
import CommentView from "../views/comment/index.vue";
import AuditPostView from "../views/audit-post/index.vue";
import RiskSensitiveWordView from "../views/risk-sensitive-word/index.vue";
import PermissionRoleView from "../views/permission-role/index.vue";

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

  placeholderRoute("/users/blacklist", "黑名单管理", "维护风险账号名单，支持统一拦截与恢复。", [
    { name: "黑名单列表", usage: "查看被拉黑账号与处理原因。", action: "按状态筛选、恢复账号。", role: "审核员、超级管理员" },
    { name: "添加黑名单", usage: "将严重违规账号快速纳入重点管控。", action: "填写原因并设置有效期。", role: "审核员、风控" }
  ]),
  placeholderRoute("/users/verification", "用户认证审核", "处理实名/住户认证，提高社区真实度。", [
    { name: "认证队列", usage: "统一查看待审认证申请。", action: "通过、驳回并留痕。", role: "审核员、社区管理员" },
    { name: "驳回模板", usage: "标准化驳回口径，减少沟通成本。", action: "选择模板并补充备注。", role: "审核员" }
  ]),
  placeholderRoute("/users/behaviors", "用户行为日志", "沉淀用户行为轨迹，支持问题追溯。", [
    { name: "行为时间线", usage: "查看登录、发帖、处罚轨迹。", action: "按用户ID查询、导出记录。", role: "审核员、客服" },
    { name: "登录设备/IP", usage: "识别异常登录和共享账号。", action: "查看设备指纹与异地告警。", role: "风控、审核员" }
  ]),

  placeholderRoute("/communities/notices", "社区公告管理", "面向社区成员发布运营通知。", [
    { name: "公告发布", usage: "创建图文公告并控制发布时间。", action: "新增、置顶、撤回。", role: "社区管理员、运营" },
    { name: "阅读统计", usage: "评估公告触达效果。", action: "查看已读/未读与点击率。", role: "运营、数据分析员" }
  ]),
  placeholderRoute("/communities/engagement", "社区成员活跃", "观察社区活跃质量与成员结构。", [
    { name: "活跃指标", usage: "查看DAU、发帖率、互动率。", action: "按社区对比趋势。", role: "运营、数据分析员" },
    { name: "成员列表", usage: "定位重点成员与低活跃群体。", action: "筛选、导出、分组运营。", role: "运营" }
  ]),

  placeholderRoute("/categories", "分类管理", "维护内容分类，支撑分发与筛选。", [
    { name: "分类配置", usage: "统一维护分类体系。", action: "新增、排序、启停。", role: "平台运营" },
    { name: "分类统计", usage: "观察分类下内容与互动表现。", action: "查看发帖量、互动量。", role: "运营、分析员" }
  ]),
  placeholderRoute("/media", "媒体内容管理", "管理图片/视频内容安全与质量。", [
    { name: "媒体资源池", usage: "集中查看媒体文件。", action: "按类型、上传人、时间筛选。", role: "审核员" },
    { name: "违规处理", usage: "快速执行媒体下架。", action: "一键下架并记录原因。", role: "审核员、风控" }
  ]),

  placeholderRoute("/audit/comments", "评论审核", "过滤辱骂、引战、广告评论。", [
    { name: "实时审核流", usage: "处理高频评论。", action: "批量通过/驳回。", role: "审核员" },
    { name: "风险标签", usage: "提升审核优先级。", action: "优先处理高风险评论。", role: "审核员、风控" }
  ]),
  placeholderRoute("/audit/users", "用户认证审核", "处理用户身份与住户认证申请。", [
    { name: "材料审核", usage: "验证用户提交材料真实性。", action: "通过、驳回、补件。", role: "审核员" },
    { name: "结果回写", usage: "同步认证状态。", action: "更新用户认证标签。", role: "审核员、运营" }
  ]),
  placeholderRoute("/audit/media", "媒体审核", "图片/视频内容安全审核入口。", [
    { name: "机审复核", usage: "对机审命中样本复核。", action: "确认违规或放行。", role: "审核员" },
    { name: "批量审核", usage: "提升审核效率。", action: "批量通过、批量驳回。", role: "审核员" }
  ]),

  placeholderRoute("/risk/anomaly", "异常行为监控", "识别刷帖、刷评、异常登录。", [
    { name: "异常面板", usage: "查看异常行为告警。", action: "按规则命中项追踪。", role: "风控、审核员" },
    { name: "风险处置", usage: "快速联动处罚能力。", action: "禁言、封禁、观察。", role: "审核员、超级管理员" }
  ]),
  placeholderRoute("/risk/appeals", "申诉处理", "处理用户处罚申诉，保证治理公平。", [
    { name: "申诉单管理", usage: "统一处理申诉流程。", action: "受理、复核、结案。", role: "客服、审核员" },
    { name: "结果通知", usage: "同步申诉处理结果。", action: "站内信/系统消息通知。", role: "客服、运营" }
  ]),

  placeholderRoute("/events/signups", "活动报名记录", "管理活动报名与核销数据。", [
    { name: "报名列表", usage: "查看参与用户明细。", action: "筛选、导出、核销。", role: "运营、社区管理员" },
    { name: "人数统计", usage: "评估活动转化情况。", action: "查看报名率、到场率。", role: "运营、分析员" }
  ]),
  placeholderRoute("/announcements", "平台公告", "发布平台级公告并定向触达。", [
    { name: "公告发布", usage: "面向全站或指定社区发布公告。", action: "草稿、发布、撤回。", role: "平台运营" },
    { name: "触达统计", usage: "查看公告触达效果。", action: "阅读率、点击率分析。", role: "运营、分析员" }
  ]),

  placeholderRoute("/messages/templates", "站内信模板", "统一维护系统通知模板。", [
    { name: "模板配置", usage: "降低重复编辑成本。", action: "新增、编辑、版本管理。", role: "运营" },
    { name: "变量管理", usage: "支持消息动态渲染。", action: "定义模板变量与示例。", role: "运营、开发" }
  ]),
  placeholderRoute("/messages/system", "系统通知", "管理平台系统消息任务。", [
    { name: "通知任务", usage: "批量发送平台通知。", action: "创建任务、定时发送。", role: "运营" },
    { name: "发送状态", usage: "监控发送成功率。", action: "重试失败任务。", role: "运营、客服" }
  ]),
  placeholderRoute("/messages/push", "Push 推送", "管理 App Push 推送任务。", [
    { name: "推送任务", usage: "按人群触达。", action: "定向推送、A/B 文案。", role: "运营" },
    { name: "效果统计", usage: "评估推送质量。", action: "查看送达率和点击率。", role: "运营、分析员" }
  ]),
  placeholderRoute("/messages/records", "推送记录", "沉淀消息发送审计记录。", [
    { name: "记录列表", usage: "回溯消息历史。", action: "按渠道、任务、用户筛选。", role: "运营、客服" },
    { name: "错误诊断", usage: "定位发送失败原因。", action: "查看错误码与重试状态。", role: "运营、开发" }
  ]),

  placeholderRoute("/analytics/growth", "用户增长分析", "监控新增、净增、活跃趋势。", [
    { name: "增长趋势", usage: "评估拉新效果。", action: "按天周月切换分析。", role: "分析员、运营" },
    { name: "来源分析", usage: "定位增长来源。", action: "按渠道、社区分组。", role: "分析员" }
  ]),
  placeholderRoute("/analytics/community", "社区活跃分析", "跟踪社区层级活跃与互动。", [
    { name: "活跃排行", usage: "识别高潜社区。", action: "按活跃率排序。", role: "运营、分析员" },
    { name: "结构分析", usage: "观察社区内容结构。", action: "帖子、评论、举报占比。", role: "分析员" }
  ]),
  placeholderRoute("/analytics/content", "内容活跃分析", "分析内容质量与传播效率。", [
    { name: "内容表现", usage: "查看帖子互动质量。", action: "统计阅读、评论、举报。", role: "运营、分析员" },
    { name: "分类表现", usage: "比较不同分类内容质量。", action: "按分类看互动率。", role: "运营、分析员" }
  ]),
  placeholderRoute("/analytics/moderation", "审核与举报分析", "评估审核效率与治理效果。", [
    { name: "审核效率", usage: "监控审核SLA。", action: "统计平均处理时长。", role: "审核主管、分析员" },
    { name: "举报闭环", usage: "查看举报处理质量。", action: "统计结案率、误判率。", role: "风控、审核主管" }
  ]),
  placeholderRoute("/analytics/retention", "留存转化分析", "评估用户粘性与关键行为转化。", [
    { name: "留存看板", usage: "观察次日/7日/30日留存。", action: "按社区分层对比。", role: "分析员、产品" },
    { name: "转化漏斗", usage: "识别流失环节。", action: "登录-浏览-发帖-互动漏斗。", role: "分析员、产品" }
  ]),
  placeholderRoute("/analytics/ranking", "热门社区排行", "展示社区综合活跃排名。", [
    { name: "热度排行", usage: "辅助运营资源倾斜。", action: "按分值排行与导出。", role: "运营、分析员" },
    { name: "变化趋势", usage: "监控社区热度变化。", action: "查看周环比变化。", role: "运营、分析员" }
  ]),

  placeholderRoute("/permissions/menus", "菜单权限", "控制后台功能菜单可见性。", [
    { name: "菜单授权", usage: "按角色配置页面访问。", action: "勾选可访问菜单。", role: "超级管理员" },
    { name: "权限校验", usage: "验证菜单权限生效。", action: "模拟角色预览。", role: "超级管理员、开发" }
  ]),
  placeholderRoute("/permissions/data", "数据权限", "控制可见数据范围。", [
    { name: "数据域授权", usage: "限制账号可见社区/区域。", action: "按社区或区域授权。", role: "超级管理员" },
    { name: "权限模板", usage: "标准化常见授权组合。", action: "创建并复用模板。", role: "超级管理员" }
  ]),
  placeholderRoute("/permissions/logs", "操作日志", "审计后台关键操作记录。", [
    { name: "日志检索", usage: "追踪关键行为责任人。", action: "按账号/模块/时间筛选。", role: "超级管理员、审计" },
    { name: "日志导出", usage: "支持审计合规需求。", action: "导出CSV归档。", role: "超级管理员" }
  ]),

  placeholderRoute("/settings/basic", "平台基础配置", "维护平台基础参数。", [
    { name: "基础信息", usage: "统一平台名称和描述。", action: "修改并发布配置。", role: "超级管理员" },
    { name: "运行参数", usage: "配置默认分页、时区等。", action: "编辑并保存。", role: "超级管理员、开发" }
  ]),
  placeholderRoute("/settings/brand", "品牌信息配置", "维护Logo、品牌文案等资产。", [
    { name: "品牌资产", usage: "统一后台与前台品牌识别。", action: "上传Logo、设置品牌名。", role: "超级管理员、运营" },
    { name: "视觉配置", usage: "支持主题与配色规范。", action: "调整主题色。", role: "超级管理员、设计" }
  ]),
  placeholderRoute("/settings/legal", "协议政策配置", "管理协议与隐私政策版本。", [
    { name: "协议版本", usage: "保障合规和追溯。", action: "新增版本、启用版本。", role: "法务、超级管理员" },
    { name: "展示配置", usage: "控制前台展示内容。", action: "配置生效范围。", role: "法务、运营" }
  ]),
  placeholderRoute("/settings/upload", "上传配置", "配置文件上传限制与策略。", [
    { name: "上传限制", usage: "控制文件大小和格式。", action: "设置白名单、大小限制。", role: "超级管理员、开发" },
    { name: "存储策略", usage: "配置存储桶与访问策略。", action: "设置公开/私有访问。", role: "开发、运维" }
  ]),
  placeholderRoute("/settings/integrations", "三方集成配置", "管理短信/邮件/第三方登录配置。", [
    { name: "渠道配置", usage: "统一配置外部服务。", action: "录入 key、secret、模板。", role: "超级管理员、开发" },
    { name: "健康检查", usage: "验证三方通道可用性。", action: "执行测试发送。", role: "开发、运维" }
  ])
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
