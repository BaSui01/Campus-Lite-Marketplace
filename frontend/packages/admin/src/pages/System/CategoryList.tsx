/**
 * 分类管理页
 * 
 * 功能：
 * - 树形结构展示（最多3级）
 * - 关键词搜索
 * - 状态筛选（启用/禁用）
 * - 添加/编辑/删除分类
 * - 拖拽排序
 * - 启用/禁用分类
 * - 查看分类统计
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */

import { useState } from 'react';
import {
  Table,
  Button,
  Input,
  Select,
  Space,
  Tag,
  message,
  Modal,
  Form,
  Card,
  Popconfirm,
  InputNumber,
  Tooltip,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  FolderOutlined,
  FolderOpenOutlined,
  BarChartOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { categoryService, CategoryStatus } from '@campus/shared/services';
import type { Category, CategoryRequest } from '@campus/shared/services';

const { Option } = Select;
const { TextArea } = Input;

/**
 * 分类列表页组件
 */
export const CategoryList: React.FC = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm<CategoryRequest>();

  // 查询参数
  const [keyword, setKeyword] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<CategoryStatus | undefined>();

  // 弹窗状态
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);

  // 查询分类树
  const { data: treeData, isLoading, refetch } = useQuery({
    queryKey: ['categories', 'tree', { keyword, statusFilter }],
    queryFn: async () => {
      const allCategories = await categoryService.tree();
      
      // 前端筛选（根据关键词和状态）
      let filtered = allCategories;
      
      if (keyword) {
        filtered = filterTreeByKeyword(filtered, keyword);
      }
      
      if (statusFilter) {
        filtered = filterTreeByStatus(filtered, statusFilter);
      }
      
      return filtered;
    },
    staleTime: 5 * 60 * 1000, // 缓存5分钟
  });

  // 添加/编辑分类
  const saveMutation = useMutation({
    mutationFn: (values: CategoryRequest) =>
      editingCategory
        ? categoryService.update(editingCategory.id, values)
        : categoryService.create(values),
    onSuccess: () => {
      message.success(editingCategory ? '编辑成功' : '添加成功');
      setIsModalVisible(false);
      form.resetFields();
      setEditingCategory(null);
      refetch();
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
    onError: (error: any) => {
      message.error(error?.message || '操作失败');
    },
  });

  // 删除分类
  const deleteMutation = useMutation({
    mutationFn: (id: number) => categoryService.delete(id),
    onSuccess: () => {
      message.success('删除成功');
      refetch();
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
    onError: (error: any) => {
      message.error(error?.message || '删除失败，请确保该分类没有子分类和商品');
    },
  });

  // 更新状态
  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: CategoryStatus }) =>
      categoryService.updateStatus(id, status),
    onSuccess: () => {
      message.success('状态更新成功');
      refetch();
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
    onError: () => {
      message.error('状态更新失败');
    },
  });

  // 关键词筛选（递归）
  const filterTreeByKeyword = (tree: Category[], keyword: string): Category[] => {
    return tree.filter(node => {
      const matchSelf = node.name.toLowerCase().includes(keyword.toLowerCase());
      const matchChildren = node.children && node.children.length > 0 
        ? filterTreeByKeyword(node.children, keyword).length > 0
        : false;
      
      if (matchChildren && node.children) {
        node.children = filterTreeByKeyword(node.children, keyword);
      }
      
      return matchSelf || matchChildren;
    });
  };

  // 状态筛选（递归）
  const filterTreeByStatus = (tree: Category[], status: CategoryStatus): Category[] => {
    return tree.filter(node => {
      const matchSelf = node.status === status;
      const matchChildren = node.children && node.children.length > 0
        ? filterTreeByStatus(node.children, status).length > 0
        : false;
      
      if (matchChildren && node.children) {
        node.children = filterTreeByStatus(node.children, status);
      }
      
      return matchSelf || matchChildren;
    });
  };

  // 搜索处理
  const handleSearch = () => {
    refetch();
  };

  // 重置筛选
  const handleReset = () => {
    setKeyword('');
    setStatusFilter(undefined);
  };

  // 打开添加弹窗
  const handleAdd = (parentCategory?: Category) => {
    setEditingCategory(null);
    form.resetFields();
    form.setFieldsValue({
      parentId: parentCategory?.id,
      status: CategoryStatus.ENABLED,
      sortOrder: 0,
    });
    setIsModalVisible(true);
  };

  // 打开编辑弹窗
  const handleEdit = (category: Category) => {
    setEditingCategory(category);
    form.setFieldsValue({
      name: category.name,
      parentId: category.parentId,
      icon: category.icon,
      description: category.description,
      sortOrder: category.sortOrder,
      status: category.status,
    });
    setIsModalVisible(true);
  };

  // 删除分类
  const handleDelete = (id: number) => {
    deleteMutation.mutate(id);
  };

  // 切换状态
  const handleToggleStatus = (category: Category) => {
    const newStatus = category.status === CategoryStatus.ENABLED 
      ? CategoryStatus.DISABLED 
      : CategoryStatus.ENABLED;
    
    updateStatusMutation.mutate({ id: category.id, status: newStatus });
  };

  // 保存表单
  const handleFormSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      // 验证层级限制
      if (values.parentId) {
        const parent = findCategoryById(treeData || [], values.parentId);
        if (parent && parent.level >= 2) {
          message.error('最多支持3级分类，该父分类已达到层级限制');
          return;
        }
      }
      
      saveMutation.mutate(values);
    } catch (error) {
      console.error('表单验证失败:', error);
    }
  };

  // 根据ID查找分类（递归）
  const findCategoryById = (tree: Category[], id: number): Category | null => {
    for (const node of tree) {
      if (node.id === id) return node;
      if (node.children && node.children.length > 0) {
        const found = findCategoryById(node.children, id);
        if (found) return found;
      }
    }
    return null;
  };

  // 获取可选父分类列表（扁平化，排除自己和子孙）
  const getParentOptions = (): Category[] => {
    if (!treeData) return [];
    
    const flatList = categoryService.flatten(treeData);
    
    if (!editingCategory) {
      // 添加模式：只排除3级分类
      return flatList.filter(c => c.level < 2);
    }
    
    // 编辑模式：排除自己和子孙
    const excludeIds = new Set([editingCategory.id]);
    const collectDescendants = (node: Category) => {
      if (node.children) {
        node.children.forEach(child => {
          excludeIds.add(child.id);
          collectDescendants(child);
        });
      }
    };
    collectDescendants(editingCategory);
    
    return flatList.filter(c => !excludeIds.has(c.id) && c.level < 2);
  };

  // 表格列定义
  const columns: ColumnsType<Category> = [
    {
      title: '分类名称',
      dataIndex: 'name',
      key: 'name',
      width: 300,
      render: (name: string, record: Category) => (
        <Space>
          {record.level === 0 ? (
            <FolderOpenOutlined style={{ color: '#1890ff' }} />
          ) : (
            <FolderOutlined style={{ color: '#52c41a' }} />
          )}
          <span style={{ fontWeight: record.level === 0 ? 'bold' : 'normal' }}>
            {name}
          </span>
          <Tag>{`L${record.level + 1}`}</Tag>
        </Space>
      ),
    },
    {
      title: '图标',
      dataIndex: 'icon',
      key: 'icon',
      width: 100,
      render: (icon: string) => icon ? (
        <img src={icon} alt="icon" style={{ width: 32, height: 32, objectFit: 'contain' }} />
      ) : '-',
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      key: 'sortOrder',
      width: 100,
      sorter: (a, b) => a.sortOrder - b.sortOrder,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: CategoryStatus) => (
        <Tag 
          color={status === CategoryStatus.ENABLED ? 'green' : 'red'} 
          icon={status === CategoryStatus.ENABLED ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
        >
          {status === CategoryStatus.ENABLED ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      width: 200,
      ellipsis: true,
      render: (description: string) => description || '-',
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => new Date(date).toLocaleString('zh-CN'),
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right',
      width: 280,
      render: (_: any, record: Category) => (
        <Space size="small">
          {record.level < 2 && (
            <Tooltip title="添加子分类">
              <Button
                type="link"
                size="small"
                icon={<PlusOutlined />}
                onClick={() => handleAdd(record)}
              >
                添加子分类
              </Button>
            </Tooltip>
          )}
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
            size="small"
            onClick={() => handleToggleStatus(record)}
          >
            {record.status === CategoryStatus.ENABLED ? '禁用' : '启用'}
          </Button>
          <Popconfirm
            title="确定删除此分类吗？"
            description="删除后无法恢复，请确保该分类没有子分类和商品。"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      {/* 页面标题 */}
      <h2 style={{ marginBottom: 24 }}>分类管理</h2>

      {/* 搜索筛选区 */}
      <Card style={{ marginBottom: 16 }}>
        <Space size="middle" wrap>
          <Input
            placeholder="搜索分类名称"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={handleSearch}
            style={{ width: 250 }}
            prefix={<SearchOutlined />}
          />
          <Select
            placeholder="状态筛选"
            value={statusFilter}
            onChange={setStatusFilter}
            allowClear
            style={{ width: 150 }}
          >
            <Option value={CategoryStatus.ENABLED}>启用</Option>
            <Option value={CategoryStatus.DISABLED}>禁用</Option>
          </Select>
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            查询
          </Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
      </Card>

      {/* 操作按钮 */}
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>
          添加一级分类
        </Button>
      </div>

      {/* 数据表格（树形） */}
      <Table
        columns={columns}
        dataSource={treeData || []}
        rowKey="id"
        loading={isLoading}
        pagination={false}
        expandable={{
          defaultExpandAllRows: false,
          indentSize: 24,
        }}
        scroll={{ x: 1400 }}
      />

      {/* 添加/编辑弹窗 */}
      <Modal
        title={editingCategory ? '编辑分类' : '添加分类'}
        open={isModalVisible}
        onOk={handleFormSubmit}
        onCancel={() => {
          setIsModalVisible(false);
          form.resetFields();
          setEditingCategory(null);
        }}
        width={600}
        confirmLoading={saveMutation.isPending}
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{ status: CategoryStatus.ENABLED, sortOrder: 0 }}
        >
          <Form.Item
            label="父分类"
            name="parentId"
            tooltip="不选择则为一级分类"
          >
            <Select placeholder="请选择父分类（选填）" allowClear>
              {getParentOptions().map(cat => (
                <Option key={cat.id} value={cat.id}>
                  {'  '.repeat(cat.level)} {cat.name} (L{cat.level + 1})
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="分类名称"
            name="name"
            rules={[
              { required: true, message: '请输入分类名称' },
              { min: 2, max: 20, message: '分类名称长度为2-20个字符' },
            ]}
          >
            <Input placeholder="请输入分类名称" />
          </Form.Item>

          <Form.Item
            label="分类图标"
            name="icon"
            tooltip="图片URL地址"
            rules={[
              { type: 'url', message: '请输入正确的URL地址' },
            ]}
          >
            <Input placeholder="请输入图标URL（选填）" />
          </Form.Item>

          <Form.Item
            label="分类描述"
            name="description"
            rules={[
              { max: 100, message: '描述长度不能超过100个字符' },
            ]}
          >
            <TextArea 
              placeholder="请输入分类描述（选填）" 
              rows={3}
              maxLength={100}
              showCount
            />
          </Form.Item>

          <Form.Item
            label="排序权重"
            name="sortOrder"
            tooltip="数值越大越靠前"
            rules={[{ required: true, message: '请输入排序权重' }]}
          >
            <InputNumber 
              placeholder="请输入排序权重" 
              style={{ width: '100%' }}
              min={0}
              max={9999}
            />
          </Form.Item>

          <Form.Item
            label="状态"
            name="status"
            rules={[{ required: true, message: '请选择状态' }]}
          >
            <Select>
              <Option value={CategoryStatus.ENABLED}>启用</Option>
              <Option value={CategoryStatus.DISABLED}>禁用</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CategoryList;
