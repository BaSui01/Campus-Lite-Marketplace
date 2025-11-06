/**
 * 加密工具单元测试
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */

import { describe, it, expect, beforeAll } from 'vitest';
import { encryptPassword, decryptPassword, generateKey, validateEncryptKey } from './crypto';

// 设置测试环境变量
beforeAll(() => {
  // @ts-ignore - 设置测试用的环境变量
  import.meta.env.VITE_ENCRYPT_KEY = 'dev-test-key-32-bytes-length!';
});

describe('crypto - 加密工具测试', () => {
  describe('encryptPassword - 加密密码', () => {
    it('应该正确加密密码', () => {
      const password = 'admin123';
      const encrypted = encryptPassword(password);
      
      expect(encrypted).toBeTruthy();
      expect(encrypted).not.toBe(password);
      expect(typeof encrypted).toBe('string');
      expect(encrypted.length).toBeGreaterThan(20);
    });

    it('空密码应该抛出异常', () => {
      expect(() => encryptPassword('')).toThrow('密码不能为空');
      expect(() => encryptPassword('   ')).toThrow('密码不能为空');
    });

    it('多次加密同一密码应该返回不同密文（因为有随机IV）', () => {
      const password = 'admin123';
      const encrypted1 = encryptPassword(password);
      const encrypted2 = encryptPassword(password);
      
      // crypto-js的AES默认使用随机IV，所以每次加密结果不同
      expect(encrypted1).not.toBe(encrypted2);
    });
  });

  describe('decryptPassword - 解密密码', () => {
    it('应该正确解密密码', () => {
      const password = 'admin123';
      const encrypted = encryptPassword(password);
      const decrypted = decryptPassword(encrypted);
      
      expect(decrypted).toBe(password);
    });

    it('空密文应该抛出异常', () => {
      expect(() => decryptPassword('')).toThrow('密文不能为空');
      expect(() => decryptPassword('   ')).toThrow('密文不能为空');
    });

    it('无效密文应该抛出异常', () => {
      expect(() => decryptPassword('invalid-base64')).toThrow();
    });

    it('应该正确处理特殊字符密码', () => {
      const specialPasswords = [
        'abc@123!',
        'pass#$%word',
        '中文密码123',
        'emoji😊pass',
      ];

      specialPasswords.forEach((password) => {
        const encrypted = encryptPassword(password);
        const decrypted = decryptPassword(encrypted);
        expect(decrypted).toBe(password);
      });
    });
  });

  describe('generateKey - 生成随机密钥', () => {
    it('应该生成32字节的随机密钥', () => {
      const key = generateKey();
      
      expect(key).toBeTruthy();
      expect(typeof key).toBe('string');
      expect(key.length).toBeGreaterThan(30);
    });

    it('每次生成的密钥应该不同', () => {
      const key1 = generateKey();
      const key2 = generateKey();
      
      expect(key1).not.toBe(key2);
    });
  });

  describe('validateEncryptKey - 验证密钥配置', () => {
    it('应该验证密钥配置正确', () => {
      const result = validateEncryptKey();
      expect(result).toBe(true);
    });
  });

  describe('加密解密完整流程', () => {
    it('应该完整测试加密→解密流程', () => {
      const testPasswords = [
        'short',
        'admin123',
        'very-long-password-with-special-chars-!@#$%^&*()',
        '123456',
        'Test@2023!',
      ];

      testPasswords.forEach((password) => {
        const encrypted = encryptPassword(password);
        const decrypted = decryptPassword(encrypted);
        
        expect(decrypted).toBe(password);
        expect(encrypted).not.toBe(password);
      });
    });
  });

  describe('边界条件测试', () => {
    it('应该处理最短密码（1个字符）', () => {
      const password = 'a';
      const encrypted = encryptPassword(password);
      const decrypted = decryptPassword(encrypted);
      
      expect(decrypted).toBe(password);
    });

    it('应该处理超长密码（1000个字符）', () => {
      const password = 'a'.repeat(1000);
      const encrypted = encryptPassword(password);
      const decrypted = decryptPassword(encrypted);
      
      expect(decrypted).toBe(password);
    });

    it('应该处理纯数字密码', () => {
      const password = '123456789';
      const encrypted = encryptPassword(password);
      const decrypted = decryptPassword(encrypted);
      
      expect(decrypted).toBe(password);
    });

    it('应该处理纯符号密码', () => {
      const password = '!@#$%^&*()';
      const encrypted = encryptPassword(password);
      const decrypted = decryptPassword(encrypted);
      
      expect(decrypted).toBe(password);
    });
  });
});
