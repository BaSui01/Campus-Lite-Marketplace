/**
 * 通知模板管理服务
 */

import { http } from '../utils/apiClient';
import type { ApiResponse } from '../types';

export interface NotificationTemplate {
  id: number;
  code: string;
  name: string;
  titleTemplate: string;
  contentTemplate: string;
  locale?: string;
  channels?: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface RenderedTemplate {
  title: string;
  content: string;
  channels: string[];
}

export class NotificationTemplateService {
  async list(): Promise<NotificationTemplate[]> {
    const res = await http.get<ApiResponse<NotificationTemplate[]>>('/api/admin/notification-templates');
    return res.data;
  }

  async save(template: NotificationTemplate): Promise<NotificationTemplate> {
    const res = await http.post<ApiResponse<NotificationTemplate>>('/api/admin/notification-templates', template);
    return res.data;
  }

  async delete(id: number): Promise<void> {
    await http.delete<ApiResponse<void>>(`/api/admin/notification-templates/${id}`);
  }

  async render(code: string, params?: Record<string, unknown>): Promise<RenderedTemplate> {
    const res = await http.post<ApiResponse<RenderedTemplate>>(
      `/api/admin/notification-templates/render/${code}`,
      params ?? {}
    );
    return res.data;
  }
}

export const notificationTemplateService = new NotificationTemplateService();
export default notificationTemplateService;
