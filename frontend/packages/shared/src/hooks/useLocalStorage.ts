/**
 * useLocalStorage Hook - 本地存储大师！💾
 * @author BaSui 😎
 * @description LocalStorage 封装 Hook，支持自动序列化、类型安全
 */

import { useState, useEffect, useCallback } from 'react';

/**
 * useLocalStorage Hook
 *
 * @description
 * LocalStorage 封装 Hook，提供类型安全的本地存储操作。
 * 自动处理 JSON 序列化和反序列化，支持初始值和错误处理。
 *
 * @template T 存储值的类型
 * @param key 存储键名
 * @param initialValue 初始值
 * @returns [value, setValue, removeValue] 值、设置函数、删除函数
 *
 * @example
 * ```tsx
 * // 存储用户偏好设置
 * function ThemeSelector() {
 *   const [theme, setTheme, removeTheme] = useLocalStorage('theme', 'light');
 *
 *   return (
 *     <div>
 *       <p>当前主题: {theme}</p>
 *       <Button onClick={() => setTheme('dark')}>切换到暗色</Button>
 *       <Button onClick={() => setTheme('light')}>切换到亮色</Button>
 *       <Button onClick={removeTheme}>重置主题</Button>
 *     </div>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 存储用户数据
 * interface UserProfile {
 *   name: string;
 *   email: string;
 *   avatar: string;
 * }
 *
 * function ProfileEditor() {
 *   const [profile, setProfile] = useLocalStorage<UserProfile>('userProfile', {
 *     name: '',
 *     email: '',
 *     avatar: '',
 *   });
 *
 *   return (
 *     <Form>
 *       <Input
 *         value={profile.name}
 *         onChange={(e) => setProfile({ ...profile, name: e.target.value })}
 *       />
 *       <Input
 *         value={profile.email}
 *         onChange={(e) => setProfile({ ...profile, email: e.target.value })}
 *       />
 *     </Form>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 存储购物车数据
 * interface CartItem {
 *   id: string;
 *   name: string;
 *   quantity: number;
 *   price: number;
 * }
 *
 * function ShoppingCart() {
 *   const [cart, setCart, clearCart] = useLocalStorage<CartItem[]>('cart', []);
 *
 *   const addItem = (item: CartItem) => {
 *     setCart([...cart, item]);
 *   };
 *
 *   const removeItem = (id: string) => {
 *     setCart(cart.filter((item) => item.id !== id));
 *   };
 *
 *   return (
 *     <div>
 *       <h2>购物车 ({cart.length})</h2>
 *       {cart.map((item) => (
 *         <div key={item.id}>
 *           {item.name} x {item.quantity} - ¥{item.price}
 *           <Button onClick={() => removeItem(item.id)}>删除</Button>
 *         </div>
 *       ))}
 *       <Button onClick={clearCart}>清空购物车</Button>
 *     </div>
 *   );
 * }
 * ```
 */
export const useLocalStorage = <T,>(
  key: string,
  initialValue: T
): [T, (value: T | ((prevValue: T) => T)) => void, () => void] => {
  /**
   * 从 LocalStorage 读取初始值
   */
  const readValue = useCallback((): T => {
    // 服务端渲染时返回初始值
    if (typeof window === 'undefined') {
      return initialValue;
    }

    try {
      const item = window.localStorage.getItem(key);

      // 如果存在，解析 JSON 并返回
      if (item) {
        return JSON.parse(item) as T;
      }

      // 如果不存在，返回初始值
      return initialValue;
    } catch (error) {
      console.warn(`读取 LocalStorage 键 "${key}" 失败:`, error);
      return initialValue;
    }
  }, [key, initialValue]);

  // 状态初始化
  const [storedValue, setStoredValue] = useState<T>(readValue);

  /**
   * 设置值到 State 和 LocalStorage
   */
  const setValue = useCallback(
    (value: T | ((prevValue: T) => T)) => {
      // 服务端渲染时不执行
      if (typeof window === 'undefined') {
        console.warn('useLocalStorage 在服务端无法使用');
        return;
      }

      try {
        // 支持函数更新
        const newValue = value instanceof Function ? value(storedValue) : value;

        // 保存到 State
        setStoredValue(newValue);

        // 保存到 LocalStorage
        window.localStorage.setItem(key, JSON.stringify(newValue));

        // 触发自定义事件（用于跨组件同步）
        window.dispatchEvent(
          new CustomEvent('local-storage', {
            detail: { key, newValue },
          })
        );
      } catch (error) {
        console.error(`设置 LocalStorage 键 "${key}" 失败:`, error);
      }
    },
    [key, storedValue]
  );

  /**
   * 删除 LocalStorage 中的值
   */
  const removeValue = useCallback(() => {
    // 服务端渲染时不执行
    if (typeof window === 'undefined') {
      console.warn('useLocalStorage 在服务端无法使用');
      return;
    }

    try {
      // 重置为初始值
      setStoredValue(initialValue);

      // 从 LocalStorage 删除
      window.localStorage.removeItem(key);

      // 触发自定义事件（用于跨组件同步）
      window.dispatchEvent(
        new CustomEvent('local-storage', {
          detail: { key, newValue: undefined },
        })
      );
    } catch (error) {
      console.error(`删除 LocalStorage 键 "${key}" 失败:`, error);
    }
  }, [key, initialValue]);

  /**
   * 监听 LocalStorage 变化（跨标签页/组件同步）
   */
  useEffect(() => {
    // 服务端渲染时不执行
    if (typeof window === 'undefined') {
      return;
    }

    /**
     * 处理 storage 事件（跨标签页）
     */
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === key && e.newValue !== null) {
        try {
          setStoredValue(JSON.parse(e.newValue) as T);
        } catch (error) {
          console.error(`解析 LocalStorage 变化失败:`, error);
        }
      }
    };

    /**
     * 处理自定义事件（同一标签页内跨组件）
     */
    const handleCustomEvent = (e: Event) => {
      const customEvent = e as CustomEvent<{ key: string; newValue: T | undefined }>;
      if (customEvent.detail.key === key) {
        if (customEvent.detail.newValue === undefined) {
          setStoredValue(initialValue);
        } else {
          setStoredValue(customEvent.detail.newValue);
        }
      }
    };

    // 监听跨标签页变化
    window.addEventListener('storage', handleStorageChange);

    // 监听同一标签页内的变化
    window.addEventListener('local-storage', handleCustomEvent);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
      window.removeEventListener('local-storage', handleCustomEvent);
    };
  }, [key, initialValue]);

  return [storedValue, setValue, removeValue];
};

export default useLocalStorage;
