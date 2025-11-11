/**
 * 导出按钮组件
 * 
 * 功能：
 * - 集成 useExport Hook
 * - 导出配置
 * - 下载进度
 * - 自动下载
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import React from 'react';
import { Button, Modal, Form, Select, DatePicker, Progress, Space, App } from 'antd';
import { DownloadOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { useModal } from '@/hooks';
import { useExport } from '@/hooks';
import { ExportType } from '@campus/shared';
import type { ButtonProps } from 'antd';

const { Option } = Select;
const { RangePicker } = DatePicker;

/**
 * 导出类型配置
 */
const EXPORT_TYPE_OPTIONS = [
  { label: '订单数据', value: ExportType.ORDERS },
  { label: '用户数据', value: ExportType.USERS },
  { label: '商品数据', value: ExportType.GOODS },
  { label: '评价数据', value: ExportType.REVIEWS },
  { label: '纠纷数据', value: ExportType.DISPUTES },
  { label: '退款数据', value: ExportType.REFUNDS },
];

/**
 * ExportButton 组件属性
 */
export interface ExportButtonProps extends Omit<ButtonProps, 'onClick'> {
  /** 导出类型，如果不传则弹出选择框 */
  exportType?: ExportType;
  /** 导出参数 */
  exportParams?: Record<string, any>;
  /** 是否显示配置弹窗，默认 true */
  showConfigModal?: boolean;
  /** 成功回调 */
  onSuccess?: (downloadUrl: string) => void;
}

/**
 * 导出按钮组件
 * 
 * @example
 * ```tsx
 * // 简单导出（直接导出，不显示配置）
 * <ExportButton
 *   exportType={ExportType.ORDERS}
 *   exportParams={{ status: 'COMPLETED' }}
 *   showConfigModal={false}
 * >
 *   导出订单
 * </ExportButton>
 * 
 * // 配置导出（显示配置弹窗）
 * <ExportButton>
 *   导出数据
 * </ExportButton>
 * ```
 */
export const ExportButton: React.FC<ExportButtonProps> = ({
  exportType,
  exportParams,
  showConfigModal = true,
  onSuccess,
  children = '导出',
  ...buttonProps
}) => {
  const { message } = App.useApp(); // ✅ 使用 App 提供的 message 实例
  const [form] = Form.useForm();
  const { visible, open, close } = useModal();
  const [selectedType, setSelectedType] = React.useState<ExportType | undefined>(exportType);

  // 导出 Hook
  const { status, progress, exporting, downloadUrl, startExport, download } = useExport({
    type: selectedType || ExportType.ORDERS,
    onSuccess: (url) => {
      message.success('导出成功！');
      onSuccess?.(url);

      // 自动下载
      window.open(url, '_blank');
    },
  });

  /**
   * 处理按钮点击
   */
  const handleClick = () => {
    if (showConfigModal) {
      // 显示配置弹窗
      open();
    } else {
      // 直接导出
      startExport(exportParams);
    }
  };

  /**
   * 处理导出提交
   */
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();

      // 设置导出类型
      setSelectedType(values.type);

      // 构建导出参数
      const params: Record<string, any> = {};
      
      if (values.dateRange && values.dateRange.length === 2) {
        params.startDate = values.dateRange[0].format('YYYY-MM-DD');
        params.endDate = values.dateRange[1].format('YYYY-MM-DD');
      }

      // 合并自定义参数
      Object.assign(params, exportParams);

      // 开始导出
      await startExport(params);

      // 不关闭弹窗，显示进度
    } catch (error) {
      console.error('表单验证失败:', error);
    }
  };

  /**
   * 处理下载
   */
  const handleDownload = () => {
    download();
    close();
    form.resetFields();
  };

  return (
    <>
      <Button
        icon={<DownloadOutlined />}
        onClick={handleClick}
        loading={exporting}
        {...buttonProps}
      >
        {children}
      </Button>

      {/* 导出配置弹窗 */}
      <Modal
        title="导出配置"
        open={visible}
        onOk={handleSubmit}
        onCancel={() => {
          close();
          form.resetFields();
        }}
        okText="开始导出"
        cancelText="取消"
        okButtonProps={{ loading: exporting }}
        maskClosable={!exporting}
        closable={!exporting}
        width={500}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="type"
            label="导出类型"
            rules={[{ required: true, message: '请选择导出类型' }]}
            initialValue={exportType}
          >
            <Select placeholder="请选择导出类型" disabled={!!exportType}>
              {EXPORT_TYPE_OPTIONS.map((option) => (
                <Option key={option.value} value={option.value}>
                  {option.label}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item name="dateRange" label="时间范围">
            <RangePicker style={{ width: '100%' }} />
          </Form.Item>
        </Form>

        {/* 导出进度 */}
        {status === 'PROCESSING' && (
          <div style={{ marginTop: 16 }}>
            <Progress percent={progress} status="active" />
            <p style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
              正在导出，请稍候...
            </p>
          </div>
        )}

        {/* 导出完成 */}
        {status === 'COMPLETED' && downloadUrl && (
          <div style={{ marginTop: 16, textAlign: 'center' }}>
            <CheckCircleOutlined style={{ fontSize: 48, color: '#52c41a' }} />
            <p style={{ marginTop: 16, fontSize: 16 }}>
              导出成功！
            </p>
            <Space>
              <Button type="primary" icon={<DownloadOutlined />} onClick={handleDownload}>
                下载文件
              </Button>
              <Button onClick={() => {
                close();
                form.resetFields();
              }}>
                关闭
              </Button>
            </Space>
          </div>
        )}
      </Modal>
    </>
  );
};
