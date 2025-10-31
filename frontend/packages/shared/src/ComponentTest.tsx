/**
 * 组件测试文件 - 验证组件导出是否正常
 * @author BaSui 😎
 */

import React from 'react';
import {
  Button,
  Input,
  Loading,
  toast,
  Modal,
  Form,
  FormItem,
} from './components';

/**
 * 测试组件 - 演示所有 P0 组件的基础用法
 */
const ComponentTest: React.FC = () => {
  const [visible, setVisible] = React.useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    toast.success('表单提交成功！');
  };

  return (
    <div style={{ padding: '24px' }}>
      <h1>🎨 校园轻享集市 - 公共组件库测试</h1>

      {/* Button 组件测试 */}
      <section>
        <h2>1️⃣ Button 按钮组件</h2>
        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
          <Button type="primary">主要按钮</Button>
          <Button type="default">默认按钮</Button>
          <Button type="danger">危险按钮</Button>
          <Button type="link">链接按钮</Button>
          <Button type="primary" size="large">大号按钮</Button>
          <Button type="primary" size="small">小号按钮</Button>
          <Button type="primary" loading>加载中...</Button>
          <Button type="primary" disabled>禁用按钮</Button>
        </div>
      </section>

      {/* Input 组件测试 */}
      <section style={{ marginTop: '32px' }}>
        <h2>2️⃣ Input 输入框组件</h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', maxWidth: '400px' }}>
          <Input placeholder="请输入文本" />
          <Input type="password" placeholder="请输入密码" />
          <Input type="number" placeholder="请输入数字" />
          <Input type="search" placeholder="搜索" />
          <Input allowClear placeholder="带清除按钮" />
          <Input error errorMessage="输入内容有误！" placeholder="错误状态" />
          <Input size="large" placeholder="大号输入框" />
          <Input size="small" placeholder="小号输入框" />
        </div>
      </section>

      {/* Loading 组件测试 */}
      <section style={{ marginTop: '32px' }}>
        <h2>3️⃣ Loading 加载组件</h2>
        <div style={{ display: 'flex', gap: '32px', flexWrap: 'wrap' }}>
          <div style={{ width: '200px', height: '100px', border: '1px dashed #ccc' }}>
            <Loading type="spinner" tip="加载中..." />
          </div>
          <div style={{ width: '200px', height: '100px', border: '1px dashed #ccc' }}>
            <Loading type="skeleton" />
          </div>
          <div style={{ width: '200px', height: '100px', border: '1px dashed #ccc' }}>
            <Loading type="spinner" size="large" />
          </div>
        </div>
      </section>

      {/* Toast 组件测试 */}
      <section style={{ marginTop: '32px' }}>
        <h2>4️⃣ Toast 消息提示</h2>
        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
          <Button onClick={() => toast.success('操作成功！')}>成功提示</Button>
          <Button onClick={() => toast.error('操作失败！')}>错误提示</Button>
          <Button onClick={() => toast.warning('请注意！')}>警告提示</Button>
          <Button onClick={() => toast.info('普通消息')}>信息提示</Button>
        </div>
      </section>

      {/* Modal 组件测试 */}
      <section style={{ marginTop: '32px' }}>
        <h2>5️⃣ Modal 模态框</h2>
        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
          <Button onClick={() => setVisible(true)}>打开模态框</Button>
        </div>
        <Modal
          visible={visible}
          title="确认操作"
          onOk={() => {
            toast.success('确认成功！');
            setVisible(false);
          }}
          onCancel={() => setVisible(false)}
        >
          <p>这是一个测试模态框！</p>
          <p>你确定要执行这个操作吗？</p>
        </Modal>
      </section>

      {/* Form 组件测试 */}
      <section style={{ marginTop: '32px' }}>
        <h2>6️⃣ Form 表单组件</h2>
        <Form layout="vertical" onSubmit={handleSubmit} style={{ maxWidth: '400px' }}>
          <FormItem label="用户名" name="username" required>
            <Input placeholder="请输入用户名" />
          </FormItem>

          <FormItem label="密码" name="password" required>
            <Input type="password" placeholder="请输入密码" />
          </FormItem>

          <FormItem label="邮箱" name="email" help="请输入有效的邮箱地址">
            <Input type="email" placeholder="请输入邮箱" />
          </FormItem>

          <FormItem>
            <div style={{ display: 'flex', gap: '12px' }}>
              <Button htmlType="submit" type="primary">提交</Button>
              <Button htmlType="reset">重置</Button>
            </div>
          </FormItem>
        </Form>
      </section>

      <footer style={{ marginTop: '64px', paddingTop: '24px', borderTop: '1px solid #f0f0f0' }}>
        <p>🎉 所有 P0 基础组件测试完成！</p>
        <p>👉 下一步：开发 P1 高级组件（Dropdown、Table、Pagination 等）</p>
      </footer>
    </div>
  );
};

export default ComponentTest;
