/**
 * 双因素认证设置页面
 * @author BaSui 😎
 * @date 2025-11-09
 */

import React, { useState, useEffect } from 'react';
import { Button, Input } from '@campus/shared/components';
import { authService } from '@campus/shared/services';
import './TwoFactorAuth.css';

interface TwoFactorSetupResponse {
  secret: string;
  qrCodeUrl: string;
  recoveryCodes: string[];
  message: string;
}

export const TwoFactorAuth: React.FC = () => {
  const [is2FAEnabled, setIs2FAEnabled] = useState(false);
  const [loading, setLoading] = useState(false);
  const [setupData, setSetupData] = useState<TwoFactorSetupResponse | null>(null);
  const [verificationCode, setVerificationCode] = useState('');
  const [password, setPassword] = useState('');
  const [showRecoveryCodes, setShowRecoveryCodes] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // 检查 2FA 状态
  useEffect(() => {
    check2FAStatus();
  }, []);

  const check2FAStatus = async () => {
    try {
      const response = await authService.check2FAStatus();
      if (response.code === 200) {
        setIs2FAEnabled(response.data || false);
      }
    } catch (error: any) {
      console.error('❌ 检查 2FA 状态失败:', error);
    }
  };

  // 启用 2FA
  const handleEnable2FA = async () => {
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await authService.enable2FA();
      if (response.code === 200 && response.data) {
        setSetupData(response.data);
        setSuccess('2FA 密钥生成成功！请扫描二维码并保存恢复码。');
      }
    } catch (error: any) {
      setError(error?.response?.data?.message || '启用 2FA 失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  // 验证 2FA 代码并完成启用
  const handleVerify2FA = async () => {
    if (!verificationCode || verificationCode.length !== 6) {
      setError('请输入 6 位验证码');
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await authService.verify2FA({ code: verificationCode });
      if (response.code === 200) {
        setSuccess('2FA 启用成功！🎉');
        setIs2FAEnabled(true);
        setSetupData(null);
        setVerificationCode('');
      } else {
        setError(response.message || '验证码错误，请重试');
      }
    } catch (error: any) {
      setError(error?.response?.data?.message || '验证失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  // 禁用 2FA
  const handleDisable2FA = async () => {
    if (!password) {
      setError('请输入密码以禁用 2FA');
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await authService.disable2FA({ password });
      if (response.code === 200) {
        setSuccess('2FA 已禁用');
        setIs2FAEnabled(false);
        setPassword('');
      } else {
        setError(response.message || '禁用失败');
      }
    } catch (error: any) {
      setError(error?.response?.data?.message || '禁用失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  // 重新生成恢复码
  const handleRegenerateRecoveryCodes = async () => {
    if (!password) {
      setError('请输入密码以重新生成恢复码');
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await authService.regenerateRecoveryCodes({ password });
      if (response.code === 200 && response.data) {
        setSetupData({
          secret: '',
          qrCodeUrl: '',
          recoveryCodes: response.data,
          message: '恢复码已重新生成',
        });
        setShowRecoveryCodes(true);
        setSuccess('恢复码重新生成成功！请妥善保存。');
        setPassword('');
      }
    } catch (error: any) {
      setError(error?.response?.data?.message || '重新生成失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="two-factor-auth">
      <h2 className="two-factor-auth__title">🔐 双因素认证（2FA）</h2>
      <p className="two-factor-auth__desc">
        双因素认证为您的账号提供额外的安全保护。启用后，登录时需要输入 6 位验证码。
      </p>

      {/* 错误提示 */}
      {error && (
        <div className="two-factor-auth__alert two-factor-auth__alert--error">
          ⚠️ {error}
        </div>
      )}

      {/* 成功提示 */}
      {success && (
        <div className="two-factor-auth__alert two-factor-auth__alert--success">
          ✅ {success}
        </div>
      )}

      {/* 2FA 未启用 */}
      {!is2FAEnabled && !setupData && (
        <div className="two-factor-auth__section">
          <p className="two-factor-auth__status">
            状态：<span className="two-factor-auth__status--disabled">未启用</span>
          </p>
          <Button
            type="primary"
            size="large"
            onClick={handleEnable2FA}
            loading={loading}
          >
            启用 2FA
          </Button>
        </div>
      )}

      {/* 2FA 设置中（显示 QR 码） */}
      {!is2FAEnabled && setupData && (
        <div className="two-factor-auth__setup">
          <h3 className="two-factor-auth__setup-title">📱 扫描二维码</h3>
          <p className="two-factor-auth__setup-desc">
            使用 Google Authenticator 或其他 TOTP 应用扫描下方二维码：
          </p>

          {/* QR 码 */}
          <div className="two-factor-auth__qr">
            <img src={setupData.qrCodeUrl} alt="2FA QR Code" />
          </div>

          {/* 手动输入密钥 */}
          <div className="two-factor-auth__secret">
            <p>或手动输入密钥：</p>
            <code>{setupData.secret}</code>
          </div>

          {/* 恢复码 */}
          <div className="two-factor-auth__recovery">
            <h4>🔑 恢复码（请妥善保存）</h4>
            <p className="two-factor-auth__recovery-desc">
              当您丢失 2FA 设备时，可以使用恢复码登录。每个恢复码只能使用一次。
            </p>
            <div className="two-factor-auth__recovery-codes">
              {setupData.recoveryCodes.map((code, index) => (
                <div key={index} className="two-factor-auth__recovery-code">
                  {code}
                </div>
              ))}
            </div>
          </div>

          {/* 验证码输入 */}
          <div className="two-factor-auth__verify">
            <h4>✅ 验证 2FA 代码</h4>
            <p>请输入 Google Authenticator 中显示的 6 位验证码：</p>
            <Input
              size="large"
              placeholder="请输入 6 位验证码"
              value={verificationCode}
              onChange={(e) => setVerificationCode(e.target.value)}
              maxLength={6}
            />
            <Button
              type="primary"
              size="large"
              onClick={handleVerify2FA}
              loading={loading}
              disabled={verificationCode.length !== 6}
            >
              验证并启用
            </Button>
          </div>
        </div>
      )}

      {/* 2FA 已启用 */}
      {is2FAEnabled && (
        <div className="two-factor-auth__section">
          <p className="two-factor-auth__status">
            状态：<span className="two-factor-auth__status--enabled">已启用 ✅</span>
          </p>

          {/* 重新生成恢复码 */}
          <div className="two-factor-auth__actions">
            <h4>🔄 重新生成恢复码</h4>
            <p>如果您丢失了恢复码，可以重新生成。旧的恢复码将失效。</p>
            <Input
              type="password"
              size="large"
              placeholder="请输入密码"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <Button
              type="default"
              size="large"
              onClick={handleRegenerateRecoveryCodes}
              loading={loading}
            >
              重新生成恢复码
            </Button>
          </div>

          {/* 显示新的恢复码 */}
          {showRecoveryCodes && setupData && setupData.recoveryCodes.length > 0 && (
            <div className="two-factor-auth__recovery">
              <h4>🔑 新的恢复码</h4>
              <div className="two-factor-auth__recovery-codes">
                {setupData.recoveryCodes.map((code, index) => (
                  <div key={index} className="two-factor-auth__recovery-code">
                    {code}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 禁用 2FA */}
          <div className="two-factor-auth__actions">
            <h4>🚫 禁用 2FA</h4>
            <p>禁用后，您的账号将失去双因素认证保护。</p>
            <Input
              type="password"
              size="large"
              placeholder="请输入密码"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <Button
              type="danger"
              size="large"
              onClick={handleDisable2FA}
              loading={loading}
            >
              禁用 2FA
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};

export default TwoFactorAuth;
