import { describe, it, expect } from 'vitest';
import { PERMISSION_CODES, ADMIN_DEFAULT_PERMISSIONS } from '../permissions';

describe('权限常量', () => {
  it('默认权限应全部来自 PERMISSION_CODES 且无重复', () => {
    const allCodes = Object.values(PERMISSION_CODES);
    const uniqueDefaults = new Set(ADMIN_DEFAULT_PERMISSIONS);

    expect(ADMIN_DEFAULT_PERMISSIONS.length).toBe(uniqueDefaults.size);
    expect(ADMIN_DEFAULT_PERMISSIONS.every((code) => allCodes.includes(code))).toBe(true);
  });

  it('PERMISSION_CODES 不应为空', () => {
    expect(Object.keys(PERMISSION_CODES).length).toBeGreaterThan(0);
  });
});
