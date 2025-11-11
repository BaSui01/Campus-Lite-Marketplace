/**
 * 通知模板管理页面
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React, { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Modal,
  Form,
  Input,
  Select,
  Space,
  message,
  Typography,
  Tag,
  Tabs,
  Divider,
  App,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  MailOutlined,
  WechatOutlined,
  MobileOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notificationTemplateService } from '@campus/shared';
import type { NotificationTemplate, RenderedTemplate } from '@campus/shared';

const { Title, Paragraph } = Typography;
const { TextArea } = Input;
const { TabPane } = Tabs;
const { Option } = Select;

const channelIcons: Record<string, React.ReactNode> = {
  EMAIL: <MailOutlined />,
  WECHAT: <WechatOutlined />,
  SMS: <MobileOutlined />,
};

const channelColors: Record<string, string> = {
  EMAIL: 'blue',
  WECHAT: 'green',
  SMS: 'orange',
};

const Notifications: React.FC = () => {
  const queryClient = useQueryClient();
  const { modal } = App.useApp();
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [previewModalVisible, setPreviewModalVisible] = useState(false);
  const [previewData, setPreviewData] = useState<RenderedTemplate | null>(null);
  const [selectedTemplate, setSelectedTemplate] = useState<NotificationTemplate | null>(null);
  const [createForm] = Form.useForm();
  const [editForm] = Form.useForm();
  const [previewForm] = Form.useForm();

  // ===== 查询模板列表 =====
  const { data: templates, isLoading } = useQuery({
    queryKey: ['notification-templates'],
    queryFn: () => notificationTemplateService.list(),
  });

  // ===== 创建模板 Mutation =====
  const createTemplateMutation = useMutation({
    mutationFn: (template: NotificationTemplate) => {
      return notificationTemplateService.save(template);
    },
    onSuccess: () => {
      message.success('模板创建成功！🎉');
      queryClient.invalidateQueries({ queryKey: ['notification-templates'] });
      setCreateModalVisible(false);
      createForm.resetFields();
    },
    onError: (error: any) => {
      message.error(`创建失败：${error.message} 😰`);
    },
  });

  // ===== 更新模板 Mutation =====
  const updateTemplateMutation = useMutation({
    mutationFn: (template: NotificationTemplate) => {
      return notificationTemplateService.save(template);
    },
    onSuccess: () => {
      message.success('模板更新成功！🎉');
      queryClient.invalidateQueries({ queryKey: ['notification-templates'] });
      setEditModalVisible(false);
      setSelectedTemplate(null);
      editForm.resetFields();
    },
    onError: (error: any) => {
      message.error(`更新失败：${error.message} 😰`);
    },
  });

  // ===== 删除模板 Mutation =====
  const deleteTemplateMutation = useMutation({
    mutationFn: (templateId: number) => {
      return notificationTemplateService.delete(templateId);
    },
    onSuccess: () => {
      message.success('模板删除成功！🎉');
      queryClient.invalidateQueries({ queryKey: ['notification-templates'] });
    },
    onError: (error: any) => {
      message.error(`删除失败：${error.message} 😰`);
    },
  });

  // ===== 预览模板 Mutation =====
  const previewMutation = useMutation({
    mutationFn: (data: { code: string; params: Record<string, any> }) => {
      return notificationTemplateService.render(data.code, data.params);
    },
    onSuccess: (data) => {
      setPreviewData(data);
    },
    onError: (error: any) => {
      message.error(`预览失败：${error.message} 😰`);
    },
  });

  // ===== 表格列定义 =====
  const columns: ColumnsType<NotificationTemplate> = [
    {
      title: '模板代码',
      dataIndex: 'code',
      key: 'code',
      render: (code) => <Tag color="blue">{code}</Tag>,
    },
    {
      title: '模板名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '语言',
      dataIndex: 'locale',
      key: 'locale',
      render: (locale) => locale ? <Tag>{locale}</Tag> : <Tag color="default">默认</Tag>,
    },
    {
      title: '发送渠道',
      dataIndex: 'channels',
      key: 'channels',
      render: (channels: unknown) => {
        const list: string[] = Array.isArray(channels)
          ? (channels as string[])
          : (typeof channels === 'string' && (channels as string))
          ? [(channels as string)]
          : [];
        return (
          <Space>
            {list.length > 0 ? (
              list.map((channel) => (
                <Tag
                  key={channel}
                  color={channelColors[channel] || 'default'}
                  icon={channelIcons[channel]}
                >
                  {channel}
                </Tag>
              ))
            ) : (
              <span style={{ color: '#999' }}>未设置</span>
            )}
          </Space>
        );
      },
    },
    {
      title: '标题模板',
      dataIndex: 'titleTemplate',
      key: 'titleTemplate',
      render: (title) => (
        <div style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {title}
        </div>
      ),
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right',
      width: 180,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handlePreview(record)}
          >
            预览
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            danger
            size="small"
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ];

  // ===== 打开创建弹窗 =====
  const handleCreate = () => {
    setCreateModalVisible(true);
  };

  // ===== 打开编辑弹窗 =====
  const handleEdit = (template: NotificationTemplate) => {
    setSelectedTemplate(template);
    editForm.setFieldsValue(template);
    setEditModalVisible(true);
  };

  // ===== 删除模板 =====
  const handleDelete = (template: NotificationTemplate) => {
    modal.confirm({
      title: '确认删除模板？',
      content: `确定要删除模板 "${template.name}" 吗？此操作不可撤销。`,
      onOk: () => deleteTemplateMutation.mutate(template.id),
    });
  };

  // ===== 预览模板 =====
  const handlePreview = (template: NotificationTemplate) => {
    setSelectedTemplate(template);
    setPreviewModalVisible(true);
  };

  // ===== 确认创建 =====
  const handleCreateSubmit = () => {
    createForm.validateFields().then((values) => {
      createTemplateMutation.mutate(values as NotificationTemplate);
    });
  };

  // ===== 确认更新 =====
  const handleEditSubmit = () => {
    if (!selectedTemplate) return;
    
    editForm.validateFields().then((values) => {
      const template = { ...selectedTemplate, ...values };
      updateTemplateMutation.mutate(template);
    });
  };

  // ===== 预览模板 =====
  const handlePreviewSubmit = () => {
    if (!selectedTemplate) return;

    previewForm.validateFields().then((values) => {
      previewMutation.mutate({
        code: selectedTemplate.code,
        params: values.params,
      });
    });
  };

  return (
    <div className="notifications" style={{ padding: '24px' }}>
      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Title level={2}>📧 通知模板管理</Title>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              创建模板
            </Button>
          </div>

          <Table
            columns={columns}
            dataSource={templates || []}
            rowKey="id"
            loading={isLoading}
            pagination={{
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total) => `共 ${total} 个模板`,
            }}
            scroll={{ x: 1200 }}
          />
        </Space>
      </Card>

      {/* 创建模板弹窗 */}
      <Modal
        title="创建通知模板"
        open={createModalVisible}
        onOk={handleCreateSubmit}
        onCancel={() => {
          setCreateModalVisible(false);
          createForm.resetFields();
        }}
        confirmLoading={createTemplateMutation.isPending}
        width={800}
      >
        <Form form={createForm} layout="vertical">
          <Form.Item
            name="code"
            label="模板代码"
            rules={[
              { required: true, message: '请输入模板代码！' },
              { pattern: /^[A-Z_0-9]+$/, message: '模板代码只能包含大写字母、下划线和数字！' },
            ]}
          >
            <Input placeholder="例如：USER_WELCOME" />
          </Form.Item>
          <Form.Item
            name="name"
            label="模板名称"
            rules={[{ required: true, message: '请输入模板名称！' }]}
          >
            <Input placeholder="请输入模板名称" />
          </Form.Item>
          <Form.Item name="locale" label="语言">
            <Select placeholder="选择语言类型" allowClear>
              <Option value="zh_CN">简体中文</Option>
              <Option value="en_US">English</Option>
            </Select>
          </Form.Item>
          <Form.Item name="channels" label="发送渠道">
            <Select mode="multiple" placeholder="选择发送渠道">
              <Option value="EMAIL">邮件</Option>
              <Option value="WECHAT">微信</Option>
              <Option value="SMS">短信</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="titleTemplate"
            label="标题模板"
            rules={[{ required: true, message: '请输入标题模板！' }]}
          >
            <Input placeholder="使用 {变量名} 作为占位符，例如：欢迎 {username}" />
          </Form.Item>
          <Form.Item
            name="contentTemplate"
            label="内容模板"
            rules={[{ required: true, message: '请输入内容模板！' }]}
          >
            <TextArea rows={6} placeholder="使用 {变量名} 作为占位符" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 编辑模板弹窗 */}
      <Modal
        title="编辑通知模板"
        open={editModalVisible}
        onOk={handleEditSubmit}
        onCancel={() => {
          setEditModalVisible(false);
          setSelectedTemplate(null);
          editForm.resetFields();
        }}
        confirmLoading={updateTemplateMutation.isPending}
        width={800}
      >
        <Form form={editForm} layout="vertical">
          <Form.Item
            name="code"
            label="模板代码"
            rules={[{ required: true, message: '请输入模板代码！' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="name"
            label="模板名称"
            rules={[{ required: true, message: '请输入模板名称！' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item name="locale" label="语言">
            <Select allowClear>
              <Option value="zh_CN">简体中文</Option>
              <Option value="en_US">English</Option>
            </Select>
          </Form.Item>
          <Form.Item name="channels" label="发送渠道">
            <Select mode="multiple">
              <Option value="EMAIL">邮件</Option>
              <Option value="WECHAT">微信</Option>
              <Option value="SMS">短信</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="titleTemplate"
            label="标题模板"
            rules={[{ required: true, message: '请输入标题模板！' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="contentTemplate"
            label="内容模板"
            rules={[{ required: true, message: '请输入内容模板！' }]}
          >
            <TextArea rows={6} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 预览模板弹窗 */}
      <Modal
        title={`预览模板：${selectedTemplate?.name}`}
        open={previewModalVisible}
        onCancel={() => {
          setPreviewModalVisible(false);
          setSelectedTemplate(null);
          setPreviewData(null);
          previewForm.resetFields();
        }}
        footer={false}
        width={800}
      >
        {selectedTemplate && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <div>
              <Title level={4}>模板信息</Title>
              <div>
                <strong>代码：</strong> {selectedTemplate.code}
              </div>
              <div>
                <strong>语言：</strong> {selectedTemplate.locale || '默认'}
              </div>
              <div>
                <strong>渠道：</strong>
                <Space style={{ marginLeft: 8 }}>
                  {(Array.isArray(selectedTemplate.channels)
                    ? selectedTemplate.channels
                    : (typeof selectedTemplate.channels === 'string'
                        ? [selectedTemplate.channels]
                        : [])
                  ).map((channel) => (
                    <Tag key={channel} color={channelColors[channel] || 'default'}>
                      {channel}
                    </Tag>
                  ))}
                </Space>
              </div>
            </div>

            <Divider />

            <div>
              <Title level={4}>测试参数</Title>
              <Form form={previewForm} layout="vertical">
                <Form.Item
                  name="params"
                  label="参数（JSON格式）"
                  rules={[{ validator: (_, value) => {
                    if (!value) {
                      return Promise.reject('请输入测试参数！');
                    }
                    try {
                      JSON.parse(value);
                      return Promise.resolve();
                    } catch {
                      return Promise.reject('参数格式不正确，请输入有效的JSON！');
                    }
                  } }]}
                >
                  <TextArea
                    rows={4}
                    placeholder='例如：{"username": "张三", "sitename": "校园集市"}'
                    defaultValue={`{"username": "张三", "sitename": "校园集市"}`}
                  />
                </Form.Item>
                <Button type="primary" onClick={handlePreviewSubmit} loading={previewMutation.isPending}>
                  预览效果
                </Button>
              </Form>
            </div>

            {previewData && (
              <>
                <Divider />

                <div>
                  <Title level={4}>预览效果</Title>
                  <Tabs defaultActiveKey="1">
                    <TabPane tab="预览结果" key="1">
                      <div>
                        <Title level={5}>标题</Title>
                        <Paragraph>{previewData.title}</Paragraph>
                        <Title level={5}>内容</Title>
                        <Paragraph>
                          <pre style={{ whiteSpace: 'pre-wrap', background: '#f5f5f5', padding: '12px', borderRadius: '4px' }}>
                            {previewData.content}
                          </pre>
                        </Paragraph>
                      </div>
                    </TabPane>
                    <TabPane tab="HTML预览" key="2">
                      <div>
                        <div dangerouslySetInnerHTML={{ __html: previewData.content }} />
                      </div>
                    </TabPane>
                  </Tabs>
                </div>
              </>
            )}
          </Space>
        )}
      </Modal>
    </div>
  );
};

export default Notifications;
