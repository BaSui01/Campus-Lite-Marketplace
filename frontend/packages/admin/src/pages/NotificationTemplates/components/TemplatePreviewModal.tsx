/**
 * 👁️ 模板预览 Modal 组件 - BaSui 搞笑专业版 😎
 *
 * 功能：
 * - 预览模板渲染效果
 * - 支持自定义参数
 * - 实时渲染预览
 *
 * @author BaSui
 * @date 2025-11-07
 */

import React, { useState, useEffect } from 'react';
import { Modal, Form, Input, Button, Card, Tag, Spin, message } from 'antd';
import { EyeOutlined, ReloadOutlined } from '@ant-design/icons';
import type { NotificationTemplate } from '@campus/shared/api';
import { notificationTemplateService } from '@campus/shared/services/notificationTemplate';
import type { RenderedTemplate } from '@campus/shared/services/notificationTemplate';

interface TemplatePreviewModalProps {
  visible: boolean;
  template: NotificationTemplate | null;
  onClose: () => void;
}

/**
 * 模板预览 Modal 组件
 */
const TemplatePreviewModal: React.FC<TemplatePreviewModalProps> = ({
  visible,
  template,
  onClose,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [renderedContent, setRenderedContent] = useState<RenderedTemplate | null>(null);

  // 提取模板中的变量
  const extractVariables = (templateText: string): string[] => {
    const regex = /\{\{(\w+)\}\}/g;
    const variables: string[] = [];
    let match;
    while ((match = regex.exec(templateText)) !== null) {
      if (!variables.includes(match[1])) {
        variables.push(match[1]);
      }
    }
    return variables;
  };

  // 当模板变化时，提取变量并初始化表单
  useEffect(() => {
    if (visible && template) {
      const titleVars = extractVariables(template.titleTemplate || '');
      const contentVars = extractVariables(template.contentTemplate || '');
      const allVars = Array.from(new Set([...titleVars, ...contentVars]));

      // 初始化表单字段
      const initialValues: Record<string, string> = {};
      allVars.forEach((varName) => {
        initialValues[varName] = getDefaultValue(varName);
      });
      form.setFieldsValue(initialValues);

      // 自动预览
      handlePreview();
    }
  }, [visible, template]);

  /**
   * 获取变量的默认值
   */
  const getDefaultValue = (varName: string): string => {
    const defaults: Record<string, string> = {
      username: '张三',
      orderNo: 'ORD20251107001',
      amount: '99.99',
      goodsTitle: 'iPhone 15 Pro',
      time: '14:30:00',
      date: '2025-11-07',
    };
    return defaults[varName] || `[${varName}]`;
  };

  /**
   * 预览模板
   */
  const handlePreview = async () => {
    if (!template) return;

    try {
      setLoading(true);
      const params = form.getFieldsValue();

      const rendered = await notificationTemplateService.render(template.code!, params);
      setRenderedContent(rendered);
    } catch (error: any) {
      console.error('❌ 渲染模板失败:', error);
      message.error(error.message || '渲染模板失败');
    } finally {
      setLoading(false);
    }
  };

  if (!template) return null;

  const titleVars = extractVariables(template.titleTemplate || '');
  const contentVars = extractVariables(template.contentTemplate || '');
  const allVars = Array.from(new Set([...titleVars, ...contentVars]));

  return (
    <Modal
      title={
        <>
          <EyeOutlined /> 模板预览 - {template.code}
        </>
      }
      open={visible}
      onCancel={onClose}
      footer={[
        <Button key="close" onClick={onClose}>
          关闭
        </Button>,
        <Button key="refresh" type="primary" icon={<ReloadOutlined />} onClick={handlePreview}>
          重新预览
        </Button>,
      ]}
      width={800}
    >
      <Spin spinning={loading}>
        {/* 参数输入区 */}
        {allVars.length > 0 && (
          <Card title="📝 参数输入" size="small" style={{ marginBottom: 16 }}>
            <Form form={form} layout="vertical">
              {allVars.map((varName) => (
                <Form.Item
                  key={varName}
                  name={varName}
                  label={varName}
                  style={{ marginBottom: 8 }}
                >
                  <Input placeholder={`输入 ${varName} 的值`} />
                </Form.Item>
              ))}
            </Form>
          </Card>
        )}

        {/* 渲染结果区 */}
        {renderedContent && (
          <Card title="✨ 渲染结果" size="small">
            <div style={{ marginBottom: 16 }}>
              <h4 style={{ marginBottom: 8 }}>标题：</h4>
              <div
                style={{
                  padding: 12,
                  background: '#f6f8fa',
                  borderRadius: 4,
                  fontSize: 16,
                  fontWeight: 600,
                }}
              >
                {renderedContent.title}
              </div>
            </div>

            <div style={{ marginBottom: 16 }}>
              <h4 style={{ marginBottom: 8 }}>内容：</h4>
              <div
                style={{
                  padding: 12,
                  background: '#f6f8fa',
                  borderRadius: 4,
                  fontSize: 14,
                  lineHeight: 1.6,
                  whiteSpace: 'pre-wrap',
                }}
              >
                {renderedContent.content}
              </div>
            </div>

            <div>
              <h4 style={{ marginBottom: 8 }}>通知渠道：</h4>
              <div>
                {renderedContent.channels.map((ch) => (
                  <Tag key={ch} color="green">
                    {ch}
                  </Tag>
                ))}
              </div>
            </div>
          </Card>
        )}

        {/* 原始模板区 */}
        <Card title="📄 原始模板" size="small" style={{ marginTop: 16 }}>
          <div style={{ marginBottom: 12 }}>
            <strong>标题模板：</strong>
            <pre style={{ background: '#f6f8fa', padding: 8, borderRadius: 4, marginTop: 8 }}>
              {template.titleTemplate}
            </pre>
          </div>
          <div>
            <strong>内容模板：</strong>
            <pre style={{ background: '#f6f8fa', padding: 8, borderRadius: 4, marginTop: 8 }}>
              {template.contentTemplate}
            </pre>
          </div>
        </Card>
      </Spin>
    </Modal>
  );
};

export default TemplatePreviewModal;
