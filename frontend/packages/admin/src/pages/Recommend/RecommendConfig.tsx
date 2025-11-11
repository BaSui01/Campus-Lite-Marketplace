/**
 * 推荐管理配置页
 * 
 * 功能：
 * - 推荐算法配置
 * - 推荐效果分析
 * - 推荐权重调整
 * - 推荐策略切换
 * - 推荐效果统计
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */
import React, { useState, useEffect } from 'react';
import {
  Card,
  Form,
  Input,
  InputNumber,
  Select,
  Switch,
  Button,
  Space,
  App,
  Divider,
  Row,
  Col,
  Statistic,
  Tag,
  Table,
} from 'antd';
import {
  SaveOutlined,
  ReloadOutlined,
  BarChartOutlined,
  ThunderboltOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { recommendService } from '@/services';
import ReactECharts from 'echarts-for-react';

const { Option } = Select;
const { TextArea } = Input;

/**
 * 推荐算法类型
 */
const ALGORITHM_TYPE_MAP: Record<string, { text: string; description: string }> = {
  COLLABORATIVE_FILTERING: { 
    text: '协同过滤', 
    description: '基于用户行为相似度推荐' 
  },
  CONTENT_BASED: { 
    text: '基于内容', 
    description: '基于商品属性相似度推荐' 
  },
  HYBRID: { 
    text: '混合推荐', 
    description: '结合多种算法的混合推荐' 
  },
  HOT: { 
    text: '热门推荐', 
    description: '推荐热门商品' 
  },
  PERSONALIZED: { 
    text: '个性化推荐', 
    description: '基于用户画像的个性化推荐' 
  },
};

export const RecommendConfig: React.FC = () => {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();

  // 查询推荐配置
  const { data: config, isLoading: configLoading } = useQuery({
    queryKey: ['recommend', 'config'],
    queryFn: () => recommendService.getConfig(),
  });

  // 查询推荐统计
  const { data: statistics } = useQuery({
    queryKey: ['recommend', 'statistics'],
    queryFn: () => recommendService.getStatistics(),
  });

  // 保存配置
  const saveMutation = useMutation({
    mutationFn: (values: any) => recommendService.updateConfig(values),
    onSuccess: () => {
      message.success('推荐配置已保存');
      queryClient.invalidateQueries({ queryKey: ['recommend', 'config'] });
    },
    onError: (error: any) => {
      message.error(error?.message || '保存推荐配置失败');
    },
  });

  // 重置配置
  const handleReset = () => {
    if (config) {
      form.setFieldsValue(config);
      message.info('配置已重置');
    }
  };

  // 保存配置
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      saveMutation.mutate(values);
    } catch (error) {
      console.error('表单验证失败', error);
    }
  };

  // 推荐效果趋势图表配置
  const effectTrendOption = {
    title: {
      text: '推荐效果趋势',
      left: 'center',
    },
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: ['点击率', '转化率'],
      bottom: 0,
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}%',
      },
    },
    series: [
      {
        name: '点击率',
        data: [20, 22, 21, 23, 25, 24, 23],
        type: 'line',
        smooth: true,
      },
      {
        name: '转化率',
        data: [5, 5.5, 5.2, 5.8, 6.0, 5.6, 5.6],
        type: 'line',
        smooth: true,
      },
    ],
  };

  // 初始化表单值
  useEffect(() => {
    if (config) {
      form.setFieldsValue(config);
    }
  }, [config, form]);

  return (
    <div style={{ padding: '24px' }}>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="推荐总数"
              value={statistics?.totalRecommendations || 0}
              prefix={<ThunderboltOutlined />}
              suffix="次"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="点击率"
              value={(statistics?.clickRate || 0) * 100}
              precision={2}
              suffix="%"
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="转化率"
              value={(statistics?.conversionRate || 0) * 100}
              precision={2}
              suffix="%"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="平均评分"
              value={statistics?.avgScore || 0}
              precision={2}
              suffix="/ 1.0"
            />
          </Card>
        </Col>
      </Row>

      {/* 效果趋势图 */}
      <Card style={{ marginBottom: 24 }}>
        <ReactECharts option={effectTrendOption} style={{ height: 400 }} />
      </Card>

      {/* 推荐配置表单 */}
      <Card 
        title="推荐配置" 
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={handleReset}>
              重置
            </Button>
            <Button 
              type="primary" 
              icon={<SaveOutlined />} 
              onClick={handleSave}
              loading={saveMutation.isPending}
            >
              保存
            </Button>
          </Space>
        }
      >
        <Form
          form={form}
          layout="vertical"
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="algorithm"
                label="推荐算法"
                rules={[{ required: true, message: '请选择推荐算法' }]}
              >
                <Select placeholder="请选择推荐算法">
                  {Object.entries(ALGORITHM_TYPE_MAP).map(([key, value]) => (
                    <Option key={key} value={key}>
                      <div>
                        <div>{value.text}</div>
                        <div style={{ fontSize: 12, color: '#8c8c8c' }}>
                          {value.description}
                        </div>
                      </div>
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="enabled"
                label="启用推荐"
                valuePropName="checked"
              >
                <Switch 
                  checkedChildren="启用" 
                  unCheckedChildren="禁用" 
                />
              </Form.Item>
            </Col>
          </Row>

          <Divider orientation="left">算法权重配置</Divider>

          <Row gutter={16}>
            <Col span={6}>
              <Form.Item
                name={['weights', 'collaborative']}
                label="协同过滤权重"
                rules={[{ required: true }]}
              >
                <InputNumber
                  min={0}
                  max={1}
                  step={0.1}
                  precision={2}
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item
                name={['weights', 'content']}
                label="内容权重"
                rules={[{ required: true }]}
              >
                <InputNumber
                  min={0}
                  max={1}
                  step={0.1}
                  precision={2}
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item
                name={['weights', 'hot']}
                label="热门权重"
                rules={[{ required: true }]}
              >
                <InputNumber
                  min={0}
                  max={1}
                  step={0.1}
                  precision={2}
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item
                name={['weights', 'personalized']}
                label="个性化权重"
                rules={[{ required: true }]}
              >
                <InputNumber
                  min={0}
                  max={1}
                  step={0.1}
                  precision={2}
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
          </Row>

          <Divider orientation="left">参数配置</Divider>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name={['params', 'maxRecommendations']}
                label="最大推荐数"
                rules={[{ required: true }]}
              >
                <InputNumber
                  min={1}
                  max={100}
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name={['params', 'minScore']}
                label="最低评分阈值"
                rules={[{ required: true }]}
              >
                <InputNumber
                  min={0}
                  max={1}
                  step={0.1}
                  precision={2}
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name={['params', 'refreshInterval']}
                label="刷新间隔（秒）"
                rules={[{ required: true }]}
              >
                <InputNumber
                  min={60}
                  max={86400}
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Card>

      {/* 算法说明 */}
      <Card title="算法说明" style={{ marginTop: 24 }}>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          {Object.entries(ALGORITHM_TYPE_MAP).map(([key, value]) => (
            <div key={key}>
              <Tag color="blue">{value.text}</Tag>
              <span style={{ marginLeft: 8, color: '#8c8c8c' }}>
                {value.description}
              </span>
            </div>
          ))}
        </Space>
      </Card>
    </div>
  );
};

export default RecommendConfig;
