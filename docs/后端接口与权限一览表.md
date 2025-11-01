# 后端接口与权限一览

更新时间：2025-10-30 13:37

## 1. 统计概览
- 控制器数量：35 个
- 方法级接口：157 个
- 具备显式角色校验的接口：48 个
- 具备显式权限编码校验的接口：55 个

| 控制器 | 基础路径 | 接口数 | 无注解 | isAuthenticated | hasRole | hasAuthority | 其他 |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: |
| AdminController | /api/admin | 13 | 0 | 0 | 0 | 13 | 0 |
| AuditLogController | /api/audit-logs | 1 | 0 | 0 | 0 | 1 | 0 |
| AuthController | /api/auth | 10 | 10 | 0 | 0 | 0 | 0 |
| BlacklistController | /api/blacklist | 4 | 0 | 0 | 4 | 0 | 0 |
| CampusController | /api/admin/campuses | 6 | 0 | 0 | 0 | 6 | 0 |
| CategoryController | - | 4 | 1 | 0 | 0 | 3 | 0 |
| ComplianceAdminController | /api/admin/compliance | 3 | 0 | 0 | 0 | 3 | 0 |
| ExportController | /api/exports | 4 | 4 | 0 | 0 | 0 | 0 |
| FavoriteController | /api/favorites | 4 | 1 | 0 | 3 | 0 | 0 |
| FeatureFlagController | /api/feature-flags | 4 | 4 | 0 | 0 | 0 | 0 |
| FileController | /api/files | 3 | 0 | 3 | 0 | 0 | 0 |
| FollowController | /api | 3 | 0 | 0 | 3 | 0 | 0 |
| GoodsController | /api/goods | 5 | 2 | 0 | 1 | 2 | 0 |
| MessageController | /api/messages | 6 | 0 | 0 | 6 | 0 | 0 |
| NotificationController | /api/notifications | 5 | 0 | 5 | 0 | 0 | 0 |
| NotificationPreferenceController | /api/notifications/preferences | 5 | 0 | 5 | 0 | 0 | 0 |
| NotificationTemplateAdminController | /api/admin/notification-templates | 4 | 0 | 0 | 0 | 4 | 0 |
| OrderController | /api/orders | 5 | 0 | 0 | 5 | 0 | 0 |
| PaymentController | /api/payment | 4 | 2 | 0 | 2 | 0 | 0 |
| PaymentMockController | /api/payment/mock | 1 | 0 | 0 | 1 | 0 | 0 |
| PerfSeedController | /api/perf/orders/timeout | 2 | 0 | 0 | 2 | 0 | 0 |
| PostController | /api/posts | 8 | 4 | 0 | 3 | 1 | 0 |
| PrivacyController | - | 4 | 0 | 0 | 2 | 2 | 0 |
| RateLimitAdminController | /api/admin/rate-limit | 8 | 0 | 0 | 0 | 8 | 0 |
| RecommendController | /api/recommend | 3 | 1 | 0 | 2 | 0 | 0 |
| RefundController | /api | 4 | 0 | 0 | 4 | 0 | 0 |
| ReplyController | /api/replies | 4 | 2 | 0 | 2 | 0 | 0 |
| ReportController | /api/reports | 4 | 0 | 0 | 2 | 2 | 0 |
| RoleAdminController | /api/admin | 6 | 0 | 0 | 0 | 6 | 0 |
| SearchController | /api/search | 1 | 1 | 0 | 0 | 0 | 0 |
| SoftDeleteAdminController | /api/admin/soft-delete | 3 | 0 | 0 | 3 | 0 | 0 |
| SubscriptionController | /api | 3 | 0 | 0 | 3 | 0 | 0 |
| TagController | - | 5 | 1 | 0 | 0 | 4 | 0 |
| TaskController | /api/tasks | 4 | 4 | 0 | 0 | 0 | 0 |
| UserController | /api/users | 4 | 1 | 3 | 0 | 0 | 0 |

## 2. 权限分级说明
- **公开**：通过 `SecurityConfig` 放行，无需登录即可访问。
- **需登录**：方法声明 `@PreAuthorize("isAuthenticated()")`，要求持有有效 JWT。
- **需登录(全局)**：未标注 `@PreAuthorize`，依赖 `SecurityConfig.anyRequest().authenticated()`，建议后续补充细粒度权限。
- **hasRole / hasAnyRole**：按业务角色（如 STUDENT、TEACHER、ADMIN、SUPER_ADMIN）控制访问。
- **hasAuthority**：按 `PermissionCodes` 中定义的系统权限字符串进行校验。

## 3. 接口分级清单
### 3.1 公开接口
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| AuthController | POST | `/api/auth/login` |
| AuthController | POST | `/api/auth/logout` |
| AuthController | POST | `/api/auth/password/reset/code/email` |
| AuthController | POST | `/api/auth/password/reset/code/sms` |
| AuthController | POST | `/api/auth/password/reset/email` |
| AuthController | POST | `/api/auth/password/reset/sms` |
| AuthController | POST | `/api/auth/refresh` |
| AuthController | POST | `/api/auth/register` |
| AuthController | POST | `/api/auth/register/by-email` |
| AuthController | POST | `/api/auth/register/code` |
| CategoryController | GET | `/api/categories/tree` |
| GoodsController | GET | `/api/goods` |
| GoodsController | GET | `/api/goods/{id}` |
| PaymentController | POST | `/api/payment/alipay/refund/notify` |
| PaymentController | POST | `/api/payment/wechat/notify` |
| PostController | GET | `/api/posts` |
| PostController | GET | `/api/posts/search` |
| PostController | GET | `/api/posts/user/{authorId}` |
| PostController | GET | `/api/posts/{id}` |
| RecommendController | GET | `/api/recommend/hot` |
| ReplyController | GET | `/api/replies/post/{postId}` |
| ReplyController | GET | `/api/replies/{parentId}/sub` |
| SearchController | GET | `/api/search` |
| TagController | GET | `/api/tags` |
| UserController | GET | `/api/users/{userId}` |

### 3.2 登录用户接口
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| FileController | DELETE | `/api/files` |
| FileController | POST | `/api/files/upload` |
| FileController | POST | `/api/files/upload-with-thumbnail` |
| NotificationController | GET | `/api/notifications` |
| NotificationController | DELETE | `/api/notifications` |
| NotificationController | PUT | `/api/notifications/mark-all-read` |
| NotificationController | PUT | `/api/notifications/mark-read` |
| NotificationController | GET | `/api/notifications/unread-count` |
| NotificationPreferenceController | POST | `/api/notifications/preferences/channel/{channel}/enabled/{enabled}` |
| NotificationPreferenceController | POST | `/api/notifications/preferences/channel/{channel}/quiet-hours` |
| NotificationPreferenceController | GET | `/api/notifications/preferences/status` |
| NotificationPreferenceController | POST | `/api/notifications/preferences/unsubscribe/{channel}/{templateCode}` |
| NotificationPreferenceController | DELETE | `/api/notifications/preferences/unsubscribe/{channel}/{templateCode}` |
| UserController | PUT | `/api/users/password` |
| UserController | GET | `/api/users/profile` |
| UserController | PUT | `/api/users/profile` |

### 3.3 角色接口
#### hasAnyRole('ADMIN','SUPER_ADMIN')
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| RecommendController | POST | `/api/recommend/admin/hot/refresh` |
| RefundController | GET | `/api/admin/refunds/{refundNo}` |
| RefundController | PUT | `/api/admin/refunds/{refundNo}/approve` |
| RefundController | PUT | `/api/admin/refunds/{refundNo}/reject` |

#### hasAnyRole('STUDENT', 'TEACHER')
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| OrderController | POST | `/api/orders` |
| OrderController | GET | `/api/orders/buyer` |
| OrderController | GET | `/api/orders/seller` |
| OrderController | GET | `/api/orders/{orderNo}` |
| OrderController | POST | `/api/orders/{orderNo}/cancel` |
| PaymentController | POST | `/api/payment/create` |
| PaymentController | GET | `/api/payment/status/{orderNo}` |

#### hasAnyRole('STUDENT','TEACHER')
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| RefundController | POST | `/api/orders/{orderNo}/refunds` |

#### hasAnyRole('STUDENT','TEACHER','ADMIN','SUPER_ADMIN')
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| RecommendController | GET | `/api/recommend/personal` |

#### hasRole('ADMIN')
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| SoftDeleteAdminController | GET | `/api/admin/soft-delete/targets` |
| SoftDeleteAdminController | DELETE | `/api/admin/soft-delete/{entity}/{id}/purge` |
| SoftDeleteAdminController | POST | `/api/admin/soft-delete/{entity}/{id}/restore` |

#### hasRole('STUDENT')
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| FavoriteController | GET | `/api/favorites` |
| FavoriteController | POST | `/api/favorites/{goodsId}` |
| FavoriteController | DELETE | `/api/favorites/{goodsId}` |
| FollowController | POST | `/api/follow/{sellerId}` |
| FollowController | DELETE | `/api/follow/{sellerId}` |
| FollowController | GET | `/api/following` |
| GoodsController | POST | `/api/goods` |
| PostController | POST | `/api/posts` |
| PostController | PUT | `/api/posts/{id}` |
| PostController | DELETE | `/api/posts/{id}` |
| PrivacyController | POST | `/api/privacy` |
| PrivacyController | GET | `/api/privacy` |
| ReplyController | POST | `/api/replies` |
| ReplyController | DELETE | `/api/replies/{id}` |
| ReportController | POST | `/api/reports` |
| ReportController | GET | `/api/reports/my` |
| SubscriptionController | POST | `/api/subscribe` |
| SubscriptionController | GET | `/api/subscribe` |
| SubscriptionController | DELETE | `/api/subscribe/{id}` |

#### hasRole('SUPER_ADMIN')
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| PaymentMockController | POST | `/api/payment/mock/wechat` |
| PerfSeedController | DELETE | `/api/perf/orders/timeout` |
| PerfSeedController | POST | `/api/perf/orders/timeout/seed` |

#### hasRole('USER')
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| BlacklistController | POST | `/api/blacklist/block/{blockedUserId}` |
| BlacklistController | GET | `/api/blacklist/check/{blockedUserId}` |
| BlacklistController | GET | `/api/blacklist/list` |
| BlacklistController | DELETE | `/api/blacklist/unblock/{blockedUserId}` |
| MessageController | GET | `/api/messages/conversations` |
| MessageController | POST | `/api/messages/conversations/{conversationId}/mark-read` |
| MessageController | GET | `/api/messages/conversations/{conversationId}/messages` |
| MessageController | POST | `/api/messages/messages/{messageId}/recall` |
| MessageController | POST | `/api/messages/send` |
| MessageController | GET | `/api/messages/unread-count` |

### 3.4 权限编码接口
#### hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_AUDIT_VIEW)
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| AuditLogController | GET | `/api/audit-logs` |

#### hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_MANAGE)
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| CampusController | GET | `/api/admin/campuses` |
| CampusController | POST | `/api/admin/campuses` |
| CampusController | POST | `/api/admin/campuses/migrate-users` |
| CampusController | POST | `/api/admin/campuses/migrate-users/validate` |
| CampusController | PUT | `/api/admin/campuses/{id}` |
| CampusController | DELETE | `/api/admin/campuses/{id}` |

#### hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_MANAGE)
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| CategoryController | POST | `/api/admin/categories` |
| CategoryController | PUT | `/api/admin/categories/{id}` |
| CategoryController | DELETE | `/api/admin/categories/{id}` |

#### hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_COMPLIANCE_REVIEW)
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| ComplianceAdminController | GET | `/api/admin/compliance/audit` |
| ComplianceAdminController | POST | `/api/admin/compliance/whitelist` |
| ComplianceAdminController | DELETE | `/api/admin/compliance/whitelist/{id}` |
| PrivacyController | GET | `/api/admin/privacy/requests` |
| PrivacyController | POST | `/api/admin/privacy/requests/{id}/complete` |

#### hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_GOODS_APPROVE)
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| GoodsController | GET | `/api/goods/pending` |
| GoodsController | POST | `/api/goods/{id}/approve` |

#### hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_POST_APPROVE)
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| PostController | POST | `/api/posts/{id}/approve` |

#### hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| NotificationTemplateAdminController | GET | `/api/admin/notification-templates` |
| NotificationTemplateAdminController | POST | `/api/admin/notification-templates` |
| NotificationTemplateAdminController | POST | `/api/admin/notification-templates/render/{code}` |
| NotificationTemplateAdminController | DELETE | `/api/admin/notification-templates/{id}` |
| RateLimitAdminController | POST | `/api/admin/rate-limit/blacklist/ips/{ip}` |
| RateLimitAdminController | DELETE | `/api/admin/rate-limit/blacklist/ips/{ip}` |
| RateLimitAdminController | POST | `/api/admin/rate-limit/enabled/{enabled}` |
| RateLimitAdminController | GET | `/api/admin/rate-limit/rules` |
| RateLimitAdminController | POST | `/api/admin/rate-limit/whitelist/ips/{ip}` |
| RateLimitAdminController | DELETE | `/api/admin/rate-limit/whitelist/ips/{ip}` |
| RateLimitAdminController | POST | `/api/admin/rate-limit/whitelist/users/{userId}` |
| RateLimitAdminController | DELETE | `/api/admin/rate-limit/whitelist/users/{userId}` |

#### hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_REPORT_HANDLE)
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| ReportController | GET | `/api/reports/pending` |
| ReportController | POST | `/api/reports/{id}/handle` |

#### hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_ROLE_ASSIGN)
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| RoleAdminController | GET | `/api/admin/roles` |
| RoleAdminController | POST | `/api/admin/roles` |
| RoleAdminController | GET | `/api/admin/roles/{roleId}` |
| RoleAdminController | PUT | `/api/admin/roles/{roleId}` |
| RoleAdminController | DELETE | `/api/admin/roles/{roleId}` |
| RoleAdminController | PUT | `/api/admin/users/{userId}/roles` |

#### hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| AdminController | GET | `/api/admin/statistics/categories` |
| AdminController | GET | `/api/admin/statistics/goods` |
| AdminController | GET | `/api/admin/statistics/orders` |
| AdminController | GET | `/api/admin/statistics/overview` |
| AdminController | GET | `/api/admin/statistics/revenue` |
| AdminController | GET | `/api/admin/statistics/today` |
| AdminController | GET | `/api/admin/statistics/top-goods` |
| AdminController | GET | `/api/admin/statistics/top-users` |
| AdminController | GET | `/api/admin/statistics/trend` |
| AdminController | GET | `/api/admin/statistics/users` |

#### hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_MANAGE)
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| TagController | POST | `/api/admin/tags` |
| TagController | POST | `/api/admin/tags/merge` |
| TagController | PUT | `/api/admin/tags/{id}` |
| TagController | DELETE | `/api/admin/tags/{id}` |

#### hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| AdminController | POST | `/api/admin/users/auto-unban` |
| AdminController | POST | `/api/admin/users/ban` |
| AdminController | POST | `/api/admin/users/{userId}/unban` |

### 3.5 仅依赖全局登录控制的接口（待补充更细权限）
| 控制器 | HTTP | 路径 |
| --- | --- | --- |
| ExportController | POST | `/api/exports` |
| ExportController | GET | `/api/exports` |
| ExportController | GET | `/api/exports/download/{token}` |
| ExportController | POST | `/api/exports/{id}/cancel` |
| FavoriteController | GET | `/api/favorites/{goodsId}/check` |
| FeatureFlagController | GET | `/api/feature-flags` |
| FeatureFlagController | POST | `/api/feature-flags` |
| FeatureFlagController | POST | `/api/feature-flags/refresh` |
| FeatureFlagController | DELETE | `/api/feature-flags/{key}` |
| TaskController | GET | `/api/tasks` |
| TaskController | POST | `/api/tasks/{name}/pause` |
| TaskController | POST | `/api/tasks/{name}/resume` |
| TaskController | POST | `/api/tasks/{name}/trigger` |

## 4. 常用 DTO 复用速查
| DTO 类 | 用途 | 相关接口 |
| --- | --- | --- |
| RegisterRequest | 注册账号 | POST /api/auth/register |
| LoginRequest | 用户名密码登录 | POST /api/auth/login |
| ResetPasswordByEmailRequest | 邮箱验证码重置密码 | POST /api/auth/password/reset/email |
| ResetPasswordBySmsRequest | 短信验证码重置密码 | POST /api/auth/password/reset/sms |
| CreateGoodsRequest | 发布商品 | POST /api/goods |
| ApproveGoodsRequest | 审核商品 | POST /api/goods/{id}/approve |
| CreatePostRequest | 发布帖子 | POST /api/posts |
| UpdatePostRequest | 编辑帖子 | PUT /api/posts/{id} |
| CreateReplyRequest | 回复帖子 | POST /api/replies |
| SendMessageRequest | 站内私信 | POST /api/messages/send |
| CreateOrderRequest | 创建订单 | POST /api/orders |
| PayOrderRequest | 旧版支付接口 | POST /api/payment/create |
| CreateSubscriptionRequest | 关键词订阅 | POST /api/subscribe |
| CreateTagRequest | 新增标签 | POST /api/admin/tags |
| UpdateProfileRequest | 修改个人资料 | PUT /api/users/profile |
| UpdatePasswordRequest | 修改密码 | PUT /api/users/password |
| BanUserRequest | 封禁用户 | POST /api/admin/users/ban |
| CreateRoleRequest | 创建角色 | POST /api/admin/roles |
| MergeTagRequest | 合并标签 | POST /api/admin/tags/merge |
| CompletePrivacyRequest | 管理员标记隐私请求完成 | POST /api/admin/privacy/requests/{id}/complete |

## 5. 风险与优化建议
- `FeatureFlagController`、`TaskController`、`ExportController` 等仅依赖全局登录校验，建议补充 `@PreAuthorize` 指定管理员权限。
- 支付回调已放行，请确保终端部署时配置白名单 IP 并进行签名验证。
- 公开接口需关注频率控制（AuthController 已通过 `@RateLimit`，推荐在网关层同步配置）。
- 角色/权限枚举集中在 `PermissionCodes`，新增接口时请统一引用避免魔法字符串。

## 6. 附录：控制器明细
### AdminController
- 基础路径：`/api/admin`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/admin/users/ban` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)` |
| POST | `/api/admin/users/{userId}/unban` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)` |
| POST | `/api/admin/users/auto-unban` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)` |
| GET | `/api/admin/statistics/overview` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)` |
| GET | `/api/admin/statistics/users` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)` |
| GET | `/api/admin/statistics/goods` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)` |
| GET | `/api/admin/statistics/orders` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)` |
| GET | `/api/admin/statistics/today` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)` |
| GET | `/api/admin/statistics/categories` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)` |
| GET | `/api/admin/statistics/trend` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)` |
| GET | `/api/admin/statistics/top-goods` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)` |
| GET | `/api/admin/statistics/top-users` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)` |
| GET | `/api/admin/statistics/revenue` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)` |

### AuditLogController
- 基础路径：`/api/audit-logs`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/audit-logs` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_AUDIT_VIEW)` |

### AuthController
- 基础路径：`/api/auth`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/auth/register` | `HttpSecurity 规则` |
| POST | `/api/auth/register/code` | `HttpSecurity 规则` |
| POST | `/api/auth/register/by-email` | `HttpSecurity 规则` |
| POST | `/api/auth/password/reset/code/email` | `HttpSecurity 规则` |
| POST | `/api/auth/password/reset/email` | `HttpSecurity 规则` |
| POST | `/api/auth/password/reset/code/sms` | `HttpSecurity 规则` |
| POST | `/api/auth/password/reset/sms` | `HttpSecurity 规则` |
| POST | `/api/auth/login` | `HttpSecurity 规则` |
| POST | `/api/auth/logout` | `HttpSecurity 规则` |
| POST | `/api/auth/refresh` | `HttpSecurity 规则` |

### BlacklistController
- 基础路径：`/api/blacklist`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/blacklist/block/{blockedUserId}` | `hasRole('USER')` |
| DELETE | `/api/blacklist/unblock/{blockedUserId}` | `hasRole('USER')` |
| GET | `/api/blacklist/list` | `hasRole('USER')` |
| GET | `/api/blacklist/check/{blockedUserId}` | `hasRole('USER')` |

### CampusController
- 基础路径：`/api/admin/campuses`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/admin/campuses` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_MANAGE)` |
| POST | `/api/admin/campuses` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_MANAGE)` |
| PUT | `/api/admin/campuses/{id}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_MANAGE)` |
| DELETE | `/api/admin/campuses/{id}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_MANAGE)` |
| POST | `/api/admin/campuses/migrate-users/validate` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_MANAGE)` |
| POST | `/api/admin/campuses/migrate-users` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_MANAGE)` |

### CategoryController
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/categories/tree` | `HttpSecurity 规则` |
| POST | `/api/admin/categories` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_MANAGE)` |
| PUT | `/api/admin/categories/{id}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_MANAGE)` |
| DELETE | `/api/admin/categories/{id}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_MANAGE)` |

### ComplianceAdminController
- 基础路径：`/api/admin/compliance`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/admin/compliance/whitelist` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_COMPLIANCE_REVIEW)` |
| DELETE | `/api/admin/compliance/whitelist/{id}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_COMPLIANCE_REVIEW)` |
| GET | `/api/admin/compliance/audit` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_COMPLIANCE_REVIEW)` |

### ExportController
- 基础路径：`/api/exports`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/exports` | `HttpSecurity 规则` |
| GET | `/api/exports` | `HttpSecurity 规则` |
| POST | `/api/exports/{id}/cancel` | `HttpSecurity 规则` |
| GET | `/api/exports/download/{token}` | `HttpSecurity 规则` |

### FavoriteController
- 基础路径：`/api/favorites`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/favorites/{goodsId}` | `hasRole('STUDENT')` |
| DELETE | `/api/favorites/{goodsId}` | `hasRole('STUDENT')` |
| GET | `/api/favorites` | `hasRole('STUDENT')` |
| GET | `/api/favorites/{goodsId}/check` | `HttpSecurity 规则` |

### FeatureFlagController
- 基础路径：`/api/feature-flags`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/feature-flags` | `HttpSecurity 规则` |
| POST | `/api/feature-flags` | `HttpSecurity 规则` |
| POST | `/api/feature-flags/refresh` | `HttpSecurity 规则` |
| DELETE | `/api/feature-flags/{key}` | `HttpSecurity 规则` |

### FileController
- 基础路径：`/api/files`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/files/upload` | `isAuthenticated()` |
| POST | `/api/files/upload-with-thumbnail` | `isAuthenticated()` |
| DELETE | `/api/files` | `isAuthenticated()` |

### FollowController
- 基础路径：`/api`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/follow/{sellerId}` | `hasRole('STUDENT')` |
| DELETE | `/api/follow/{sellerId}` | `hasRole('STUDENT')` |
| GET | `/api/following` | `hasRole('STUDENT')` |

### GoodsController
- 基础路径：`/api/goods`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/goods` | `hasRole('STUDENT')` |
| GET | `/api/goods` | `HttpSecurity 规则` |
| GET | `/api/goods/{id}` | `HttpSecurity 规则` |
| GET | `/api/goods/pending` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_GOODS_APPROVE)` |
| POST | `/api/goods/{id}/approve` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_GOODS_APPROVE)` |

### MessageController
- 基础路径：`/api/messages`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/messages/send` | `hasRole('USER')` |
| GET | `/api/messages/unread-count` | `hasRole('USER')` |
| GET | `/api/messages/conversations` | `hasRole('USER')` |
| GET | `/api/messages/conversations/{conversationId}/messages` | `hasRole('USER')` |
| POST | `/api/messages/conversations/{conversationId}/mark-read` | `hasRole('USER')` |
| POST | `/api/messages/messages/{messageId}/recall` | `hasRole('USER')` |

### NotificationController
- 基础路径：`/api/notifications`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/notifications` | `isAuthenticated()` |
| GET | `/api/notifications/unread-count` | `isAuthenticated()` |
| PUT | `/api/notifications/mark-read` | `isAuthenticated()` |
| PUT | `/api/notifications/mark-all-read` | `isAuthenticated()` |
| DELETE | `/api/notifications` | `isAuthenticated()` |

### NotificationPreferenceController
- 基础路径：`/api/notifications/preferences`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/notifications/preferences/channel/{channel}/enabled/{enabled}` | `isAuthenticated()` |
| POST | `/api/notifications/preferences/channel/{channel}/quiet-hours` | `isAuthenticated()` |
| POST | `/api/notifications/preferences/unsubscribe/{channel}/{templateCode}` | `isAuthenticated()` |
| DELETE | `/api/notifications/preferences/unsubscribe/{channel}/{templateCode}` | `isAuthenticated()` |
| GET | `/api/notifications/preferences/status` | `isAuthenticated()` |

### NotificationTemplateAdminController
- 基础路径：`/api/admin/notification-templates`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/admin/notification-templates` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)` |
| POST | `/api/admin/notification-templates` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)` |
| DELETE | `/api/admin/notification-templates/{id}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)` |
| POST | `/api/admin/notification-templates/render/{code}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)` |

### OrderController
- 基础路径：`/api/orders`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/orders` | `hasAnyRole('STUDENT', 'TEACHER')` |
| GET | `/api/orders/buyer` | `hasAnyRole('STUDENT', 'TEACHER')` |
| GET | `/api/orders/seller` | `hasAnyRole('STUDENT', 'TEACHER')` |
| GET | `/api/orders/{orderNo}` | `hasAnyRole('STUDENT', 'TEACHER')` |
| POST | `/api/orders/{orderNo}/cancel` | `hasAnyRole('STUDENT', 'TEACHER')` |

### PaymentController
- 基础路径：`/api/payment`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/payment/create` | `hasAnyRole('STUDENT', 'TEACHER')` |
| POST | `/api/payment/wechat/notify` | `HttpSecurity 规则` |
| GET | `/api/payment/status/{orderNo}` | `hasAnyRole('STUDENT', 'TEACHER')` |
| POST | `/api/payment/alipay/refund/notify` | `HttpSecurity 规则` |

### PaymentMockController
- 基础路径：`/api/payment/mock`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/payment/mock/wechat` | `hasRole('SUPER_ADMIN')` |

### PerfSeedController
- 基础路径：`/api/perf/orders/timeout`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/perf/orders/timeout/seed` | `hasRole('SUPER_ADMIN')` |
| DELETE | `/api/perf/orders/timeout` | `hasRole('SUPER_ADMIN')` |

### PostController
- 基础路径：`/api/posts`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/posts` | `hasRole('STUDENT')` |
| PUT | `/api/posts/{id}` | `hasRole('STUDENT')` |
| GET | `/api/posts` | `HttpSecurity 规则` |
| GET | `/api/posts/search` | `HttpSecurity 规则` |
| GET | `/api/posts/user/{authorId}` | `HttpSecurity 规则` |
| GET | `/api/posts/{id}` | `HttpSecurity 规则` |
| POST | `/api/posts/{id}/approve` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_POST_APPROVE)` |
| DELETE | `/api/posts/{id}` | `hasRole('STUDENT')` |

### PrivacyController
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/privacy` | `hasRole('STUDENT')` |
| GET | `/api/privacy` | `hasRole('STUDENT')` |
| GET | `/api/admin/privacy/requests` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_COMPLIANCE_REVIEW)` |
| POST | `/api/admin/privacy/requests/{id}/complete` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_COMPLIANCE_REVIEW)` |

### RateLimitAdminController
- 基础路径：`/api/admin/rate-limit`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/admin/rate-limit/rules` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)` |
| POST | `/api/admin/rate-limit/enabled/{enabled}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)` |
| POST | `/api/admin/rate-limit/whitelist/users/{userId}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)` |
| DELETE | `/api/admin/rate-limit/whitelist/users/{userId}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)` |
| POST | `/api/admin/rate-limit/whitelist/ips/{ip}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)` |
| DELETE | `/api/admin/rate-limit/whitelist/ips/{ip}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)` |
| POST | `/api/admin/rate-limit/blacklist/ips/{ip}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)` |
| DELETE | `/api/admin/rate-limit/blacklist/ips/{ip}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)` |

### RecommendController
- 基础路径：`/api/recommend`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/recommend/hot` | `HttpSecurity 规则` |
| GET | `/api/recommend/personal` | `hasAnyRole('STUDENT','TEACHER','ADMIN','SUPER_ADMIN')` |
| POST | `/api/recommend/admin/hot/refresh` | `hasAnyRole('ADMIN','SUPER_ADMIN')` |

### RefundController
- 基础路径：`/api`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/orders/{orderNo}/refunds` | `hasAnyRole('STUDENT','TEACHER')` |
| PUT | `/api/admin/refunds/{refundNo}/approve` | `hasAnyRole('ADMIN','SUPER_ADMIN')` |
| PUT | `/api/admin/refunds/{refundNo}/reject` | `hasAnyRole('ADMIN','SUPER_ADMIN')` |
| GET | `/api/admin/refunds/{refundNo}` | `hasAnyRole('ADMIN','SUPER_ADMIN')` |

### ReplyController
- 基础路径：`/api/replies`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/replies` | `hasRole('STUDENT')` |
| GET | `/api/replies/post/{postId}` | `HttpSecurity 规则` |
| GET | `/api/replies/{parentId}/sub` | `HttpSecurity 规则` |
| DELETE | `/api/replies/{id}` | `hasRole('STUDENT')` |

### ReportController
- 基础路径：`/api/reports`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/reports` | `hasRole('STUDENT')` |
| GET | `/api/reports/pending` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_REPORT_HANDLE)` |
| GET | `/api/reports/my` | `hasRole('STUDENT')` |
| POST | `/api/reports/{id}/handle` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_REPORT_HANDLE)` |

### RoleAdminController
- 基础路径：`/api/admin`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/admin/roles` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_ROLE_ASSIGN)` |
| GET | `/api/admin/roles/{roleId}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_ROLE_ASSIGN)` |
| POST | `/api/admin/roles` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_ROLE_ASSIGN)` |
| PUT | `/api/admin/roles/{roleId}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_ROLE_ASSIGN)` |
| DELETE | `/api/admin/roles/{roleId}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_ROLE_ASSIGN)` |
| PUT | `/api/admin/users/{userId}/roles` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_ROLE_ASSIGN)` |

### SearchController
- 基础路径：`/api/search`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/search` | `HttpSecurity 规则` |

### SoftDeleteAdminController
- 基础路径：`/api/admin/soft-delete`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/admin/soft-delete/targets` | `hasRole('ADMIN')` |
| POST | `/api/admin/soft-delete/{entity}/{id}/restore` | `hasRole('ADMIN')` |
| DELETE | `/api/admin/soft-delete/{entity}/{id}/purge` | `hasRole('ADMIN')` |

### SubscriptionController
- 基础路径：`/api`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| POST | `/api/subscribe` | `hasRole('STUDENT')` |
| DELETE | `/api/subscribe/{id}` | `hasRole('STUDENT')` |
| GET | `/api/subscribe` | `hasRole('STUDENT')` |

### TagController
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/tags` | `HttpSecurity 规则` |
| POST | `/api/admin/tags` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_MANAGE)` |
| PUT | `/api/admin/tags/{id}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_MANAGE)` |
| DELETE | `/api/admin/tags/{id}` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_MANAGE)` |
| POST | `/api/admin/tags/merge` | `hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_MANAGE)` |

### TaskController
- 基础路径：`/api/tasks`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/tasks` | `HttpSecurity 规则` |
| POST | `/api/tasks/{name}/trigger` | `HttpSecurity 规则` |
| POST | `/api/tasks/{name}/pause` | `HttpSecurity 规则` |
| POST | `/api/tasks/{name}/resume` | `HttpSecurity 规则` |

### UserController
- 基础路径：`/api/users`
| HTTP | 路径 | 权限校验 |
| --- | --- | --- |
| GET | `/api/users/profile` | `isAuthenticated()` |
| GET | `/api/users/{userId}` | `HttpSecurity 规则` |
| PUT | `/api/users/profile` | `isAuthenticated()` |
| PUT | `/api/users/password` | `isAuthenticated()` |

