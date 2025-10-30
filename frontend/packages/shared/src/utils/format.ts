/**
 * 🎨 BaSui 的格式化工具函数
 * @description 日期、金额、文件大小等格式化
 */

// ==================== 日期格式化 ====================

/**
 * 格式化日期
 * @param date 日期对象或时间戳
 * @param format 格式字符串（默认：YYYY-MM-DD HH:mm:ss）
 */
export const formatDate = (
  date: Date | number | string,
  format = 'YYYY-MM-DD HH:mm:ss'
): string => {
  const d = typeof date === 'string' ? new Date(date) : new Date(date);

  const pad = (num: number) => String(num).padStart(2, '0');

  const replacements: Record<string, string> = {
    YYYY: String(d.getFullYear()),
    MM: pad(d.getMonth() + 1),
    DD: pad(d.getDate()),
    HH: pad(d.getHours()),
    mm: pad(d.getMinutes()),
    ss: pad(d.getSeconds()),
  };

  return format.replace(/YYYY|MM|DD|HH|mm|ss/g, match => replacements[match]);
};

/**
 * 格式化相对时间（如：刚刚、3分钟前、1天前）
 */
export const formatRelativeTime = (date: Date | number | string): string => {
  const d = typeof date === 'string' ? new Date(date) : new Date(date);
  const now = new Date();
  const diff = now.getTime() - d.getTime();

  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  const months = Math.floor(days / 30);
  const years = Math.floor(months / 12);

  if (seconds < 60) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 30) return `${days}天前`;
  if (months < 12) return `${months}个月前`;
  return `${years}年前`;
};

// ==================== 金额格式化 ====================

/**
 * 格式化金额（添加千位分隔符）
 * @param amount 金额
 * @param decimals 小数位数（默认：2）
 * @param prefix 前缀（默认：¥）
 */
export const formatMoney = (amount: number, decimals = 2, prefix = '¥'): string => {
  const fixed = amount.toFixed(decimals);
  const [integer, decimal] = fixed.split('.');
  const formattedInteger = integer.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  return `${prefix}${formattedInteger}${decimal ? '.' + decimal : ''}`;
};

/**
 * 格式化积分（添加千位分隔符）
 */
export const formatPoints = (points: number): string => {
  return String(points).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

// ==================== 文件大小格式化 ====================

/**
 * 格式化文件大小
 * @param bytes 字节数
 * @param decimals 小数位数（默认：2）
 */
export const formatFileSize = (bytes: number, decimals = 2): string => {
  if (bytes === 0) return '0 Bytes';

  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return `${(bytes / Math.pow(k, i)).toFixed(decimals)} ${sizes[i]}`;
};

// ==================== 手机号格式化 ====================

/**
 * 格式化手机号（隐藏中间4位）
 */
export const formatPhone = (phone: string): string => {
  if (!phone || phone.length !== 11) return phone;
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
};

// ==================== 身份证格式化 ====================

/**
 * 格式化身份证号（隐藏中间部分）
 */
export const formatIdCard = (idCard: string): string => {
  if (!idCard || idCard.length < 8) return idCard;
  return idCard.replace(/(\d{6})\d+(\d{4})/, '$1********$2');
};

// ==================== 数字格式化 ====================

/**
 * 格式化数字（添加千位分隔符）
 */
export const formatNumber = (num: number): string => {
  return String(num).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

/**
 * 格式化百分比
 * @param value 数值（0-1）
 * @param decimals 小数位数（默认：2）
 */
export const formatPercent = (value: number, decimals = 2): string => {
  return `${(value * 100).toFixed(decimals)}%`;
};
