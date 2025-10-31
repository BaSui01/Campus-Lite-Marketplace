/**
 * 认证布局组件
 * @author BaSui 😎
 * @description 用于登录、注册页面的布局
 */

import { Outlet } from 'react-router-dom';
import './AuthLayout.css';

/**
 * 认证布局组件
 */
const AuthLayout = () => {
  return (
    <div className="auth-layout">
      {/* 背景装饰 */}
      <div className="auth-layout__bg">
        <div className="auth-layout__bg-circle auth-layout__bg-circle--1"></div>
        <div className="auth-layout__bg-circle auth-layout__bg-circle--2"></div>
        <div className="auth-layout__bg-circle auth-layout__bg-circle--3"></div>
      </div>

      {/* 内容区 */}
      <div className="auth-layout__content">
        {/* Logo */}
        <div className="auth-layout__logo">
          <span className="auth-layout__logo-icon">🎓</span>
          <span className="auth-layout__logo-text">校园轻享集市</span>
        </div>

        {/* 表单区域 */}
        <div className="auth-layout__form-wrapper">
          <Outlet />
        </div>

        {/* 底部信息 */}
        <div className="auth-layout__footer">
          <p className="auth-layout__footer-text">
            © 2025 校园轻享集市 | Made with ❤️ by BaSui 😎
          </p>
        </div>
      </div>
    </div>
  );
};

export default AuthLayout;
