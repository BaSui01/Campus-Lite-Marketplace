/**
 * 枚举常量定义
 * @author BaSui 😎
 * @description 各种业务枚举的常量定义（下拉选项、标签等）
 */

// ==================== 用户角色 ====================

/**
 * 用户角色选项
 */
export const USER_ROLE_OPTIONS = [
  { label: '普通用户', value: 'USER' },
  { label: '管理员', value: 'ADMIN' },
  { label: '超级管理员', value: 'SUPER_ADMIN' },
] as const;

/**
 * 性别选项
 */
export const GENDER_OPTIONS = [
  { label: '男', value: 'MALE' },
  { label: '女', value: 'FEMALE' },
  { label: '未知', value: 'UNKNOWN' },
] as const;

// ==================== 排序相关 ====================

/**
 * 排序方向选项
 */
export const SORT_ORDER_OPTIONS = [
  { label: '升序', value: 'ASC' },
  { label: '降序', value: 'DESC' },
] as const;

/**
 * 物品排序字段选项
 */
export const GOODS_SORT_FIELD_OPTIONS = [
  { label: '创建时间', value: 'createdAt' },
  { label: '价格', value: 'price' },
  { label: '浏览次数', value: 'viewCount' },
  { label: '收藏次数', value: 'favoriteCount' },
  { label: '已售数量', value: 'soldCount' },
] as const;

/**
 * 帖子排序字段选项
 */
export const POST_SORT_FIELD_OPTIONS = [
  { label: '创建时间', value: 'createdAt' },
  { label: '浏览次数', value: 'viewCount' },
  { label: '点赞次数', value: 'likeCount' },
  { label: '评论次数', value: 'commentCount' },
] as const;

// ==================== 日期范围 ====================

/**
 * 日期范围选项
 */
export const DATE_RANGE_OPTIONS = [
  { label: '今天', value: 'TODAY' },
  { label: '最近7天', value: 'LAST_7_DAYS' },
  { label: '最近30天', value: 'LAST_30_DAYS' },
  { label: '最近3个月', value: 'LAST_3_MONTHS' },
  { label: '最近6个月', value: 'LAST_6_MONTHS' },
  { label: '最近1年', value: 'LAST_YEAR' },
  { label: '自定义', value: 'CUSTOM' },
] as const;

// ==================== 分页 ====================
// 注意：PAGE_SIZE_OPTIONS 和 DEFAULT_PAGE_SIZE 已在 config.ts 中定义，避免重复

/**
 * 默认页码（从 1 开始）
 */
export const DEFAULT_PAGE = 1;

// ==================== 积分相关 ====================

/**
 * 积分类型选项
 */
export const POINTS_TYPE_OPTIONS = [
  { label: '获得积分', value: 'EARN' },
  { label: '消费积分', value: 'SPEND' },
] as const;

/**
 * 积分来源选项
 */
export const POINTS_SOURCE_OPTIONS = [
  { label: '注册奖励', value: 'REGISTER' },
  { label: '签到奖励', value: 'SIGN_IN' },
  { label: '发布物品', value: 'PUBLISH_GOODS' },
  { label: '完成交易', value: 'COMPLETE_ORDER' },
  { label: '评价订单', value: 'REVIEW_ORDER' },
  { label: '积分支付', value: 'POINTS_PAYMENT' },
  { label: '系统调整', value: 'SYSTEM_ADJUST' },
] as const;

// ==================== 举报类型 ====================

/**
 * 举报类型选项
 */
export const REPORT_TYPE_OPTIONS = [
  { label: '违规商品', value: 'GOODS' },
  { label: '违规订单', value: 'ORDER' },
  { label: '违规用户', value: 'USER' },
  { label: '违规帖子', value: 'POST' },
  { label: '违规评论', value: 'REPLY' },
] as const;

/**
 * 举报原因选项
 */
export const REPORT_REASON_OPTIONS = [
  { label: '虚假信息', value: 'FAKE_INFO' },
  { label: '违禁物品', value: 'BANNED_ITEM' },
  { label: '欺诈行为', value: 'FRAUD' },
  { label: '侵犯版权', value: 'COPYRIGHT' },
  { label: '侮辱谩骂', value: 'ABUSE' },
  { label: '色情低俗', value: 'PORNOGRAPHY' },
  { label: '广告骚扰', value: 'SPAM' },
  { label: '其他原因', value: 'OTHER' },
] as const;

// ==================== 退款原因 ====================

/**
 * 退款原因选项
 */
export const REFUND_REASON_OPTIONS = [
  { label: '不想要了', value: 'DONT_WANT' },
  { label: '商品质量问题', value: 'QUALITY_ISSUE' },
  { label: '商品与描述不符', value: 'NOT_AS_DESCRIBED' },
  { label: '卖家发错货', value: 'WRONG_ITEM' },
  { label: '其他原因', value: 'OTHER' },
] as const;

// ==================== 操作类型 ====================

/**
 * 操作类型选项（审计日志）
 */
export const ACTION_TYPE_OPTIONS = [
  { label: '创建', value: 'CREATE' },
  { label: '更新', value: 'UPDATE' },
  { label: '删除', value: 'DELETE' },
  { label: '查询', value: 'READ' },
  { label: '登录', value: 'LOGIN' },
  { label: '登出', value: 'LOGOUT' },
  { label: '导出', value: 'EXPORT' },
  { label: '导入', value: 'IMPORT' },
] as const;

// ==================== 文件类型 ====================

/**
 * 文件类型选项
 */
export const FILE_TYPE_OPTIONS = [
  { label: '图片', value: 'IMAGE' },
  { label: '视频', value: 'VIDEO' },
  { label: '音频', value: 'AUDIO' },
  { label: '文档', value: 'DOCUMENT' },
  { label: '其他', value: 'OTHER' },
] as const;

/**
 * 图片文件MIME类型
 */
export const IMAGE_MIME_TYPES = [
  'image/jpeg',
  'image/png',
  'image/gif',
  'image/webp',
  'image/svg+xml',
] as const;

/**
 * 视频文件MIME类型
 */
export const VIDEO_MIME_TYPES = [
  'video/mp4',
  'video/webm',
  'video/ogg',
  'video/quicktime',
] as const;

/**
 * 音频文件MIME类型
 */
export const AUDIO_MIME_TYPES = [
  'audio/mpeg',
  'audio/wav',
  'audio/ogg',
  'audio/webm',
] as const;

/**
 * 文档文件MIME类型
 */
export const DOCUMENT_MIME_TYPES = [
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/vnd.ms-excel',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  'application/vnd.ms-powerpoint',
  'application/vnd.openxmlformats-officedocument.presentationml.presentation',
  'text/plain',
] as const;

// ==================== 评分 ====================

/**
 * 评分选项（1-5星）
 */
export const RATING_OPTIONS = [
  { label: '1星（非常差）', value: 1, icon: '⭐' },
  { label: '2星（较差）', value: 2, icon: '⭐⭐' },
  { label: '3星（一般）', value: 3, icon: '⭐⭐⭐' },
  { label: '4星（满意）', value: 4, icon: '⭐⭐⭐⭐' },
  { label: '5星（非常满意）', value: 5, icon: '⭐⭐⭐⭐⭐' },
] as const;

// ==================== 时间相关 ====================

/**
 * 一天的毫秒数
 */
export const ONE_DAY_MS = 24 * 60 * 60 * 1000;

/**
 * 一小时的毫秒数
 */
export const ONE_HOUR_MS = 60 * 60 * 1000;

/**
 * 一分钟的毫秒数
 */
export const ONE_MINUTE_MS = 60 * 1000;

/**
 * 一秒的毫秒数
 */
export const ONE_SECOND_MS = 1000;

// ==================== 价格相关 ====================

/**
 * 价格范围选项（单位：元）
 */
export const PRICE_RANGE_OPTIONS = [
  { label: '0-50元', min: 0, max: 50 },
  { label: '50-100元', min: 50, max: 100 },
  { label: '100-200元', min: 100, max: 200 },
  { label: '200-500元', min: 200, max: 500 },
  { label: '500-1000元', min: 500, max: 1000 },
  { label: '1000元以上', min: 1000, max: null },
] as const;

/**
 * 分转元
 */
export const CENT_TO_YUAN = 100;

/**
 * 元转分
 */
export const YUAN_TO_CENT = 100;

// ==================== 布尔值选项 ====================

/**
 * 是/否选项
 */
export const YES_NO_OPTIONS = [
  { label: '是', value: true },
  { label: '否', value: false },
] as const;

/**
 * 启用/禁用选项
 */
export const ENABLE_DISABLE_OPTIONS = [
  { label: '启用', value: true },
  { label: '禁用', value: false },
] as const;

// ==================== 导出工具函数 ====================

/**
 * 将枚举转换为选项列表
 * @param enumObj 枚举对象
 * @param labels 标签映射
 * @returns 选项列表
 */
export function enumToOptions<T extends Record<string, string>>(
  enumObj: T,
  labels: Record<keyof T, string>
): Array<{ label: string; value: T[keyof T] }> {
  return Object.keys(enumObj).map((key) => ({
    label: labels[key as keyof T],
    value: enumObj[key as keyof T],
  }));
}

/**
 * 根据值获取标签
 * @param value 值
 * @param options 选项列表
 * @returns 标签
 */
export function getLabelByValue<T>(
  value: T,
  options: ReadonlyArray<{ label: string; value: T }>
): string | undefined {
  return options.find((option) => option.value === value)?.label;
}
