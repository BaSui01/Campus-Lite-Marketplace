/**
 * useForm Hook - 表单状态管理大师！📝
 * @author BaSui 😎
 * @description 管理表单状态、验证、提交等功能
 */

import { useState, useCallback, useMemo } from 'react';

/**
 * 表单字段值类型
 */
export type FormValues = Record<string, any>;

/**
 * 表单字段错误类型
 */
export type FormErrors = Record<string, string | undefined>;

/**
 * 表单字段触碰状态类型
 */
export type FormTouched = Record<string, boolean | undefined>;

/**
 * 验证规则函数类型
 */
export type ValidatorFn = (value: any, values: FormValues) => string | undefined;

/**
 * 验证规则接口
 */
export interface ValidationRule {
  /**
   * 是否必填
   */
  required?: boolean | string;

  /**
   * 最小长度
   */
  minLength?: number | { value: number; message?: string };

  /**
   * 最大长度
   */
  maxLength?: number | { value: number; message?: string };

  /**
   * 最小值
   */
  min?: number | { value: number; message?: string };

  /**
   * 最大值
   */
  max?: number | { value: number; message?: string };

  /**
   * 正则表达式验证
   */
  pattern?: RegExp | { value: RegExp; message?: string };

  /**
   * 自定义验证函数
   */
  validate?: ValidatorFn | ValidatorFn[];
}

/**
 * 表单字段配置接口
 */
export type FormConfig<T extends FormValues = FormValues> = {
  [K in keyof T]?: ValidationRule;
};

/**
 * useForm 配置接口
 */
export interface UseFormOptions<T extends FormValues = FormValues> {
  /**
   * 初始值
   */
  initialValues: T;

  /**
   * 验证规则
   */
  validationRules?: FormConfig<T>;

  /**
   * 提交处理函数
   */
  onSubmit?: (values: T) => void | Promise<void>;

  /**
   * 验证模式
   * - onChange: 值改变时验证
   * - onBlur: 失焦时验证
   * - onSubmit: 提交时验证
   * @default 'onBlur'
   */
  validateMode?: 'onChange' | 'onBlur' | 'onSubmit';
}

/**
 * useForm 返回值接口
 */
export interface UseFormResult<T extends FormValues = FormValues> {
  /**
   * 表单值
   */
  values: T;

  /**
   * 表单错误
   */
  errors: FormErrors;

  /**
   * 表单触碰状态
   */
  touched: FormTouched;

  /**
   * 是否正在提交
   */
  isSubmitting: boolean;

  /**
   * 是否有错误
   */
  isValid: boolean;

  /**
   * 是否已修改
   */
  isDirty: boolean;

  /**
   * 设置字段值
   */
  setFieldValue: (name: keyof T, value: any) => void;

  /**
   * 设置字段错误
   */
  setFieldError: (name: keyof T, error: string | undefined) => void;

  /**
   * 设置字段触碰状态
   */
  setFieldTouched: (name: keyof T, touched: boolean) => void;

  /**
   * 获取字段属性（用于表单控件）
   */
  getFieldProps: (name: keyof T) => {
    name: keyof T;
    value: any;
    onChange: (e: React.ChangeEvent<any>) => void;
    onBlur: (e: React.FocusEvent<any>) => void;
    error: string | undefined;
  };

  /**
   * 验证单个字段
   */
  validateField: (name: keyof T) => Promise<string | undefined>;

  /**
   * 验证所有字段
   */
  validateForm: () => Promise<FormErrors>;

  /**
   * 处理提交
   */
  handleSubmit: (e?: React.FormEvent) => Promise<void>;

  /**
   * 重置表单
   */
  reset: () => void;

  /**
   * 设置多个值
   */
  setValues: (values: Partial<T>) => void;

  /**
   * 设置多个错误
   */
  setErrors: (errors: FormErrors) => void;
}

/**
 * 执行验证
 */
const runValidation = (value: any, rule: ValidationRule, allValues: FormValues): string | undefined => {
  // 必填验证
  if (rule.required) {
    const isEmpty = value === undefined || value === null || value === '' || (Array.isArray(value) && value.length === 0);
    if (isEmpty) {
      return typeof rule.required === 'string' ? rule.required : '此字段为必填项';
    }
  }

  // 如果值为空且非必填，跳过其他验证
  if (value === undefined || value === null || value === '') {
    return undefined;
  }

  // 最小长度验证
  if (rule.minLength) {
    const config = typeof rule.minLength === 'number' ? { value: rule.minLength } : rule.minLength;
    if (String(value).length < config.value) {
      return config.message || `最少 ${config.value} 个字符`;
    }
  }

  // 最大长度验证
  if (rule.maxLength) {
    const config = typeof rule.maxLength === 'number' ? { value: rule.maxLength } : rule.maxLength;
    if (String(value).length > config.value) {
      return config.message || `最多 ${config.value} 个字符`;
    }
  }

  // 最小值验证
  if (rule.min !== undefined) {
    const config = typeof rule.min === 'number' ? { value: rule.min } : rule.min;
    if (Number(value) < config.value) {
      return config.message || `最小值为 ${config.value}`;
    }
  }

  // 最大值验证
  if (rule.max !== undefined) {
    const config = typeof rule.max === 'number' ? { value: rule.max } : rule.max;
    if (Number(value) > config.value) {
      return config.message || `最大值为 ${config.value}`;
    }
  }

  // 正则验证
  if (rule.pattern) {
    const config = rule.pattern instanceof RegExp ? { value: rule.pattern } : rule.pattern;
    if (!config.value.test(String(value))) {
      return config.message || '格式不正确';
    }
  }

  // 自定义验证
  if (rule.validate) {
    const validators = Array.isArray(rule.validate) ? rule.validate : [rule.validate];
    for (const validator of validators) {
      const error = validator(value, allValues);
      if (error) {
        return error;
      }
    }
  }

  return undefined;
};

/**
 * useForm Hook
 *
 * @description 表单状态管理 Hook，提供表单验证、提交、重置等功能
 *
 * @example
 * ```tsx
 * function LoginForm() {
 *   const form = useForm({
 *     initialValues: {
 *       username: '',
 *       password: '',
 *     },
 *     validationRules: {
 *       username: {
 *         required: '请输入用户名',
 *         minLength: { value: 3, message: '用户名至少 3 个字符' },
 *       },
 *       password: {
 *         required: '请输入密码',
 *         minLength: { value: 6, message: '密码至少 6 个字符' },
 *       },
 *     },
 *     onSubmit: async (values) => {
 *       await api.login(values);
 *       toast.success('登录成功！');
 *     },
 *   });
 *
 *   return (
 *     <Form onSubmit={form.handleSubmit}>
 *       <FormItem
 *         label="用户名"
 *         error={form.touched.username ? form.errors.username : undefined}
 *       >
 *         <Input {...form.getFieldProps('username')} />
 *       </FormItem>
 *
 *       <FormItem
 *         label="密码"
 *         error={form.touched.password ? form.errors.password : undefined}
 *       >
 *         <Input type="password" {...form.getFieldProps('password')} />
 *       </FormItem>
 *
 *       <Button htmlType="submit" type="primary" loading={form.isSubmitting}>
 *         登录
 *       </Button>
 *     </Form>
 *   );
 * }
 * ```
 */
export const useForm = <T extends FormValues = FormValues>(
  options: UseFormOptions<T>
): UseFormResult<T> => {
  const { initialValues, validationRules = {}, onSubmit, validateMode = 'onBlur' } = options;

  // 表单值
  const [values, setValuesState] = useState<T>(initialValues);

  // 表单错误
  const [errors, setErrorsState] = useState<FormErrors>({});

  // 表单触碰状态
  const [touched, setTouchedState] = useState<FormTouched>({});

  // 是否正在提交
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 是否有错误
  const isValid = useMemo(() => Object.keys(errors).length === 0, [errors]);

  // 是否已修改
  const isDirty = useMemo(
    () => JSON.stringify(values) !== JSON.stringify(initialValues),
    [values, initialValues]
  );

  /**
   * 设置字段值
   */
  const setFieldValue = useCallback((name: keyof T, value: any) => {
    setValuesState((prev) => ({ ...prev, [name]: value }));
  }, []);

  /**
   * 设置字段错误
   */
  const setFieldError = useCallback((name: keyof T, error: string | undefined) => {
    setErrorsState((prev) => ({ ...prev, [name]: error }));
  }, []);

  /**
   * 设置字段触碰状态
   */
  const setFieldTouched = useCallback((name: keyof T, touched: boolean) => {
    setTouchedState((prev) => ({ ...prev, [name]: touched }));
  }, []);

  /**
   * 验证单个字段
   */
  const validateField = useCallback(
    async (name: keyof T): Promise<string | undefined> => {
      const rule = validationRules[name as keyof typeof validationRules];
      if (!rule) {
        return undefined;
      }

      const value = values[name];
      const error = runValidation(value, rule, values);

      setFieldError(name, error);
      return error;
    },
    [values, validationRules, setFieldError]
  );

  /**
   * 验证所有字段
   */
  const validateForm = useCallback(async (): Promise<FormErrors> => {
    const newErrors: FormErrors = {};

    for (const name in validationRules) {
      const rule = validationRules[name as keyof typeof validationRules];
      if (rule) {
        const value = values[name as keyof T];
        const error = runValidation(value, rule, values);
        if (error) {
          newErrors[name] = error;
        }
      }
    }

    setErrorsState(newErrors);
    return newErrors;
  }, [values, validationRules]);

  /**
   * 获取字段属性（用于表单控件）
   */
  const getFieldProps = useCallback(
    (name: keyof T) => {
      return {
        name,
        value: values[name] || '',
        onChange: (e: React.ChangeEvent<any>) => {
          const value = e.target.value;
          setFieldValue(name, value);

          if (validateMode === 'onChange') {
            validateField(name);
          }
        },
        onBlur: () => {
          setFieldTouched(name, true);

          if (validateMode === 'onBlur') {
            validateField(name);
          }
        },
        error: touched[name as string] ? errors[name as string] : undefined,
      };
    },
    [values, errors, touched, validateMode, setFieldValue, setFieldTouched, validateField]
  );

  /**
   * 处理提交
   */
  const handleSubmit = useCallback(
    async (e?: React.FormEvent) => {
      e?.preventDefault();

      // 标记所有字段为已触碰
      const allTouched: FormTouched = {};
      for (const name in values) {
        allTouched[name] = true;
      }
      setTouchedState(allTouched);

      // 验证表单
      const validationErrors = await validateForm();

      // 如果有错误，不提交
      if (Object.keys(validationErrors).length > 0) {
        return;
      }

      // 提交表单
      setIsSubmitting(true);
      try {
        await onSubmit?.(values);
      } catch (error) {
        console.error('表单提交失败:', error);
      } finally {
        setIsSubmitting(false);
      }
    },
    [values, validateForm, onSubmit]
  );

  /**
   * 重置表单
   */
  const reset = useCallback(() => {
    setValuesState(initialValues);
    setErrorsState({});
    setTouchedState({});
    setIsSubmitting(false);
  }, [initialValues]);

  /**
   * 设置多个值
   */
  const setValues = useCallback((newValues: Partial<T>) => {
    setValuesState((prev) => ({ ...prev, ...newValues }));
  }, []);

  /**
   * 设置多个错误
   */
  const setErrors = useCallback((newErrors: FormErrors) => {
    setErrorsState((prev) => ({ ...prev, ...newErrors }));
  }, []);

  return {
    values,
    errors,
    touched,
    isSubmitting,
    isValid,
    isDirty,
    setFieldValue,
    setFieldError,
    setFieldTouched,
    getFieldProps,
    validateField,
    validateForm,
    handleSubmit,
    reset,
    setValues,
    setErrors,
  };
};

export default useForm;
