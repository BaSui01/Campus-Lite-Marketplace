/**
 * 🔐 BaSui 的验证工具函数
 * @description 表单验证、数据校验
 */

// ==================== 正则表达式 ====================

/** 手机号正则 */
const PHONE_REGEX = /^1[3-9]\d{9}$/;

/** 邮箱正则 */
const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

/** 身份证号正则（18位） */
const ID_CARD_REGEX = /^\d{17}[\dXx]$/;

/** 密码正则（8-20位，包含字母和数字） */
const PASSWORD_REGEX = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*?&]{8,20}$/;

/** URL 正则 */
const URL_REGEX = /^(https?|ftp):\/\/[^\s/$.?#].[^\s]*$/i;

/** 用户名正则（4-20位字母、数字、下划线） */
const USERNAME_REGEX = /^[a-zA-Z0-9_]{4,20}$/;

// ==================== 基础验证 ====================

/**
 * 验证手机号
 */
export const isValidPhone = (phone: string): boolean => {
  return PHONE_REGEX.test(phone);
};

/**
 * 验证邮箱
 */
export const isValidEmail = (email: string): boolean => {
  return EMAIL_REGEX.test(email);
};

/**
 * 验证身份证号
 */
export const isValidIdCard = (idCard: string): boolean => {
  if (!ID_CARD_REGEX.test(idCard)) return false;

  // 校验位验证
  const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
  const checkCodes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];

  let sum = 0;
  for (let i = 0; i < 17; i++) {
    const char = idCard[i];
    const weight = weights[i];
    if (!char || weight === undefined) return false; // 🛡️ 严格检查数组访问
    sum += parseInt(char) * weight;
  }

  const checkCode = checkCodes[sum % 11];
  const lastChar = idCard[17];
  // 🛡️ 严格检查数组访问和返回值
  if (!checkCode || !lastChar) return false;
  return lastChar.toUpperCase() === checkCode;
};

/**
 * 验证密码（8-20位，包含字母和数字）
 */
export const isValidPassword = (password: string): boolean => {
  return PASSWORD_REGEX.test(password);
};

/**
 * 验证 URL
 */
export const isValidUrl = (url: string): boolean => {
  return URL_REGEX.test(url);
};

/**
 * 验证用户名（4-20位字母、数字、下划线）
 */
export const isValidUsername = (username: string): boolean => {
  return USERNAME_REGEX.test(username);
};

// ==================== 高级验证 ====================

/**
 * 验证是否为空
 */
export const isEmpty = (value: any): boolean => {
  if (value === null || value === undefined) return true;
  if (typeof value === 'string') return value.trim() === '';
  if (Array.isArray(value)) return value.length === 0;
  if (typeof value === 'object') return Object.keys(value).length === 0;
  return false;
};

/**
 * 验证字符串长度
 */
export const isValidLength = (str: string, min: number, max: number): boolean => {
  const len = str.length;
  return len >= min && len <= max;
};

/**
 * 验证数字范围
 */
export const isInRange = (num: number, min: number, max: number): boolean => {
  return num >= min && num <= max;
};

/**
 * 验证文件类型
 */
export const isValidFileType = (file: File, allowedTypes: string[]): boolean => {
  const fileType = file.type.toLowerCase();
  const fileExtension = file.name.split('.').pop()?.toLowerCase();

  return allowedTypes.some(type => {
    type = type.toLowerCase();
    // 支持 MIME 类型（如 image/jpeg）
    if (type.includes('/')) {
      return fileType === type;
    }
    // 支持扩展名（如 jpg）
    return fileExtension === type.replace('.', '');
  });
};

/**
 * 验证文件大小
 */
export const isValidFileSize = (file: File, maxSize: number): boolean => {
  return file.size <= maxSize;
};

// ==================== 业务验证 ====================

/**
 * 验证价格（大于0，最多2位小数）
 */
export const isValidPrice = (price: number): boolean => {
  if (price <= 0) return false;
  const decimalPart = String(price).split('.')[1];
  return !decimalPart || decimalPart.length <= 2;
};

/**
 * 验证积分（必须为正整数）
 */
export const isValidPoints = (points: number): boolean => {
  return Number.isInteger(points) && points > 0;
};

/**
 * 验证验证码（6位数字）
 */
export const isValidCode = (code: string): boolean => {
  return /^\d{6}$/.test(code);
};
