/**
 * 密码加密工具
 * 
 * 使用 AES-256-CBC 算法对敏感数据进行加密传输
 * 
 * @module utils/crypto
 * @author BaSui 😎
 * @date 2025-11-06
 */

import CryptoJS from 'crypto-js';

/**
 * 获取加密密钥
 * 
 * @returns 加密密钥
 * @throws {Error} 密钥未配置时抛出异常
 */
const getEncryptKey = (): string => {
  const key = import.meta.env.VITE_ENCRYPT_KEY;
  
  if (!key) {
    console.error('❌ 加密密钥未配置，请检查环境变量 VITE_ENCRYPT_KEY');
    throw new Error('加密密钥未配置，请检查环境变量');
  }
  
  if (key.length < 32) {
    console.warn('⚠️ 加密密钥长度不足32字节，安全性较低');
  }
  
  return key;
};

/**
 * 加密密码（带时间戳，防重放攻击）
 * 
 * @param password 明文密码
 * @returns 加密后的Base64字符串
 * @throws {Error} 加密失败时抛出异常
 * 
 * @example
 * ```typescript
 * const encrypted = encryptPassword('admin123');
 * // 返回: "U2FsdGVkX1+8xZq..."
 * ```
 */
export function encryptPassword(password: string): string {
  if (!password || password.trim().length === 0) {
    throw new Error('密码不能为空');
  }
  
  try {
    const key = getEncryptKey();
    
    // 🛡️ 防重放攻击：添加时间戳
    const timestamp = Date.now();
    const payload = `${timestamp}|${password}`;
    
    const encrypted = CryptoJS.AES.encrypt(payload, key).toString();
    
    if (!encrypted) {
      throw new Error('加密结果为空');
    }
    
    return encrypted;
  } catch (error) {
    console.error('❌ 密码加密失败:', error);
    
    if (error instanceof Error && error.message.includes('环境变量')) {
      throw error;
    }
    
    throw new Error('密码加密失败，请重试');
  }
}

/**
 * 加密密码（不带时间戳，用于兼容或测试）
 * 
 * @param password 明文密码
 * @returns 加密后的Base64字符串
 * @deprecated 推荐使用 encryptPassword（带时间戳）
 */
export function encryptPasswordSimple(password: string): string {
  if (!password || password.trim().length === 0) {
    throw new Error('密码不能为空');
  }
  
  try {
    const key = getEncryptKey();
    const encrypted = CryptoJS.AES.encrypt(password, key).toString();
    
    if (!encrypted) {
      throw new Error('加密结果为空');
    }
    
    return encrypted;
  } catch (error) {
    console.error('❌ 密码加密失败:', error);
    throw new Error('密码加密失败，请重试');
  }
}

/**
 * 解密密码（仅用于测试和调试）
 * 
 * ⚠️ 注意：生产环境不应该在前端解密密码
 * 
 * @param encryptedPassword 加密的Base64字符串
 * @returns 明文密码
 * @throws {Error} 解密失败时抛出异常
 * 
 * @example
 * ```typescript
 * const decrypted = decryptPassword('U2FsdGVkX1+8xZq...');
 * // 返回: "admin123"
 * ```
 */
export function decryptPassword(encryptedPassword: string): string {
  if (!encryptedPassword || encryptedPassword.trim().length === 0) {
    throw new Error('密文不能为空');
  }
  
  try {
    const key = getEncryptKey();
    const decrypted = CryptoJS.AES.decrypt(encryptedPassword, key);
    const plainText = decrypted.toString(CryptoJS.enc.Utf8);
    
    if (!plainText || plainText.length === 0) {
      throw new Error('解密结果为空，可能密文格式错误或密钥不匹配');
    }
    
    return plainText;
  } catch (error) {
    console.error('❌ 密码解密失败:', error);
    
    if (error instanceof Error) {
      throw error;
    }
    
    throw new Error('密码解密失败，请检查密文格式或密钥配置');
  }
}

/**
 * 生成随机密钥（用于初始化配置）
 * 
 * @returns 32字节的随机密钥（Base64编码）
 * 
 * @example
 * ```typescript
 * const key = generateKey();
 * // 返回: "7xK9mP4nL2vB8qW5tR6uY3sA1cD0fE2g..."
 * ```
 */
export function generateKey(): string {
  return CryptoJS.lib.WordArray.random(32).toString(CryptoJS.enc.Base64);
}

/**
 * 验证加密密钥配置是否正确
 * 
 * @returns true=配置正确, false=配置错误
 */
export function validateEncryptKey(): boolean {
  try {
    const key = import.meta.env.VITE_ENCRYPT_KEY;
    
    if (!key) {
      console.error('❌ 加密密钥未配置');
      return false;
    }
    
    if (key.length < 32) {
      console.warn('⚠️ 加密密钥长度不足32字节');
      return false;
    }
    
    // 测试加密解密是否正常
    const testPassword = 'test123';
    const encrypted = CryptoJS.AES.encrypt(testPassword, key).toString();
    const decrypted = CryptoJS.AES.decrypt(encrypted, key).toString(CryptoJS.enc.Utf8);
    
    if (decrypted !== testPassword) {
      console.error('❌ 加密解密测试失败');
      return false;
    }
    
    console.log('✅ 加密密钥配置正确');
    return true;
  } catch (error) {
    console.error('❌ 加密密钥验证失败:', error);
    return false;
  }
}
