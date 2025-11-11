/**
 * 人机验证服务 - 让机器人无处可逃!🤖🚫
 * @author BaSui 😎
 * @date 2025-11-10
 * @description 提供图形验证码、滑块验证码生成和验证功能
 */

import { apiClient } from '../utils/apiClient';
import type {
  ApiResponseCaptchaResponse,
  ApiResponseSlideCaptchaResponse,
  ApiResponseBoolean,
  CaptchaResponse,
  SlideCaptchaResponse,
  SlideVerifyRequest,
} from '../api/models';

/**
 * 🎨 图形验证码服务
 */
export const imageCaptchaService = {
  /**
   * 生成图形验证码（4位数字+字母）
   * @returns {Promise<CaptchaResponse>} 验证码响应（captchaId + Base64图片）
   * @example
   * const { captchaId, imageBase64 } = await imageCaptchaService.generate();
   * // imageBase64: "data:image/png;base64,iVBORw0KGg..."
   */
  generate: async (): Promise<CaptchaResponse> => {
    const response = await apiClient.get<ApiResponseCaptchaResponse>('/api/captcha/image');

    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '❌ 生成图形验证码失败');
    }

    return response.data.data;
  },

  /**
   * 验证图形验证码
   * @param {string} captchaId - 验证码ID
   * @param {string} code - 用户输入的验证码
   * @returns {Promise<boolean>} 验证是否通过
   * @example
   * const isValid = await imageCaptchaService.verify(captchaId, '3F4A');
   * if (!isValid) {
   *   console.log('❌ 验证码错误!');
   * }
   */
  verify: async (captchaId: string, code: string): Promise<boolean> => {
    const response = await apiClient.post<ApiResponseBoolean>(
      '/api/captcha/image/verify',
      null,
      {
        params: { captchaId, code },
      }
    );

    return response.data.data === true;
  },
};

/**
 * 🧩 滑块验证码服务
 */
export const slideCaptchaService = {
  /**
   * 生成滑块验证码（简单版本，仅返回目标位置）
   * @returns {Promise<CaptchaResponse>} 滑块响应（slideId + 目标位置）
   * @deprecated 推荐使用 generateWithImage() 获取完整滑块图片
   */
  generate: async (): Promise<CaptchaResponse> => {
    const response = await apiClient.get<ApiResponseCaptchaResponse>('/api/captcha/slide');

    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '❌ 生成滑块验证码失败');
    }

    return response.data.data;
  },

  /**
   * 生成滑块验证码（完整版本，包含背景图、滑块图、Y轴位置）
   * @returns {Promise<SlideCaptchaResponse>} 滑块验证码响应
   * @example
   * const { slideId, backgroundImage, sliderImage, yPosition } = await slideCaptchaService.generateWithImage();
   * // backgroundImage: "data:image/png;base64,iVBORw0KGg..." (300x200背景图)
   * // sliderImage: "data:image/png;base64,iVBORw0KGg..." (50x50滑块图)
   */
  generateWithImage: async (): Promise<SlideCaptchaResponse> => {
    const response = await apiClient.get<ApiResponseSlideCaptchaResponse>('/api/captcha/slide/image');

    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '❌ 生成滑块验证码失败');
    }

    return response.data.data;
  },

  /**
   * 验证滑块验证码（简单版本，仅验证X轴位置）
   * @param {string} slideId - 滑块ID
   * @param {number} position - 用户滑动的X轴位置
   * @returns {Promise<boolean>} 验证是否通过（允许±5px误差）
   * @example
   * const isValid = await slideCaptchaService.verify(slideId, 120);
   */
  verify: async (slideId: string, position: number): Promise<boolean> => {
    const response = await apiClient.post<ApiResponseBoolean>(
      '/api/captcha/slide/verify',
      null,
      {
        params: { slideId, position },
      }
    );

    return response.data.data === true;
  },

  /**
   * 验证滑块验证码（完整版本，包含轨迹分析）
   * @param {SlideVerifyRequest} request - 验证请求（slideId + xPosition + track）
   * @returns {Promise<boolean>} 验证是否通过
   * @example
   * const track = [
   *   { x: 0, y: 0, t: 0 },
   *   { x: 10, y: 0, t: 100 },
   *   { x: 120, y: 0, t: 500 },
   * ];
   * const isValid = await slideCaptchaService.verifyWithTrack({
   *   slideId,
   *   xPosition: 120,
   *   track,
   * });
   */
  verifyWithTrack: async (request: SlideVerifyRequest): Promise<boolean> => {
    const response = await apiClient.post<ApiResponseBoolean>('/api/captcha/slide/verify/track', request);

    return response.data.data === true;
  },
};

/**
 * 🔐 统一验证服务（方便调用）
 */
export const captchaService = {
  /**
   * 图形验证码
   */
  image: imageCaptchaService,

  /**
   * 滑块验证码
   */
  slide: slideCaptchaService,
};

/**
 * 🎯 验证码Hook工具类型定义（供React组件使用）
 */
export interface CaptchaHookResult {
  /** 验证码ID */
  captchaId: string | null;
  /** 验证码图片（Base64） */
  imageBase64: string | null;
  /** 是否正在加载 */
  loading: boolean;
  /** 错误信息 */
  error: string | null;
  /** 刷新验证码 */
  refresh: () => Promise<void>;
  /** 验证验证码 */
  verify: (code: string) => Promise<boolean>;
}

export interface SlideCaptchaHookResult {
  /** 滑块ID */
  slideId: string | null;
  /** 背景图片（Base64） */
  backgroundImage: string | null;
  /** 滑块图片（Base64） */
  sliderImage: string | null;
  /** Y轴位置 */
  yPosition: number | null;
  /** 是否正在加载 */
  loading: boolean;
  /** 错误信息 */
  error: string | null;
  /** 刷新滑块 */
  refresh: () => Promise<void>;
  /** 验证滑块 */
  verify: (position: number, track?: SlideVerifyRequest['track']) => Promise<boolean>;
}

// ========== 方案B：统一验证码验证接口（新增 - BaSui 2025-11-11） ==========

/**
 * 导入方案B相关类型
 */
import type {
  CaptchaVerifyRequest,
  CaptchaVerifyResponse,
  ApiResponse,
} from '../types/captcha';

/**
 * 🎯 统一验证码验证服务（方案B - 推荐）
 *
 * 工作流程：
 * 1. 用户完成验证码验证（滑动/输入/旋转/点击）
 * 2. 前端调用此接口验证验证码
 * 3. 验证成功后获得验证码通行证（captchaToken，有效期60秒）
 * 4. 登录时携带captchaToken，无需再次验证验证码
 *
 * @param {CaptchaVerifyRequest} request - 验证请求
 * @returns {Promise<CaptchaVerifyResponse>} 验证码通行证
 *
 * @example
 * // 图形验证码
 * const response = await verifyCaptcha({
 *   type: 'image',
 *   captchaId: 'xxx',
 *   captchaCode: '3F4A',
 * });
 * console.log('验证码通行证:', response.captchaToken);
 *
 * @example
 * // 滑块验证码
 * const response = await verifyCaptcha({
 *   type: 'slider',
 *   captchaId: 'xxx',
 *   slidePosition: 120,
 * });
 * console.log('验证码通行证:', response.captchaToken);
 */
export const verifyCaptcha = async (
  request: CaptchaVerifyRequest
): Promise<CaptchaVerifyResponse> => {
  try {
    console.log('[verifyCaptcha] 🔐 开始验证验证码:', request.type);

    const response = await apiClient.post<ApiResponse<CaptchaVerifyResponse>>(
      '/api/captcha/verify',
      request
    );

    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '❌ 验证码验证失败');
    }

    console.log('[verifyCaptcha] ✅ 验证码验证成功，获得通行证:', response.data.data.captchaToken);

    return response.data.data;
  } catch (error: any) {
    console.error('[verifyCaptcha] ❌ 验证码验证失败:', error);
    throw new Error(error?.response?.data?.message || error?.message || '验证码验证失败，请重试');
  }
};

/**
 * 🔐 导出统一验证服务（方便调用）
 */
export const unifiedCaptchaService = {
  /**
   * 验证验证码（方案B - 推荐）
   */
  verify: verifyCaptcha,
};
