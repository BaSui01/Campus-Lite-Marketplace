/**
 * 📧 通知模板管理页面 - BaSui 搞笑专业版 😎
 *
 * 功能：
 * - 通知模板列表展示
 * - 创建/编辑模板
 * - 删除模板
 * - 模板预览（支持参数替换）
 *
 * @author BaSui
 * @date 2025-11-07
 */

import React, { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Space,
  Tag,
  message,
  Modal,
  Card,
  Input,
  Popconfirm,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { notificationTemplateService } from '@campus/shared/services/notificationTemplate';
import type { NotificationTemplate } from '@campus/shared/api';
import TemplateEditModal from './components/TemplateEditModal';
import TemplatePreviewModal from './components/TemplatePreviewModal';

const { Search } = Input;

/**
 * 通知模板列表页面
 */
const NotificationTemplateList: React.FC = () => {
  // ========== 状态管理 ==========
  const [loading, setLoading] = useState(false);
  const [templates, setTemplates] = useState<NotificationTemplate[]>([]);
  const [filteredTemplates, setFilteredTemplates] = useState<NotificationTemplate[]>([]);
  const [searchKeyword, setSearchKeyword] = useState('');

  // 编辑 Modal
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [currentTemplate, setCurrentTemplate] = useState<NotificationTemplate | null>(null);

  // 预览 Modal
  const [previewModalVisible, setPreviewModalVisible] = useState(false);
  const [previewTemplate, setPreviewTemplate] = useState<NotificationTemplate | null>(null);

  // ========== 数据加载 ==========

  /**
   * 加载模板列表
   */
  const loadTemplates = async () => {
    setLoading(true);
    try {
      const data = await notificationTemplateService.list();
      setTemplates(data);
      setFilteredTemplates(data);
      message.success('模板列表加载成功！');
    } catch (error: any) {
      console.error('❌ 加载模板列表失败:', error);
      message.error(error.message || '加载模板列表失败');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 搜索过滤
   */
  const handleSearch = (value: string) => {
    setSearchKeyword(value);
    if (!value.trim()) {
      setFilteredTemplates(templates);
      return;
    }

    const keyword = value.toLowerCase();
    const filtered = templates.filter(
      (tpl) =>
        tpl.code?.toLowerCase().includes(keyword) ||
        tpl.titleTemplate?.toLowerCase().includes(keyword) ||
        tpl.contentTemplate?.toLowerCase().includes(keyword)
    );
    setFilteredTemplates(filtered);
  };

  /**
   * 创建模板
   */
  const handleCreate = () => {
    setCurrentTemplate(null);
    setEditModalVisible(true);
  };

  /**
   * 编辑模板
   */
  const handleEdit = (template: NotificationTemplate) => {
    setCurrentTemplate(template);
    setEditModalVisible(true);
  };

  /**
   * 删除模板
   */
  const handleDelete = async (id: number) => {
    try {
      await notificationTemplateService.delete(id);
      message.success('删除成功！');
      loadTemplates();
    } catch (error: any) {
      console.error('❌ 删除模板失败:', error);
      message.error(error.message || '删除模板失败');
    }
  };

  /**
   * 预览模板
   */
  const handlePreview = (template: NotificationTemplate) => {
    setPreviewTemplate(template);
    setPreviewModalVisible(true);
  };

  /**
   * 保存模板（创建或更新）
   */
  const handleSaveTemplate = async (template: NotificationTemplate) => {
    try {
      await notificationTemplateService.save(template);
      message.success(template.id ? '更新成功！' : '创建成功！');
      setEditModalVisible(false);
      loadTemplates();
    } catch (error: any) {
      console.error('❌ 保存模板失败:', error);
      message.error(error.message || '保存模板失败');
    }
  };

  // 初始加载
  useEffect(() => {
    loadTemplates();
  }, []);

  // ========== 表格列定义 ==========

  const columns: ColumnsType<NotificationTemplate> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
      sorter: (a, b) => (a.id || 0) - (b.id || 0),
    },
    {
      title: '模板编码',
      dataIndex: 'code',
      key: 'code',
      width: 200,
      render: (code) => <Tag color="blue">{code}</Tag>,
    },
    {
      title: '标题模板',
      dataIndex: 'titleTemplate',
      key: 'titleTemplate',
      ellipsis: true,
    },
    {
      title: '内容模板',
      dataIndex: 'contentTemplate',
      key: 'contentTemplate',
      ellipsis: true,
      width: 300,
    },
    {
      title: '语言',
      dataIndex: 'locale',
      key: 'locale',
      width: 100,
      render: (locale) => locale || 'zh_CN',
    },
    {
      title: '通知渠道',
      dataIndex: 'channels',
      key: 'channels',
      width: 200,
      render: (channels: string[]) =>
        channels?.map((ch) => (
          <Tag key={ch} color="green">
            {ch}
          </Tag>
        )),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
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
          <Popconfirm
            title="确定要删除这个模板吗？"
            onConfirm={() => handleDelete(record.id!)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // ========== 渲染 ==========

  return (
    <div style={{ padding: '24px' }}>
      <Card>
        {/* 页面标题和操作栏 */}
        <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
          <div>
            <h1 style={{ margin: 0, fontSize: 24, fontWeight: 600 }}>📧 通知模板管理</h1>
            <p style={{ margin: '8px 0 0', color: '#666' }}>
              管理系统通知模板，支持自定义标题和内容 😎
            </p>
          </div>
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadTemplates} loading={loading}>
              刷新
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              新建模板
            </Button>
          </Space>
        </div>

        {/* 搜索框 */}
        <div style={{ marginBottom: 16 }}>
          <Search
            placeholder="搜索模板编码、标题或内容"
            allowClear
            enterButton="搜索"
            size="large"
            onSearch={handleSearch}
            onChange={(e) => handleSearch(e.target.value)}
            style={{ maxWidth: 400 }}
          />
        </div>

        {/* 表格 */}
        <Table
          columns={columns}
          dataSource={filteredTemplates}
          rowKey="id"
          loading={loading}
          pagination={{
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
            defaultPageSize: 10,
            pageSizeOptions: ['10', '20', '50', '100'],
          }}
          scroll={{ x: 1200 }}
        />
      </Card>

      {/* 编辑 Modal */}
      <TemplateEditModal
        visible={editModalVisible}
        template={currentTemplate}
        onSave={handleSaveTemplate}
        onCancel={() => setEditModalVisible(false)}
      />

      {/* 预览 Modal */}
      <TemplatePreviewModal
        visible={previewModalVisible}
        template={previewTemplate}
        onClose={() => setPreviewModalVisible(false)}
      />
    </div>
  );
};

export default NotificationTemplateList;
