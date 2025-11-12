/**
 * ✅ 已重构：使用 OpenAPI 生成的 DefaultApi
 *
 * 通知模板管理服务 - BaSui 搞笑专业版 😎
 *
 * 功能：
 * - 📋 模板列表查询
 * - ✏️ 创建/更新模板
 * - 🗑️ 删除模板
 * - 👁️ 模板渲染预览
 *
 * @author BaSui
 * @date 2025-11-07
 */

import { getApi } from '../utils/apiClient';
import type { NotificationTemplate } from '../api';

/**
 * 渲染后的模板（用于预览）
 */
export interface RenderedTemplate {
  title: string;       // 渲染后的标题
  content: string;     // 渲染后的内容
  channels: string[];  // 通知渠道
}

/**
 * 通知模板管理服务类
 */
export class NotificationTemplateService {
  /**
   * 📋 获取所有通知模板列表
   *
   * GET /api/admin/notification-templates
   *
   * @returns Promise<NotificationTemplate[]>
   *
   * @example
   * const templates = await notificationTemplateService.list();
   * templates.forEach(tpl => {
   *   console.log(`模板: ${tpl.code} - ${tpl.titleTemplate}`);
   * });
   */
  async list(): Promise<NotificationTemplate[]> {
    const api = getApi();
    const response = await api.list2(); // list2 是 OpenAPI 生成的方法名（notification-templates）

    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取通知模板列表失败');
    }

    return response.data.data as NotificationTemplate[];
  }

  /**
   * ✏️ 创建或更新通知模板
   *
   * POST /api/admin/notification-templates
   *
   * @param template - 模板数据
   * @returns Promise<NotificationTemplate>
   *
   * @example
   * const newTemplate = await notificationTemplateService.save({
   *   code: 'ORDER_PAID',
   *   name: '订单支付成功',
   *   titleTemplate: '订单 {{orderNo}} 已支付',
   *   contentTemplate: '您的订单已支付成功，金额: ¥{{amount}}',
   *   locale: 'zh_CN',
   *   channels: ['SYSTEM', 'EMAIL']
   * });
   */
  async save(template: NotificationTemplate): Promise<NotificationTemplate> {
    const api = getApi();
    const response = await api.save({ notificationTemplate: template });

    if (response.data.code !== 200) {
      throw new Error(response.data.message || '保存通知模板失败');
    }

    return response.data.data as NotificationTemplate;
  }

  /**
   * 🗑️ 删除通知模板
   *
   * DELETE /api/admin/notification-templates/{id}
   *
   * @param id - 模板ID
   * @returns Promise<void>
   *
   * @example
   * await notificationTemplateService.delete(3001);
   */
  async delete(id: number): Promise<void> {
    const api = getApi();
    const response = await api._delete({ id }); // _delete 是 OpenAPI 生成的方法名

    if (response.data.code !== 200) {
      throw new Error(response.data.message || '删除通知模板失败');
    }
  }

  /**
   * 👁️ 渲染模板预览
   *
   * POST /api/admin/notification-templates/render/{code}
   *
   * @param code - 模板编码
   * @param params - 渲染参数（用于替换模板变量）
   * @returns Promise<RenderedTemplate>
   *
   * @example
   * const rendered = await notificationTemplateService.render('ORDER_PAID', {
   *   orderNo: 'ORD123456',
   *   amount: 99.99
   * });
   * console.log('渲染后的标题:', rendered.title);
   * console.log('渲染后的内容:', rendered.content);
   */
  async render(code: string, _params?: Record<string, unknown>): Promise<RenderedTemplate> {
    const api = getApi();
    const response = await api.render({ code }); // render 方法只接受 code 参数

    if (response.data.code !== 200) {
      throw new Error(response.data.message || '渲染模板失败');
    }

    const data = response.data.data as any;

    // 转换后端响应格式为前端需要的格式
    return {
      title: data.title || '',
      content: data.content || '',
      channels: data.channels || [],
    };
  }
}

export const notificationTemplateService = new NotificationTemplateService();
export default notificationTemplateService;
