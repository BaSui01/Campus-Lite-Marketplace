/**
 * ✏️ 模板编辑 Modal 组件 - BaSui 搞笑专业版 😎
 *
 * 功能：
 * - 创建/编辑通知模板
 * - 支持模板变量插入
 * - 多渠道选择
 *
 * @author BaSui
 * @date 2025-11-07
 */

import React, { useState, useEffect } from 'react';
import { Modal, Form, Input, Select, Button, Space, Tag, message } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { NotificationTemplate } from '@campus/shared/api';

const { TextArea } = Input;
const { Option } = Select;

interface TemplateEditModalProps {
  visible: boolean;
  template: NotificationTemplate | null; // null 表示新建
  onSave: (template: NotificationTemplate) => void;
  onCancel: () => void;
}

/**
 * 常用模板变量
 */
const TEMPLATE_VARIABLES = [
  { label: '用户名', value: '{{username}}' },
  { label: '订单号', value: '{{orderNo}}' },
  { label: '金额', value: '{{amount}}' },
  { label: '商品名称', value: '{{goodsTitle}}' },
  { label: '时间', value: '{{time}}' },
  { label: '日期', value: '{{date}}' },
];

/**
 * 通知渠道
 */
const NOTIFICATION_CHANNELS = ['SYSTEM', 'EMAIL', 'SMS', 'WECHAT'];

/**
 * 模板编辑 Modal 组件
 */
const TemplateEditModal: React.FC<TemplateEditModalProps> = ({
  visible,
  template,
  onSave,
  onCancel,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  // 当模板变化时，更新表单
  useEffect(() => {
    if (visible && template) {
      form.setFieldsValue({
        code: template.code,
        titleTemplate: template.titleTemplate,
        contentTemplate: template.contentTemplate,
        locale: template.locale || 'zh_CN',
        channels: template.channels || ['SYSTEM'],
      });
    } else if (visible && !template) {
      form.resetFields();
      form.setFieldsValue({
        locale: 'zh_CN',
        channels: ['SYSTEM'],
      });
    }
  }, [visible, template, form]);

  /**
   * 插入模板变量
   */
  const insertVariable = (field: string, variable: string) => {
    const currentValue = form.getFieldValue(field) || '';
    form.setFieldsValue({
      [field]: currentValue + variable,
    });
  };

  /**
   * 提交表单
   */
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const templateData: NotificationTemplate = {
        ...template,
        ...values,
      };

      onSave(templateData);
    } catch (error) {
      console.error('❌ 表单验证失败:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={template ? '✏️ 编辑模板' : '➕ 新建模板'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      confirmLoading={loading}
      width={800}
      okText="保存"
      cancelText="取消"
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="code"
          label="模板编码"
          rules={[
            { required: true, message: '请输入模板编码' },
            { pattern: /^[A-Z_]+$/, message: '只能使用大写字母和下划线' },
          ]}
        >
          <Input
            placeholder="例如：ORDER_PAID"
            disabled={!!template} // 编辑时不可修改编码
          />
        </Form.Item>

        <Form.Item
          name="titleTemplate"
          label={
            <Space>
              <span>标题模板</span>
              <Space size="small">
                {TEMPLATE_VARIABLES.map((v) => (
                  <Tag
                    key={v.value}
                    color="blue"
                    style={{ cursor: 'pointer' }}
                    onClick={() => insertVariable('titleTemplate', v.value)}
                  >
                    + {v.label}
                  </Tag>
                ))}
              </Space>
            </Space>
          }
          rules={[{ required: true, message: '请输入标题模板' }]}
        >
          <Input placeholder="例如：订单 {{orderNo}} 已支付" />
        </Form.Item>

        <Form.Item
          name="contentTemplate"
          label={
            <Space>
              <span>内容模板</span>
              <Space size="small">
                {TEMPLATE_VARIABLES.map((v) => (
                  <Tag
                    key={v.value}
                    color="green"
                    style={{ cursor: 'pointer' }}
                    onClick={() => insertVariable('contentTemplate', v.value)}
                  >
                    + {v.label}
                  </Tag>
                ))}
              </Space>
            </Space>
          }
          rules={[{ required: true, message: '请输入内容模板' }]}
        >
          <TextArea
            rows={6}
            placeholder="例如：您的订单已支付成功，订单号：{{orderNo}}，金额：¥{{amount}}，感谢您的支持！"
          />
        </Form.Item>

        <Form.Item
          name="locale"
          label="语言"
          rules={[{ required: true, message: '请选择语言' }]}
        >
          <Select placeholder="选择语言">
            <Option value="zh_CN">简体中文</Option>
            <Option value="en_US">英文</Option>
          </Select>
        </Form.Item>

        <Form.Item
          name="channels"
          label="通知渠道"
          rules={[{ required: true, message: '请选择至少一个通知渠道' }]}
        >
          <Select mode="multiple" placeholder="选择通知渠道">
            {NOTIFICATION_CHANNELS.map((ch) => (
              <Option key={ch} value={ch}>
                {ch}
              </Option>
            ))}
          </Select>
        </Form.Item>
      </Form>

      <div style={{ marginTop: 16, padding: 12, background: '#f6ffed', borderRadius: 4 }}>
        <p style={{ margin: 0, color: '#52c41a', fontWeight: 600 }}>💡 使用提示：</p>
        <ul style={{ margin: '8px 0 0', paddingLeft: 20, color: '#666' }}>
          <li>模板编码必须唯一，建议使用大写字母和下划线</li>
          <li>点击上方标签可快速插入常用变量</li>
          <li>使用 {'{{'} 和 {'}}'}  包裹变量名，例如：{'{'}{'{'} username {'}'}{'}'}</li>
          <li>可以选择多个通知渠道，系统会根据用户偏好自动选择</li>
        </ul>
      </div>
    </Modal>
  );
};

export default TemplateEditModal;
