/**
 * 验证码相关类型定义
 * @author BaSui 😎
 * @date 2025-11-11
 * @description 方案B（验证码单独验证）相关类型
 */

/**
 * 验证码类型
 */
export type CaptchaType = 'image' | 'slider' | 'rotate' | 'click';

/**
 * 点击坐标点
 */
export interface ClickPoint {
  x: number;
  y: number;
}

/**
 * 滑块轨迹点
 */
export interface TrackPoint {
  x: number;
  y: number;
  t: number;
}

/**
 * 统一验证码验证请求
 */
export interface CaptchaVerifyRequest {
  /** 验证码类型 */
  type: CaptchaType;
  /** 验证码ID（通用字段） */
  captchaId: string;
  /** 图形验证码输入（4位字符） */
  captchaCode?: string;
  /** 滑块位置（X轴坐标） */
  slidePosition?: number;
  /** 滑块轨迹（可选） */
  track?: TrackPoint[];
  /** 旋转角度（0-360度） */
  rotateAngle?: number;
  /** 点击坐标列表 */
  clickPoints?: ClickPoint[];
}

/**
 * 验证码验证响应
 */
export interface CaptchaVerifyResponse {
  /** 验证码通行证（临时token，有效期60秒） */
  captchaToken: string;
  /** 过期时间（秒） */
  expiresIn: number;
  /** 验证成功提示 */
  message: string;
}

/**
 * API响应包装类型
 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}
