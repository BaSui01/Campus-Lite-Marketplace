/**
 * 任务管理页面
 * 
 * 功能：
 * - 任务列表展示
 * - 启动/暂停任务
 * - 手动触发任务
 * - 任务统计卡片
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */

import { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Input,
  Row,
  Col,
  Statistic,
  Tooltip,
  Modal,
  App,
  Modal,
} from 'antd';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  ThunderboltOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { taskService, TaskStatus } from '@campus/shared/services/task';
import type { ScheduledTask } from '@campus/shared/services/task';
import dayjs from 'dayjs';

/**
 * 任务状态映射
 */
const TASK_STATUS_MAP = {
  RUNNING: { text: '运行中', color: 'green', icon: <PlayCircleOutlined /> },
  PAUSED: { text: '已暂停', color: 'orange', icon: <PauseCircleOutlined /> },
  DISABLED: { text: '已禁用', color: 'red', icon: <CloseCircleOutlined /> },
};

export const TaskList: React.FC = () => {
  const queryClient = useQueryClient();
  const { message, modal } = App.useApp();

  // 触发任务参数弹窗
  const [triggerModalVisible, setTriggerModalVisible] = useState(false);
  const [selectedTask, setSelectedTask] = useState<ScheduledTask | null>(null);
  const [triggerParams, setTriggerParams] = useState<string>('');

  // 查询任务列表
  const { data: tasks, isLoading, refetch } = useQuery({
    queryKey: ['tasks', 'list'],
    queryFn: () => taskService.list(),
    refetchInterval: 30000, // 30秒自动刷新
  });

  // 触发任务
  const triggerMutation = useMutation({
    mutationFn: ({ name, params }: { name: string; params?: string }) =>
      taskService.trigger(name, params),
    onSuccess: () => {
      message.success('任务已触发执行');
      setTriggerModalVisible(false);
      setTriggerParams('');
      refetch();
    },
    onError: () => {
      message.error('触发任务失败');
    },
  });

  // 暂停任务
  const pauseMutation = useMutation({
    mutationFn: (name: string) => taskService.pause(name),
    onSuccess: () => {
      message.success('任务已暂停');
      refetch();
    },
    onError: () => {
      message.error('暂停任务失败');
    },
  });

  // 恢复任务
  const resumeMutation = useMutation({
    mutationFn: (name: string) => taskService.resume(name),
    onSuccess: () => {
      message.success('任务已恢复');
      refetch();
    },
    onError: () => {
      message.error('恢复任务失败');
    },
  });

  // ==================== 事件处理 ====================

  /**
   * 启动/暂停任务
   */
  const handleToggleStatus = (task: ScheduledTask) => {
    if (task.status === TaskStatus.RUNNING) {
      pauseMutation.mutate(task.name);
    } else {
      resumeMutation.mutate(task.name);
    }
  };

  /**
   * 打开触发任务弹窗
   */
  const handleOpenTriggerModal = (task: ScheduledTask) => {
    setSelectedTask(task);
    setTriggerModalVisible(true);
  };

  /**
   * 确认触发任务
   */
  const handleConfirmTrigger = () => {
    if (!selectedTask) return;
    triggerMutation.mutate({
      name: selectedTask.name,
      params: triggerParams || undefined,
    });
  };

  // ==================== 表格列定义 ====================

  const columns = [
    {
      title: '任务名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
      render: (name: string, record: ScheduledTask) => (
        <div>
          <div style={{ fontWeight: 500 }}>{name}</div>
          {record.description && (
            <div style={{ fontSize: 12, color: '#8c8c8c' }}>{record.description}</div>
          )}
        </div>
      ),
    },
    {
      title: 'Cron表达式',
      dataIndex: 'cron',
      key: 'cron',
      width: 150,
      render: (cron: string) => <Tag color="blue">{cron}</Tag>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: TaskStatus) => {
        const config = TASK_STATUS_MAP[status];
        return (
          <Tag color={config.color} icon={config.icon}>
            {config.text}
          </Tag>
        );
      },
    },
    {
      title: '上次执行',
      dataIndex: 'lastExecuteTime',
      key: 'lastExecuteTime',
      width: 180,
      render: (time: string, record: ScheduledTask) => (
        <div>
          <div>{time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'}</div>
          {record.lastExecuteSuccess !== undefined && (
            <Tag
              color={record.lastExecuteSuccess ? 'green' : 'red'}
              icon={record.lastExecuteSuccess ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
              style={{ marginTop: 4 }}
            >
              {record.lastExecuteSuccess ? '成功' : '失败'}
            </Tag>
          )}
        </div>
      ),
    },
    {
      title: '下次执行',
      dataIndex: 'nextExecuteTime',
      key: 'nextExecuteTime',
      width: 180,
      render: (time: string) => (
        <div>
          <ClockCircleOutlined style={{ marginRight: 4 }} />
          {time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'}
        </div>
      ),
    },
    {
      title: '执行次数',
      key: 'executeCount',
      width: 150,
      render: (_: unknown, record: ScheduledTask) => (
        <div>
          <div>总数: {record.totalExecuteCount || 0}</div>
          <div style={{ fontSize: 12, color: '#8c8c8c' }}>
            成功: {record.successExecuteCount || 0} / 失败: {record.failureExecuteCount || 0}
          </div>
        </div>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      fixed: 'right' as const,
      render: (_: unknown, record: ScheduledTask) => (
        <Space>
          <Button
            size="small"
            type={record.status === TaskStatus.RUNNING ? 'default' : 'primary'}
            icon={record.status === TaskStatus.RUNNING ? <PauseCircleOutlined /> : <PlayCircleOutlined />}
            onClick={() => handleToggleStatus(record)}
            disabled={record.status === TaskStatus.DISABLED}
          >
            {record.status === TaskStatus.RUNNING ? '暂停' : '启动'}
          </Button>
          <Button
            size="small"
            icon={<ThunderboltOutlined />}
            onClick={() => handleOpenTriggerModal(record)}
            disabled={record.status === TaskStatus.DISABLED}
          >
            手动触发
          </Button>
        </Space>
      ),
    },
  ];

  // ==================== 统计数据 ====================

  const runningCount = tasks?.filter((t) => t.status === TaskStatus.RUNNING).length || 0;
  const pausedCount = tasks?.filter((t) => t.status === TaskStatus.PAUSED).length || 0;
  const disabledCount = tasks?.filter((t) => t.status === TaskStatus.DISABLED).length || 0;
  const totalExecuteCount = tasks?.reduce((sum, t) => sum + (t.totalExecuteCount || 0), 0) || 0;

  // ==================== 渲染 ====================

  return (
    <div style={{ padding: 24 }}>
      {/* 页面头部 */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2>
          <ClockCircleOutlined style={{ marginRight: 8 }} />
          任务管理
        </h2>
        <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
          刷新
        </Button>
      </div>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="任务总数"
              value={tasks?.length || 0}
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="运行中"
              value={runningCount}
              valueStyle={{ color: '#52c41a' }}
              prefix={<PlayCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已暂停"
              value={pausedCount}
              valueStyle={{ color: '#faad14' }}
              prefix={<PauseCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="总执行次数"
              value={totalExecuteCount}
              prefix={<ThunderboltOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 任务列表 */}
      <Card>
        <Table
          dataSource={tasks || []}
          columns={columns}
          rowKey="name"
          loading={isLoading}
          pagination={{
            pageSize: 20,
            showTotal: (total) => `共 ${total} 个任务`,
          }}
        />
      </Card>

      {/* 触发任务弹窗 */}
      <Modal
        title="手动触发任务"
        open={triggerModalVisible}
        onOk={handleConfirmTrigger}
        onCancel={() => {
          setTriggerModalVisible(false);
          setTriggerParams('');
        }}
        confirmLoading={triggerMutation.isPending}
      >
        <div style={{ marginBottom: 16 }}>
          <strong>任务名称：</strong>{selectedTask?.name}
        </div>
        <div style={{ marginBottom: 16 }}>
          <strong>任务描述：</strong>{selectedTask?.description || '-'}
        </div>
        <div>
          <strong>任务参数（可选）：</strong>
          <Input
            placeholder="例如：campusId=1"
            value={triggerParams}
            onChange={(e) => setTriggerParams(e.target.value)}
            style={{ marginTop: 8 }}
          />
        </div>
      </Modal>
    </div>
  );
};
