/**
 * 组件复用示例展示
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React, { useState } from 'react';
import {
  Card,
  Typography,
  Space,
  Table,
  Tag,
  Button,
  Input,
  Modal,
  Form,
  Selector,
  Timeline,
  Progress,
  Alert,
} from 'antd';
import {
  UserOutlined,
  MailOutlined,
  FileSearchOutlined,
  SafetyOutlined,
} from '@ant-design/icons';
import { PermissionGuard } from '../components';
import { PERMISSION_CODES } from '@campus/shared';
import {
  StatCard,
  LineChart,
  BarChart,
} from '../components';
import {
  EmptyState,
  NoDataEmpty,
  NoPermissionEmpty,
} from '../components/Feedback';
import {
  CreateSuccess,
  UpdateSuccess,
  DeleteSuccess,
} from '../components/Feedback';
import PageTransition from '../components/Transitions';
import { usePermission } from '../hooks';

const { Title, Paragraph, Text } = Typography;

interface MockData {
  id: number;
  name: string;
  email: string;
  role: string;
  status: string;
}

const mockData: MockData[] = [
  { id: 1, name: '张三', email: 'zhangsan@example.com', role: '管理员', status: '活跃' },
  { id: 2, name: '李四', email: 'lisi@example.com', role: '编辑', status: '停用' },
  { id: 3, name: '王五', email: 'wangwu@example.com', role: '访客', status: '活跃' },
];

export const ComponentGallery: React.FC = () => {
  const [modalVisible, setModalVisible] = useState(false);
  const [successType, setSuccessType] = useState<'create' | 'update' | 'delete' | null>(null);
  const { hasPermission } = usePermission();

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id' },
    { title: '姓名', dataIndex: 'name', key: 'name' },
    { title: '邮箱', dataIndex: 'email', key: 'email' },
    {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      render: (role: string) => (
        <Tag color="blue">{role}</Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === '活跃' ? 'green' : 'red'}>{status}</Tag>
      ),
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record: MockData) => (
        <Space>
          <Button size="small" onClick={() => setModalVisible(true)}>
            编辑
          </Button>
          <Button size="small" danger onClick={() => setSuccessType('delete')}>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Title level={2}>🧩 组件复用示例展示</Title>
      
      <PageTransition>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          
          {/* 数据展示卡片 */}
          <Card title="📊 数据统计卡片">
            <Space wrap>
              <StatCard
                title="总用户数"
                value={1234}
                icon={<UserOutlined />}
                color="#1890ff"
                trend={12}
                trendLabel="较上月"
              />
              <StatCard
                title="今日邮件"
                value={56}
                icon={<MailOutlined />}
                color="#52c41a"
              />
              <StatCard
                title="系统日志"
                value={89}
                icon={<FileSearchOutlined />}
                color="#faad14"
              />
              <StatCard
                title="安全事件"
                value={3}
                icon={<SafetyOutlined />}
                color="#f5222d"
              />
            </Space>
          </Card>

          {/* 图表示例 */}
          <Card title="📈 图表组件">
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <div>
                <Title level={4}>用户增长趋势</Title>
                <LineChart
                  data={[
                    { time: '1月', value: 100 },
                    { time: '2月', value: 200 },
                    { time: '3月', value: 300 },
                    { time: '4月', value: 400 },
                    { time: '5月', value: 500 },
                  ]}
                  height={300}
                  color="#52c41a"
                />
              </div>
              <div>
                <Title level={4}>访问量统计</Title>
                <BarChart
                  data={[
                    { name: '首页', value: 1234 },
                    { name: '用户页', value: 567 },
                    { name: '商品页', value: 890 },
                    { name: '订单页', value: 456 },
                  ]}
                  height={300}
                  color="#1890ff"
                />
              </div>
            </Space>
          </Card>

          {/* 权限控制示例 */}
          <Card title="🔐 权限控制组件">
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <Paragraph>
                使用 <Text code>PermissionGuard</Text> 组件可以方便地控制页面和功能的访问权限。
              </Paragraph>
              
              <Space wrap>
                <PermissionGuard permission={PERMISSION_CODES.SYSTEM_USER_VIEW}>
                  <Button type="primary">查看用户（需要权限）</Button>
                </PermissionGuard>
                
                <PermissionGuard permission={PERMISSION_CODES.SYSTEM_USER_CREATE}>
                  <Button type="primary">创建用户（需要权限）</Button>
                </PermissionGuard>
                
                <PermissionGuard permissions={[
                  PERMISSION_CODES.SYSTEM_USER_UPDATE,
                  PERMISSION_CODES.SYSTEM_USER_DELETE
                ]}>
                  <Button type="primary">编辑/删除用户（任一权限）</Button>
                </PermissionGuard>
              </Space>
              
              <Alert
                message="权限检查结果"
                description={`当前用户权限状态：${
                  hasPermission(PERMISSION_CODES.SYSTEM_USER_VIEW) ? '有用户查看权限' : '无用户查看权限'
                }, ${
                  hasPermission(PERMISSION_CODES.SYSTEM_USER_CREATE) ? '有用户创建权限' : '无用户创建权限'
                }`}
                type="info"
              />
            </Space>
          </Card>

          {/* 表格操作示例 */}
          <Card title="📋 表格与操作">
            <Table
              dataSource={mockData}
              columns={columns}
              rowKey="id"
              pagination={false}
            />
          </Card>

          {/* 空状态示例 */}
          <Card title="📄 空状态组件">
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <div>
                <Title level={5}>无数据空状态</Title>
                <NoDataEmpty />
              </div>
              <div>
                <Title level={5}>无权限空状态</Title>
                <NoPermissionEmpty />
              </div>
            </Space>
          </Card>

          {/* 成功状态示例 */}
          <Card title="✅ 成功状态组件">
            <Space wrap>
              <Button onClick={() => setSuccessType('create')}>
                创建成功示例
              </Button>
              <Button onClick={() => setSuccessType('update')}>
                更新成功示例
              </Button>
              <Button danger onClick={() => setSuccessType('delete')}>
                删除成功示例
              </Button>
            </Space>
          </Card>

          {/* 进度操作示例 */}
          <Card title="📌 操作流程示例">
            <Timeline>
              <Timeline.Item color="blue">创建数据</Timeline.Item>
              <Timeline.Item color="green">数据验证</Timeline.Item>
              <Timeline.Item color="orange">提交审核</Timeline.Item>
              <Timeline.Item>
                <div>处理完成</div>
                <Progress percent={100} showInfo={false} />
              </Timeline.Item>
            </Timeline>
          </Card>

        </Space>
      </PageTransition>

      {/* 编辑模态框 */}
      <Modal
        title="编辑用户"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={[
          <Button key="cancel" onClick={() => setModalVisible(false)}>
            取消
          </Button>,
          <Button key="submit" type="primary" onClick={() => {
            setModalVisible(false);
            setSuccessType('update');
          }}>
            提交
          </Button>,
        ]}
      >
        <Form layout="vertical">
          <Form.Item label="姓名">
            <Input placeholder="请输入姓名" />
          </Form.Item>
          <Form.Item label="邮箱">
            <Input placeholder="请输入邮箱" />
          </Form.Item>
          <Form.Item label="角色">
            <Selector style={{ width: '100%' }} placeholder="请选择角色">
              <Selector.Option value="admin">管理员</Selector.Option>
              <Selector.Option value="editor">编辑</Selector.Option>
              <Selector.Option value="viewer">查看者</Selector.Option>
            </Selector>
          </Form.Item>
          <Form.Item label="状态">
            <Selector style={{ width: '100%' }} placeholder="请选择状态">
              <Selector.Option value="active">活跃</Selector.Option>
              <Selector.Option value="inactive">停用</Selector.Option>
            </Selector>
          </Form.Item>
        </Form>
      </Modal>

      {/* 成功状态弹窗 */}
      <Modal
        open={successType !== null}
        footer={null}
        onCancel={() => setSuccessType(null)}
        width={500}
      >
        {successType === 'create' && (
          <CreateSuccess onAction={() => setSuccessType(null)} onBack={() => setSuccessType(null)} />
        )}
        {successType === 'update' && (
          <UpdateSuccess onAction={() => setSuccessType(null)} onBack={() => setSuccessType(null)} />
        )}
        {successType === 'delete' && (
          <DeleteSuccess onAction={() => setSuccessType(null)} onBack={() => setSuccessType(null)} />
        )}
      </Modal>

    </div>
  );
};
