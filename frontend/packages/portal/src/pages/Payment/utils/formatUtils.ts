/**
 * <åwýp
 * @author BaSui =
 * @description 'öô¡öI<ýp
 */

/**
 * <'>:
 */
export const formatCurrency = (amount: number | string): string => {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount;
  return `¥${num.toFixed(2)}`;
};

/**
 * <öô>:
 */
export const formatDateTime = (dateTime: string): string => {
  const date = new Date(dateTime);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

/**
 * <¡ö>:
 */
export const formatCountdown = (milliseconds: number): string => {
  const totalSeconds = Math.floor(milliseconds / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;

  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
};

/**
 * <øùöô
 */
export const formatRelativeTime = (dateTime: string): string => {
  const date = new Date(dateTime);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  const minutes = Math.floor(diff / (1000 * 60));
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (minutes < 1) return '';
  if (minutes < 60) return `${minutes}ŸM`;
  if (hours < 24) return `${hours}öM`;
  if (days < 7) return `${days})M`;

  return formatDateTime(dateTime);
};