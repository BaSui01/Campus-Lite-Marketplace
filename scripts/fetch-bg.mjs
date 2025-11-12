/**
 * 从互联网抓取通用“底版”背景图，保存到 docs/assets/bg.jpg
 * 可通过环境变量控制：
 *  - BG_URL:   直接指定图片URL（优先）
 *  - BG_QUERY: 关键词（英文逗号分隔），如 "abstract,gradient,soft"
 *  - BG_SIZE:  目标尺寸，默认 "1920x1080"
 *  - BG_PROVIDER: 源，"picsum" | "unsplash"（默认 picsum）
 *
 * 用法：
 *  - npm --prefix scripts run ppt:bg:fetch
 *  - 或：BG_QUERY="abstract,blur" BG_PROVIDER=unsplash npm --prefix scripts run ppt:bg:fetch
 */
import fs from 'node:fs';
import path from 'node:path';

const SIZE = process.env.BG_SIZE || '1920x1080';
const PROVIDER = (process.env.BG_PROVIDER || 'picsum').toLowerCase();
const QUERY = process.env.BG_QUERY || 'abstract,gradient,soft,blur';
const DIRECT_URL = process.env.BG_URL || '';
const OUT_DIR = path.resolve('docs/assets');
const OUT_FILE = path.join(OUT_DIR, 'bg.jpg');

function providerUrls() {
  const [w, h] = SIZE.split('x');
  const unsplash = `https://source.unsplash.com/${w}x${h}/?${encodeURIComponent(QUERY)}`;
  const picsum = `https://picsum.photos/${w}/${h}`;
  if (DIRECT_URL) return [DIRECT_URL, unsplash, picsum];
  return PROVIDER === 'unsplash' ? [unsplash, picsum] : [picsum, unsplash];
}

async function fetchImage(url) {
  try {
    const res = await fetch(url, { redirect: 'follow' });
    if (!res.ok) return null;
    const ab = await res.arrayBuffer();
    return Buffer.from(ab);
  } catch {
    return null;
  }
}

async function main() {
  if (!fs.existsSync(OUT_DIR)) fs.mkdirSync(OUT_DIR, { recursive: true });
  const urls = providerUrls();
  for (const u of urls) {
    const buf = await fetchImage(u);
    if (buf) {
      fs.writeFileSync(OUT_FILE, buf);
      console.log(`✅ 已保存背景：${OUT_FILE}（来自 ${u.includes('picsum') ? 'picsum' : (u.includes('unsplash') ? 'unsplash' : 'direct')}）`);
      console.log('提示：已自动生效，所有页面背景将优先使用 docs/assets/bg.jpg。');
      return;
    }
    console.warn(`⚠️ 拉取失败：${u}`);
  }
  console.error('❌ 未能下载背景图，请更换 BG_QUERY 或 BG_URL 重试。');
  process.exit(1);
}

main();

