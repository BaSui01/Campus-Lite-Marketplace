/**
 * 角色权限管理页面
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React, { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Tree,
  message,
  Typography,
  Tag,
  Tooltip,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  KeyOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { roleService } from '@/services';
import { PermissionGuard } from '@/components';
import { PERMISSION_CODES } from '@campus/shared';
import type { RoleSummary, RoleDetail } from '@/services'; // ✅ 修复：类型也从本地 services 导入

const { Title } = Typography;
const { TextArea } = Input;

const RoleList: React.FC = () => {
  const queryClient = useQueryClient();
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [selectedRole, setSelectedRole] = useState<RoleDetail | null>(null);
  const [createForm] = Form.useForm();
  const [editForm] = Form.useForm();

  // ===== 查询角色列表 =====
  const { data: roles, isLoading } = useQuery({
    queryKey: ['roles'],
    queryFn: () => roleService.listRoles(),
  });

  // ===== 权限树数据 =====
  const permissionTreeData = React.useMemo(() => {
    const groupByModule = (permissions: string[]) => {
      const grouped: Record<string, string[]> = {};
      
      permissions.forEach(permission => {
        const [module, action] = permission.split(':');
        if (!grouped[module]) {
          grouped[module] = [];
        }
        grouped[module].push(action);
      });
      
      return Object.entries(grouped).map(([module, actions]) => ({
        title: module,
        key: module,
        children: actions.map(action => ({
          title: `${module}:${action}`,
          key: `${module}:${action}`,
          isLeaf: true,
        })),
      }));
    };

    const allPermissions = Object.values(PERMISSION_CODES);
    return groupByModule(allPermissions);
  }, []);

  // ===== 创建角色 Mutation =====
  const createRoleMutation = useMutation({
    mutationFn: (payload: { name: string; description?: string; permissions: string[] }) => {
      return roleService.createRole(payload);
    },
    onSuccess: () => {
      message.success('角色创建成功！🎉');
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      setCreateModalVisible(false);
      createForm.resetFields();
    },
    onError: (error: any) => {
      message.error(`创建失败：${error.message} 😰`);
    },
  });

  // ===== 更新角色 Mutation =====
  const updateRoleMutation = useMutation({
    mutationFn: ({ roleId, payload }: { roleId: number; payload: any }) => {
      return roleService.updateRole(roleId, payload);
    },
    onSuccess: () => {
      message.success('角色更新成功！🎉');
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      setEditModalVisible(false);
      setSelectedRole(null);
      editForm.resetFields();
    },
    onError: (error: any) => {
      message.error(`更新失败：${error.message} 😰`);
    },
  });

  // ===== 删除角色 Mutation =====
  const deleteRoleMutation = useMutation({
    mutationFn: (roleId: number) => {
      return roleService.deleteRole(roleId);
    },
    onSuccess: () => {
      message.success('角色删除成功！🎉');
      queryClient.invalidateQueries({ queryKey: ['roles'] });
    },
    onError: (error: any) => {
      message.error(`删除失败：${error.message} 😰`);
    },
  });

  // ===== 表格列定义 =====
  const columns: ColumnsType<RoleSummary> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '角色名称',
      dataIndex: 'name',
      key: 'name',
      render: (text, record) => (
        <div>
          <div>{text}</div>
          {record.builtIn && (
            <Tag color="orange" size="small" style={{ marginTop: 4 }}>内置角色</Tag>
          )}
        </div>
      ),
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      render: (text) => (
        <Tooltip title={text} placement="topLeft">
          <div style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {text || '-'}
          </div>
        </Tooltip>
      ),
    },
    {
      title: '权限数量',
      dataIndex: 'permissionCount',
      key: 'permissionCount',
      render: (count) => (
        <Tag color="blue">{count} 项权限</Tag>
      ),
    },
    {
      title: '用户数量',
      dataIndex: 'userCount',
      key: 'userCount',
      render: (count) => (
        <Tag color="green">{count} 个用户</Tag>
      ),
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right',
      width: 200,
      render: (_, record) => (
        <PermissionGuard permission={PERMISSION_CODES.SYSTEM_ROLE_ASSIGN}>
          <Space>
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEditRole(record.id)}
              disabled={record.builtIn}
            >
              编辑
            </Button>
            <Button
              type="link"
              danger
              size="small"
              icon={<DeleteOutlined />}
              onClick={() => handleDeleteRole(record)}
              disabled={record.builtIn}
            >
              删除
            </Button>
          </Space>
        </PermissionGuard>
      ),
    },
  ];

  // ===== 打开创建弹窗 =====
  const handleCreateRole = () => {
    setCreateModalVisible(true);
  };

  // ===== 打开编辑弹窗 =====
  const handleEditRole = async (roleId: number) => {
    try {
      const role = await roleService.getRole(roleId);
      setSelectedRole(role);
      editForm.setFieldsValue({
        name: role.name,
        description: role.description,
        permissions: role.permissions,
      });
      setEditModalVisible(true);
    } catch (error) {
      message.error('获取角色信息失败 😰');
    }
  };

  // ===== 删除角色 =====
  const handleDeleteRole = (role: RoleSummary) => {
    Modal.confirm({
      title: '确认删除角色？',
      content: `确定要删除角色 "${role.name}" 吗？删除后使用该角色的用户将失去相应权限。`,
      okText: '确认删除',
      cancelText: '取消',
      okType: 'danger',
      onOk: () => deleteRoleMutation.mutate(role.id),
    });
  };

  // ===== 确认创建角色 =====
  const handleCreateSubmit = () => {
    createForm.validateFields().then((values) => {
      createRoleMutation.mutate(values);
    });
  };

  // ===== 确认更新角色 =====
  const handleEditSubmit = () => {
    if (!selectedRole) return;
    
    editForm.validateFields().then((values) => {
      updateRoleMutation.mutate({
        roleId: selectedRole.id,
        payload: values,
      });
    });
  };

  return (
    <div className="role-list" style={{ padding: '24px' }}>
      <PermissionGuard permission={PERMISSION_CODES.SYSTEM_ROLE_ASSIGN}>
        <Card>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Title level={2}>🔐 角色权限管理</Title>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={handleCreateRole}
              >
                创建角色
              </Button>
            </div>

            <Table
              columns={columns}
              dataSource={roles || []}
              rowKey="id"
              loading={isLoading}
              pagination={{
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total) => `共 ${total} 个角色`,
              }}
              scroll={{ x: 1000 }}
            />
          </Space>
        </Card>
      </PermissionGuard>

      {/* 创建角色弹窗 */}
      <Modal
        title="创建角色"
        open={createModalVisible}
        onOk={handleCreateSubmit}
        onCancel={() => {
          setCreateModalVisible(false);
          createForm.resetFields();
        }}
        confirmLoading={createRoleMutation.isPending}
        width={600}
      >
        <Form form={createForm} layout="vertical">
          <Form.Item
            name="name"
            label="角色名称"
            rules={[
              { required: true, message: '请输入角色名称！' },
              { min: 2, message: '角色名称至少2个字符！' },
            ]}
          >
            <Input placeholder="请输入角色名称" />
          </Form.Item>
          <Form.Item
            name="description"
            label="角色描述"
          >
            <TextArea rows={3} placeholder="请输入角色描述" />
          </Form.Item>
          <Form.Item
            name="permissions"
            label="权限配置"
            rules={[{ required: true, message: '请选择权限！' }]}
          >
            <Tree
              checkable
              treeData={permissionTreeData}
              height={300}
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* 编辑角色弹窗 */}
      <Modal
        title={`编辑角色：${selectedRole?.name}`}
        open={editModalVisible}
        onOk={handleEditSubmit}
        onCancel={() => {
          setEditModalVisible(false);
          setSelectedRole(null);
          editForm.resetFields();
        }}
        confirmLoading={updateRoleMutation.isPending}
        width={600}
      >
        {selectedRole && (
          <Form form={editForm} layout="vertical">
            <Form.Item
              name="name"
              label="角色名称"
              rules={[
                { required: true, message: '请输入角色名称！' },
                { min: 2, message: '角色名称至少2个字符！' },
              ]}
            >
              <Input placeholder="请输入角色名称" />
            </Form.Item>
            <Form.Item
              name="description"
              label="角色描述"
            >
              <TextArea rows={3} placeholder="请输入角色描述" />
            </Form.Item>
            <Form.Item
              name="permissions"
              label="权限配置"
              rules={[{ required: true, message: '请选择权限！' }]}
            >
              <Tree
                checkable
                treeData={permissionTreeData}
                height={300}
              />
            </Form.Item>
          </Form>
        )}
      </Modal>
    </div>
  );
};

export default RoleList;
