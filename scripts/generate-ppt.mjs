/**
 * 生成项目演示 PPT（中文，20-30页，少字版）
 * 使用 pptxgenjs，输出到 docs/presentations/campus-lite-marketplace-demo-YYYYMMDD.pptx
 *
 * 说明：
 * - 内容基于仓库 README 与 docs/specs/SPEC_INDEX.md 摘要编排
 * - 每页尽量 3-5 个要点，8-14 字/要点
 * - 如需自定义，请编辑 slides[] 定义
 */
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import PptxGenJS from 'pptxgenjs';
// 可选：自动截图（若安装 puppeteer 则启用）
let puppeteer = null;
try {
  if (process.env.PPT_CAPTURE === '1' || process.env.PPT_CAPTURE === 'true') {
    // 延迟 require，避免强依赖
    puppeteer = await import('puppeteer').then(m => m.default || m).catch(() => null);
  }
} catch { /* no-op */ }

// ---------- 基础信息（可按需微调） ----------
const projectNameZh = '校园轻享集市系统';
const projectNameEn = 'Campus Lite Marketplace';
const author = '项目组';
const today = new Date();
const y = today.getFullYear();
const m = String(today.getMonth() + 1).padStart(2, '0');
const d = String(today.getDate()).padStart(2, '0');
const dateStr = `${y}-${m}-${d}`;
// 自定义封面与结尾信息
const DISPLAY_DATE = (process.env.PPT_DATE || `${y}/${m}/${d}`).trim();
const SPEAKER = (process.env.PPT_SPEAKER || author).trim();
const TITLE_OVERRIDE = (process.env.PPT_TITLE || '').trim();
const END_TITLE = (process.env.PPT_END_TITLE || '感谢聆听  ·  欢迎交流').trim();
const outDir = path.resolve('docs/presentations');
const outFile = path.join(outDir, `campus-lite-marketplace-demo-${y}${m}${d}.pptx`);

// ---------- 读取仓库摘要（用于少量动态信息） ----------
function readTextSafe(p) {
  try {
    return fs.readFileSync(p, 'utf-8');
  } catch {
    return '';
  }
}
// 文本源仅使用指定 txt（优先这两个文件）
const TEXT_FILES = [
  path.resolve('docs/assets/text/README.txt'),
  path.resolve('docs/assets/text/SPEC_INDEX.txt'),
];
function stripMarkdown(md) {
  return md
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/`([^`]+)`/g, '$1')
    .replace(/!\[[^\]]*\]\([^\)]*\)/g, ' ')
    .replace(/\[([^\]]*)\]\([^\)]*\)/g, '$1')
    .replace(/^\s{0,3}#{1,6}\s+/gm, '')
    .replace(/[*_]{1,3}([^*_]+)[*_]{1,3}/g, '$1')
    .replace(/^>\s?/gm, '')
    .replace(/^-+\s+/gm, '')
    .replace(/\||\t/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}
function collectTxtFromFiles() {
  return TEXT_FILES.map(readTextSafe).filter(Boolean).join('\n');
}
// 可选：从常见 md 转存到 txt 目录
if ((process.env.PPT_EXTRACT_MD_TO_TXT || '0') === '1') {
  const outTextDir = path.resolve('docs/assets/text');
  if (!fs.existsSync(outTextDir)) fs.mkdirSync(outTextDir, { recursive: true });
  const mdCandidates = [
    path.resolve('README.md'),
    path.resolve('backend/README.md'),
    path.resolve('docs/specs/SPEC_INDEX.md'),
  ];
  for (const mdPath of mdCandidates) {
    const md = readTextSafe(mdPath);
    if (md) {
      const base = path.basename(mdPath).replace(/\\.md$/i, '.txt');
      fs.writeFileSync(path.join(outTextDir, base), stripMarkdown(md));
    }
  }
}
const readme = collectTxtFromFiles() || stripMarkdown(readTextSafe(path.resolve('README.md')));
const specIndex = readTextSafe(path.resolve('docs/assets/text/SPEC_INDEX.txt')) || stripMarkdown(readTextSafe(path.resolve('docs/specs/SPEC_INDEX.md')));

const hasFrontend = fs.existsSync(path.resolve('frontend'));
const hasBackend = fs.existsSync(path.resolve('backend'));
const hasDocker = fs.existsSync(path.resolve('docker'));

// 简易指标提取（容错）
const coverageHint = specIndex.includes('≥85%') || readme.includes('85%')
  ? '覆盖率 ≥85%'
  : '覆盖率 良好';
const perfHint = (readme + specIndex).includes('P95 < 300ms')
  ? '搜索P95 < 300ms'
  : '接口性能 稳定';

// ---------- 幻灯片内容定义（标题 + 要点） ----------
const slides = [
  {
    type: 'cover',
    title: `${TITLE_OVERRIDE || projectNameZh}`,
    subtitle: `主讲人：${SPEAKER}\n时间：${DISPLAY_DATE}`,
  },
  {
    title: '目录',
    bullets: [
      '项目概述',
      '技术与架构',
      '后端专题',
      '前端专题',
      '界面截图',
      '致谢',
    ],
  },
  {
    title: '项目概述',
    bullets: [
      '覆盖校园二手交易、社区互动与资源共享，打造一体化生态。',
      '支持商品发布/智能搜索/下单支付/退款售后，业务闭环完整高效。',
      '即时消息与通知模板贯通，统一风控审计保证沟通及时与数据可靠。',
      '后端Java21+SpringBoot，前端Monorepo架构，工程化可扩展与可维护。',
    ],
  },
  {
    title: '面向人群与价值',
    bullets: [
      '学生：便捷交易与交流',
      '社团：活动与资源共享',
      '学校：治理与风控工具',
      '开发：工程化最佳实践',
    ],
  },
  {
    title: '核心特性总览',
    bullets: [
      '用户体系与风控',
      '商品/订单全链路',
      '支付接入与退款',
      '消息中心与通知',
      '统计看板与运营',
    ],
  },
  {
    title: '技术栈',
    bullets: [
      '后端：Java 21 / SB3',
      '数据库：PostgreSQL 16',
      '缓存：Redis 7 + Redisson',
      '前端：React 18 + TS',
      '构建：Maven / pnpm',
    ],
  },
  { type: 'diagram-architecture' },
  {
    title: '后端模块',
    bullets: [
      '分层：Controller/Service/Repo',
      '公共层：DTO/异常/切面',
      '异步：事件 + 调度任务',
      '缓存：热点与二级缓存',
    ],
  },
  {
    title: '前端架构（Monorepo）',
    bullets: [
      'Workspace：packages/*',
      'shared 公共层复用',
      'portal 用户端',
      'admin 管理端',
    ],
  },
  {
    title: '支付能力',
    bullets: [
      '支付宝 / 微信 V2/V3',
      '统一支付门面',
      '异步通知与对账',
      '退款流程与幂等',
    ],
  },
  {
    title: '订单与交易',
    bullets: [
      '全状态机管理',
      '取消与超时控制',
      '售后与退款支持',
      '审计与追踪完善',
    ],
  },
  {
    title: '搜索与推荐',
    bullets: [
      'PostgreSQL FTS',
      '中文分词 + 高亮',
      'Redis 热榜 + 定时',
      '命中 <100ms 级',
    ],
  },
  {
    title: '消息与通知',
    bullets: [
      'WebSocket 实时',
      '已读与会话管理',
      '模板化通知',
      '多渠道扩展',
    ],
  },
  {
    title: '安全与权限',
    bullets: [
      'Spring Security + JWT',
      'RBAC 多角色控制',
      '黑白名单与限流',
      '隐私与合规治理',
    ],
  },
  {
    title: '性能与并发',
    bullets: [
      'Redisson 分布式锁',
      '缓存穿透与降级',
      '异步化解耦主链路',
      perfHint,
    ],
  },
  {
    title: '可观测性与监控',
    bullets: [
      '日志 + TraceId 透传',
      'Prometheus 指标',
      'Grafana 看板',
      '统一异常处理',
    ],
  },
  {
    title: '多校区与功能开关',
    bullets: [
      '校区数据隔离',
      'Feature Flags',
      '缓存刷新机制',
      '灰度与评估器',
    ],
  },
  {
    title: '前端组件亮点',
    bullets: [
      '共享组件与类型',
      '验证码与安全控件',
      '图表与分页组件',
      'API 客户端封装',
    ],
  },
  {
    title: '前端页面与路由',
    bullets: [
      'React Router v6',
      '模块化路由表',
      '受保护路由',
      '404 与重定向',
    ],
  },
  {
    title: '前端状态与数据流',
    bullets: [
      'React Query 缓存',
      'Zustand 轻量状态',
      '请求拦截与刷新',
      '错误边界与提示',
    ],
  },
  {
    title: '前端数据可视化',
    bullets: [
      'ECharts 组件化',
      '主题与响应式',
      '异步数据渲染',
      '性能优化策略',
    ],
  },
  { type: 'thankyou' },
];

// ---------- 样式与主题 ----------
// 每页文字总上限（要点+说明），限定在 50–200，默认 200
const MAX_CHARS = Math.max(50, Math.min(200, parseInt(process.env.PPT_MAX_CHARS_PER_SLIDE || '200', 10)));
// 说明段最小目标字数，默认 20 字，并且不超过 (MAX_CHARS - 50)
const NOTE_MIN_CHARS = (() => {
  const n = parseInt(process.env.PPT_NOTE_MIN_CHARS || '20', 10);
  const cap = Math.max(0, MAX_CHARS - 50);
  return Math.max(0, Math.min(n, cap));
})();
// bullets 总字数下限（仅 bullets，不含说明），默认 MAX_CHARS-20 或 120 取较小者，但不低于50
const BULLETS_MIN_CHARS = (() => {
  const def = Math.min(MAX_CHARS - 20, 120);
  const n = parseInt(process.env.PPT_BULLETS_MIN_CHARS || String(def), 10);
  const maxAllowed = Math.max(0, MAX_CHARS - NOTE_MIN_CHARS);
  return Math.max(50, Math.min(n, maxAllowed));
})();
// 默认主题改为中性灰+暖橙（避免蓝色）
const COLORS = {
  primary: (process.env.PPT_PRIMARY_COLOR || '1F2937').replace('#','').toUpperCase(),   // 灰-800
  secondary: (process.env.PPT_SECONDARY_COLOR || 'F97316').replace('#','').toUpperCase(), // 橙-500
  dark: (process.env.PPT_DARK_COLOR || '0F172A').replace('#','').toUpperCase(),
  light: (process.env.PPT_LIGHT_COLOR || 'FAFAFA').replace('#','').toUpperCase(),
  gray: (process.env.PPT_GRAY_COLOR || '6B7280').replace('#','').toUpperCase(),
};
const FONT = { face: os.platform() === 'win32' ? 'Microsoft YaHei' : 'Noto Sans CJK SC' };
const LOGO_PATH = process.env.PPT_LOGO_PATH
  || ['docs/assets/logo.png','docs/assets/logo.jpg','frontend/packages/shared/public/logo.png']
      .map(p => path.resolve(p)).find(p => fs.existsSync(p));
// 背景图（封面与内页可分开）
const FORCED_BG_PATH = (() => {
  const envPath = process.env.PPT_FORCE_BG_PATH ? path.resolve(process.env.PPT_FORCE_BG_PATH) : null;
  if (envPath && fs.existsSync(envPath)) return envPath;
  const defaultPath = path.resolve('docs/assets/bg.jpg');
  return fs.existsSync(defaultPath) ? defaultPath : null;
})();
const BG_COVER = [FORCED_BG_PATH, process.env.PPT_BG_IMAGE_COVER,
  // 优先使用全局 assets 目录
  'docs/assets/cover.jpg','docs/assets/cover.png','docs/assets/bg-cover.jpg','docs/assets/bg.jpg',
  // 兼容 presentations 目录
  'docs/presentations/assets/cover.jpg','docs/presentations/assets/cover.png','docs/presentations/assets/bg.jpg']
  .filter(Boolean).map(p => path.resolve(p)).find(p => fs.existsSync(p));
const BG_DEFAULT = [FORCED_BG_PATH, process.env.PPT_BG_IMAGE,
  // 优先使用全局 assets 目录
  'docs/assets/default.jpg','docs/assets/default.png','docs/assets/bg.jpg','docs/assets/bg.png',
  // 兼容 presentations 目录
  'docs/presentations/assets/default.jpg','docs/presentations/assets/default.png','docs/presentations/assets/bg.jpg','docs/presentations/assets/bg.png']
  .filter(Boolean).map(p => path.resolve(p)).find(p => fs.existsSync(p));
const WATERMARK = process.env.PPT_WATERMARK || '';
const PAGE_FOOTER = process.env.PPT_FOOTER || 'Campus Lite Marketplace';
const CARD_BG = process.env.PPT_CARD_BG || 'FFFFFF';
const HEADER_HEIGHT = parseFloat(process.env.PPT_HEADER_H || '0.9'); // 顶部色带高度
const CONTENT_CARD = {
  x: 0.5, y: 1.1, w: 12.3, h: 5.9,
};
// 网络图片抓取配置（无需 API Key，使用 Unsplash Source）
const FETCH_IMAGES = process.env.PPT_FETCH_IMAGES === '1' || process.env.PPT_FETCH_IMAGES === 'true';
const FORCE_LOCAL_ASSETS = (process.env.PPT_FORCE_LOCAL_ASSETS ?? '1') === '1'; // 默认强制使用本地 assets
const CENTER_MODE = (process.env.PPT_CENTER_MODE ?? '1') === '1'; // 默认开启居中模式
const IMAGE_DIR = path.resolve('docs/presentations/assets');
const IMAGE_SIZE = process.env.PPT_IMAGE_SIZE || '1600x900';
const IMAGE_PROVIDER = process.env.PPT_IMAGE_PROVIDER || 'unsplash'; // unsplash|picsum
const IMAGE_TOPICS = {
  '项目概述': 'campus,students,market',
  '技术栈': 'java,spring boot,react,postgresql,redis',
  '后端模块': 'springboot,java,api,service,controller',
  '支付能力': 'alipay,wechat,payment,online',
  '订单与交易': 'ecommerce,order,checkout,receipt',
  '搜索与推荐': 'search,ranking,algorithm',
  '消息与通知': 'websocket,notification,message',
  '安全与权限': 'jwt,rbac,security,lock',
  '性能与并发': 'redis,lock,scalability,performance',
  '可观测性与监控': 'prometheus,grafana,metrics,monitoring',
  '前端架构（Monorepo）': 'monorepo,workspace,react',
  '前端页面与路由': 'react,router,ui',
  '前端状态与数据流': 'react query,zustand,state',
  '前端数据可视化': 'echarts,charts,analytics',
  '前端组件亮点': 'components,design,ui',
};

// ---------- 文本提取与补充 ----------
function clampChars(str, max) {
  if (!str) return '';
  if (str.length <= max) return str;
  return str.slice(0, Math.max(0, max - 1)) + '…';
}
function extractSection(text, headingRegex) {
  const m = text.match(headingRegex);
  if (!m) return '';
  const start = m.index ?? 0;
  const rest = text.slice(start).split('\n').slice(1); // skip heading line
  const lines = [];
  for (const line of rest) {
    if (/^##\s/.test(line)) break;
    const t = line.replace(/^[#>\-\*`\s]+/g, '').trim();
    if (t) lines.push(t);
  }
  return lines.join('；');
}
function searchByKeywords(text, keywords = [], maxLen = 140) {
  const lines = text.split('\n').map(l => l.trim()).filter(Boolean);
  const hit = [];
  for (const line of lines) {
    if (keywords.some(k => line.includes(k))) {
      const t = line.replace(/^[-*•\s]+/, '').replace(/`/g,'').replace(/\s+/g,' ');
      if (!hit.includes(t)) hit.push(t);
    }
    if (hit.join('；').length >= maxLen) break;
  }
  return clampChars(hit.join('；'), maxLen);
}
function supplementFor(title, bullets, budget) {
  // 优先匹配特定章节
  if (/技术栈/.test(title)) {
    const sec = extractSection(readme, /^##\s*🧱\s*技术栈/m) || extractSection(readme, /^##\s*技术栈/m);
    if (sec) return clampChars(sec, budget);
  }
  if (/部署|运维|部署方案/.test(title)) {
    const sec = extractSection(readme, /^##\s*🐳\s*Docker/m) || extractSection(readme, /^##\s*🐳\s*Docker 化部署/m);
    if (sec) return clampChars(sec, budget);
  }
  if (/测试|质量/.test(title)) {
    const sec = extractSection(readme, /^##\s*🧪\s*测试与质量/m) || searchByKeywords(readme, ['Jacoco','SpotBugs','verify']);
    if (sec) return clampChars(sec, budget);
  }
  // 关键词回退：从 README + specs 搜索相关句子
  const keywordsMap = {
    '项目概述': ['校园','二手','交易','社区','支付'],
    '面向人群与价值': ['学生','社团','学校','开发'],
    '核心特性': ['用户体系','商品','订单','支付','消息','运营','监控'],
    '后端模块': ['Controller','Service','Repository','异步','缓存','切面','异常'],
    '前端架构': ['Monorepo','packages','shared','portal','admin'],
    '前端页面与路由': ['React Router','路由','受保护','重定向','404'],
    '前端状态与数据流': ['React Query','Zustand','状态','缓存','刷新','错误'],
    '前端数据可视化': ['ECharts','图表','可视化','主题','响应式','性能'],
    '支付能力': ['支付宝','微信','退款','对账','异步通知','幂等'],
    '订单与交易': ['状态机','取消','超时','审计'],
    '搜索与推荐': ['全文检索','高亮','分词','Redis','热榜'],
    '消息与通知': ['WebSocket','已读','模板','通知'],
    '安全与权限': ['JWT','RBAC','黑白名单','限流','合规'],
    '性能与并发': ['Redisson','分布式锁','降级','异步'],
    '可观测性与监控': ['Prometheus','Grafana','日志','Trace','Sleuth'],
    '配置与环境': ['.env','密钥','日志级别','支付配置'],
    '多校区与功能开关': ['多校区','Feature Flags','灰度','缓存刷新'],
    '报表与导出': ['流式分页','签名下载','权限','定时任务'],
    '前端组件亮点': ['组件','验证码','图表','分页','API 客户端'],
    '已交付里程碑': ['Monorepo','搜索','推荐','监控','核心业务'],
    'Demo 演示脚本': ['登录','下单','支付回调','通知'],
    '风险与对策': ['证书','一致性','热点','熔断'],
    '三个月路线图': ['管理端','移动端','A/B','风控'],
    '成果与指标': ['工程化','部署','体验','性能'],
    '目录': ['项目','架构','部署','质量'],
    '系统架构': ['Controller','Service','Repository','Redis','PostgreSQL','Nginx'],
  };
  const key = Object.keys(keywordsMap).find(k => title.includes(k)) || title;
  const kw = keywordsMap[key] || bullets || [];
  const merged = [readme, specIndex].join('\n');
  let text = searchByKeywords(merged, kw, budget);
  if (!text && bullets?.length) {
    text = clampChars(`要点：${bullets.join('、')}。`, budget);
  }
  return text;
}

// 当说明不足时，自动扩写为更自然的段落
function expandParagraph(title, bullets, current, minChars, limit) {
  if ((current || '').length >= minChars) return current;
  const head = /目录|Q & A|界面截图/.test(title) ? '' : '本页概述：';
  const bulletLine = (bullets && bullets.length)
    ? `涵盖${bullets.join('、')}等要点，`
    : '';
  // 从 README 里再找一句补充（优先取非空的一行）
  const extra = (() => {
    const line = (readme.split('\n').map(s => s.trim()).filter(Boolean)
      .find(s => s.length > 10 && s.length < 60)) || '';
    return line.replace(/^[#>\-\*•\s]+/, '');
  })();
  const para = `${head}${bulletLine}${current || ''}${extra ? ` ${extra}` : ''}`;
  return clampChars(para.replace(/\s+/g, ' '), limit);
}

// 让 bullets 自身达到目标字数：按主题补句或追加条目
function enrichBullets(title, bullets, targetMin) {
  const base = Array.isArray(bullets) ? bullets.slice() : [];
  const countChars = (arr) => arr.join('').replace(/\s/g, '').length;
  let total = countChars(base);
  if (total >= targetMin) return base;
  // 试着从 supplement 中拆分短句做“加料”
  const budget = Math.max(40, targetMin - total);
  const sup = supplementFor(title, base, budget * 2); // 拿多点，便于拆分
  const parts = (sup || '').split(/[；。.!?]/).map(s => s.trim()).filter(s => s.length >= 6);
  for (const p of parts) {
    if (total >= targetMin) break;
    base.push(p);
    total = countChars(base);
  }
  // 仍不足：对现有 bullets 追加短句（尽量不破坏阅读）
  let i = 0;
  while (total < targetMin && base.length && i < base.length) {
    base[i] = base[i].replace(/[。;；]*$/, '') + '，并提供可扩展实践。';
    total = countChars(base);
    i++;
  }
  return base;
}
// ---------- 构建 PPT ----------
async function build() {
  if (!fs.existsSync(outDir)) {
    fs.mkdirSync(outDir, { recursive: true });
  }
  const pptx = new PptxGenJS();
  pptx.author = author;
  pptx.company = projectNameEn;
  pptx.subject = `${projectNameZh} - 演示文稿`;
  pptx.title = `${projectNameZh} 演示 - ${dateStr}`;
  pptx.layout = 'LAYOUT_16x9';

  // 基础装饰：背景、页脚与水印
  function applyBackground(slide, kind) {
    if (kind === 'cover' && BG_COVER) {
      slide.background = { path: BG_COVER };
    } else if (BG_DEFAULT) {
      slide.background = { path: BG_DEFAULT };
    }
  }
  function addHeaderBar(slide, title) {
    // 顶部细线 + 居中标题
    slide.addShape(pptx.ShapeType.rect, { x: 0, y: 0, w: 13.33, h: 0.12, fill: COLORS.gray, line: { type: 'none' } });
    slide.addText(title, { x: 0, y: 0.12, w: 13.33, h: 0.7, bold: true, fontSize: 24, color: COLORS.primary, align: 'center', fontFace: FONT.face });
    if (LOGO_PATH) slide.addImage({ path: LOGO_PATH, x: 11.6, y: 0.12, w: 1.2, h: 0.52, sizing: { type: 'contain', w: 1.2, h: 0.52 } });
  }
  function addFooter(slide) {
    slide.addShape(pptx.ShapeType.rect, { x: 0, y: 7.1, w: 13.33, h: 0.15, fill: COLORS.gray, line: { type: 'none' } });
    const footerText = PAGE_FOOTER ? `${PAGE_FOOTER}  ·  ${DISPLAY_DATE}` : `${DISPLAY_DATE}`;
    slide.addText(footerText, { x: 0, y: 7.12, w: 13.33, h: 0.3, fontSize: 10, color: COLORS.gray, align: 'center', fontFace: FONT.face });
    if (WATERMARK) {
      slide.addText(WATERMARK, { x: 0, y: 6.8, w: 13.33, h: 0.4, fontSize: 12, color: COLORS.gray, align: 'center', fontFace: FONT.face });
    }
  }
  function addContentCard(slide) {
    // 居中内容卡片（已居中），加轻微阴影
    slide.addShape(pptx.ShapeType.roundRect, {
      x: CONTENT_CARD.x, y: CONTENT_CARD.y, w: CONTENT_CARD.w, h: CONTENT_CARD.h,
      fill: CARD_BG, line: { color: 'E5E7EB' },
      shadow: { type: 'outer', color: '000000', blur: 4, offset: 1, angle: 45, opacity: 0.15 },
    });
  }

  // 封面
  function addCover({ title, subtitle }) {
    const slide = pptx.addSlide();
    applyBackground(slide, 'cover');
    if (!BG_COVER) {
      slide.background = { fill: COLORS.dark };
    }
    // 居中内容容器
    const panelW = 11.2, panelH = 4.8;
    const panelX = (13.33 - panelW) / 2;
    const panelY = (7.5 - panelH) / 2;
    slide.addShape(pptx.ShapeType.roundRect, {
      x: panelX, y: panelY, w: panelW, h: panelH,
      fill: { type: 'solid', color: '000000', transparency: 20 },
      line: { type: 'none' },
      shadow: { type: 'outer', color: '000000', blur: 8, offset: 2, angle: 45, opacity: 0.25 },
    });
    // 标题居中
    slide.addText(title, {
      x: panelX + 0.6, y: panelY + 0.8, w: panelW - 1.2, h: 1.8,
      bold: true, fontSize: 50, color: 'FFFFFF', align: 'center', fontFace: FONT.face,
    });
    slide.addText(subtitle, {
      x: panelX + 0.6, y: panelY + 2.5, w: panelW - 1.2, h: 0.9,
      fontSize: 18, color: 'E5E7EB', align: 'center', fontFace: FONT.face,
    });
    // 底部细线与页脚（灰色，替代蓝色）
    slide.addShape(pptx.ShapeType.rect, { x: 0, y: 7.1, w: 13.33, h: 0.15, fill: COLORS.gray, line: { type: 'none' } });
    slide.addText(`主讲人：${SPEAKER}    时间：${DISPLAY_DATE}`, { x: 0.5, y: 7.12, w: 12.3, h: 0.4, fontSize: 12, color: COLORS.gray, align: 'right', fontFace: FONT.face });
    if (LOGO_PATH) {
      slide.addImage({ path: LOGO_PATH, x: panelX + panelW - 1.8, y: panelY + 0.2, w: 1.6, h: 1.6, sizing: { type: 'contain', w: 1.6, h: 1.6 } });
    }
  }

  // 下载与收集主题配图
  async function fetchThemeImages(slideDefs) {
    // 若强制本地，则尝试从 docs/assets 下按标题命名的图片加载
    if (FORCE_LOCAL_ASSETS) {
      const localMap = {};
      // 允许通过环境变量 JSON 指定本地文件映射：{ "支付能力": "docs/assets/pay.jpg" }
      try {
        if (process.env.PPT_IMAGE_FILES) {
          const filesMap = JSON.parse(process.env.PPT_IMAGE_FILES);
          for (const [title, filePath] of Object.entries(filesMap)) {
            const abs = path.resolve(String(filePath));
            if (fs.existsSync(abs)) localMap[title] = abs;
          }
        }
      } catch (e) {
        console.warn('⚠️ PPT_IMAGE_FILES 解析失败：', e.message);
      }
      for (const s of slideDefs) {
        if (!s || !s.title || s.type) continue;
        const cand = [
          path.resolve(`docs/assets/${s.title}.jpg`),
          path.resolve(`docs/assets/${s.title}.png`),
          path.resolve(`docs/assets/${s.title}.jpeg`),
        ];
        const found = cand.find(p => fs.existsSync(p));
        if (found) localMap[s.title] = found;
      }
      return localMap;
    }
    if (FETCH_IMAGES && FORCE_LOCAL_ASSETS) {
      console.warn('ℹ️ 已启用 PPT_FORCE_LOCAL_ASSETS，忽略网络配图抓取。');
    }
    if (!FETCH_IMAGES) return {};
    if (!fs.existsSync(IMAGE_DIR)) fs.mkdirSync(IMAGE_DIR, { recursive: true });
    const map = {};
    // 允许通过环境变量注入覆盖（JSON 字符串）
    let overrides = {};
    try {
      if (process.env.PPT_IMAGE_TOPICS) {
        overrides = JSON.parse(process.env.PPT_IMAGE_TOPICS);
      }
    } catch (e) {
      console.warn('⚠️ PPT_IMAGE_TOPICS 解析失败：', e.message);
    }
    const urlUnsplash = (topic) => `https://source.unsplash.com/${IMAGE_SIZE}/?${encodeURIComponent(topic)}`;
    const urlPicsum = () => {
      const [w,h] = IMAGE_SIZE.split('x');
      return `https://picsum.photos/${w}/${h}`;
    };
    const toSlug = (t) => t.replace(/[\\/:*?"<>|#\s]+/g, '_').slice(0,64);
    for (const s of slideDefs) {
      if (!s || !s.title || s.type) continue;
      if (/封面|目录|系统架构|界面截图|致谢|Q & A/i.test(s.title)) continue;
      const topic = overrides[s.title] || IMAGE_TOPICS[s.title] || 'technology';
      const file = path.join(IMAGE_DIR, `${toSlug(s.title)}.jpg`);
      if (fs.existsSync(file)) { map[s.title] = file; continue; }
      try {
        const tryUrls = [];
        if (IMAGE_PROVIDER === 'picsum') {
          tryUrls.push(urlPicsum());
          tryUrls.push(urlUnsplash(topic));
        } else {
          tryUrls.push(urlUnsplash(topic));
          tryUrls.push(urlPicsum());
        }
        let ok = false;
        for (const u of tryUrls) {
          const res = await fetch(u, { redirect: 'follow' }).catch(() => null);
          if (res && res.ok) {
            const ab = await res.arrayBuffer();
            fs.writeFileSync(file, Buffer.from(ab));
            map[s.title] = file;
            console.log(`🖼️ 已下载主题图：${s.title} -> ${file} (${u.includes('picsum') ? 'picsum' : 'unsplash'})`);
            ok = true;
            break;
          }
        }
        if (!ok) {
          console.warn(`⚠️ 图片获取失败 ${s.title}: 所有源不可用`);
        }
      } catch (e) {
        console.warn(`⚠️ 下载异常 ${s.title}: ${e.message}`);
      }
    }
    return map;
  }

  // 将 bullets 拆分为两列（尽量平均字数）
  function splitBulletsTwoCols(bulArr) {
    const arr = bulArr.slice();
    const len = (a) => a.join('').replace(/\s/g,'').length;
    const col1 = [], col2 = [];
    let l1 = 0, l2 = 0;
    for (const b of arr) {
      if (l1 <= l2) { col1.push(b); l1 = len(col1); } else { col2.push(b); l2 = len(col2); }
    }
    return [col1, col2];
  }

  // 标题 + 要点（可带主题配图，交替布局）
  function addTitleBullets({ title, bullets }, imageMap = {}, index = 0) {
    const slide = pptx.addSlide();
    applyBackground(slide, 'content');
    if (!BG_DEFAULT) {
      slide.background = { fill: 'FFFFFF' };
    }
    addHeaderBar(slide, title);
    addContentCard(slide);
    // 保证 bullets 文本达到设定下限
    const isToc = title === '目录';
    const enhancedBullets = isToc ? (bullets || []) : enrichBullets(title, bullets, BULLETS_MIN_CHARS);
    const bulletChars = (enhancedBullets || []).join('').length;
    const bulletFont = bulletChars > 200 ? 18 : 20;
    // 内容区域
    const hasImg = !CENTER_MODE && imageMap && imageMap[title];
    const imageLeft = index % 2 === 1; // 交替：奇数页图片在左
    const textArea = { x: CONTENT_CARD.x + 0.4, y: CONTENT_CARD.y + 0.2, w: CONTENT_CARD.w - 0.8, h: 3.8 };
    if (hasImg) {
      const imgW = 4.6, gap = 0.6;
      if (imageLeft) {
        slide.addImage({ path: imageMap[title], x: CONTENT_CARD.x + 0.2, y: CONTENT_CARD.y + 0.2, w: imgW, h: 3.8, sizing: { type: 'cover' } });
        textArea.x = CONTENT_CARD.x + imgW + gap + 0.2;
        textArea.w = CONTENT_CARD.w - imgW - gap - 0.6;
      } else {
        textArea.w = CONTENT_CARD.w - imgW - gap - 0.6;
        slide.addImage({ path: imageMap[title], x: CONTENT_CARD.x + textArea.w + gap + 0.2, y: CONTENT_CARD.y + 0.2, w: imgW, h: 3.8, sizing: { type: 'cover' } });
      }
    }
    // 居中模式下，始终单列且文本水平居中
    const useTwoCols = !hasImg && bulletChars >= 180 && !CENTER_MODE;
    if (useTwoCols) {
      const [c1, c2] = splitBulletsTwoCols(enhancedBullets);
      const items1 = c1.map((t) => ({ text: `• ${t}`, options: { fontSize: bulletFont, color: COLORS.dark, fontFace: FONT.face, breakLine: true } }));
      const items2 = c2.map((t) => ({ text: `• ${t}`, options: { fontSize: bulletFont, color: COLORS.dark, fontFace: FONT.face, breakLine: true } }));
      const colW = (textArea.w - 0.6) / 2;
      slide.addText(items1, { x: textArea.x, y: textArea.y, w: colW, h: textArea.h, align: 'left' });
      slide.addText(items2, { x: textArea.x + colW + 0.6, y: textArea.y, w: colW, h: textArea.h, align: 'left' });
    } else {
      const items = (enhancedBullets || []).map((t) => ({ text: `• ${t}`, options: { fontSize: bulletFont, color: COLORS.dark, fontFace: FONT.face, breakLine: true } }));
      if (CENTER_MODE) {
        const maxW = Math.min(10.2, textArea.w);
        const cx = CONTENT_CARD.x + (CONTENT_CARD.w - maxW) / 2;
        slide.addText(items, { x: cx, y: textArea.y, w: maxW, h: textArea.h, align: 'center' });
      } else {
        slide.addText(items, { x: textArea.x, y: textArea.y, w: textArea.w, h: textArea.h, align: 'left' });
      }
    }
    // 补充说明（自适应至 MAX_CHARS）
    if (!isToc) {
      const budget = Math.max(0, MAX_CHARS - bulletChars);
      let note = budget >= NOTE_MIN_CHARS ? supplementFor(title, bullets, budget) : '';
      note = expandParagraph(title, bullets, note, NOTE_MIN_CHARS, budget);
      if (note) {
        slide.addText(note, { x: CONTENT_CARD.x + 0.4, y: CONTENT_CARD.y + 4.25, w: CONTENT_CARD.w - 0.8, h: 1.6, fontSize: 16, color: COLORS.gray, align: 'center', fontFace: FONT.face });
      }
    }
    addFooter(slide);
  }

  // 简易架构图
  function addArchitectureDiagram() {
    const slide = pptx.addSlide();
    applyBackground(slide, 'content');
    addHeaderBar(slide, '系统架构');
    addContentCard(slide);

    // 在内容卡片区域内水平居中绘制三列结构
    const area = { x: CONTENT_CARD.x, y: CONTENT_CARD.y, w: CONTENT_CARD.w, h: CONTENT_CARD.h };
    const colW = 3.2, colGap = 2.0;
    const totalW = colW * 3 + colGap * 2;
    const startX = area.x + (area.w - totalW) / 2;
    const y1 = area.y + 0.5;
    const y2 = y1 + 1.4;
    const y3 = y2 + 1.4;

    // 前端（左列）
    slide.addShape(pptx.ShapeType.roundRect, { x: startX, y: y1, w: colW, h: 1.0, fill: COLORS.secondary, line: { color: COLORS.gray } });
    slide.addText('前端\n(React + TS)', { x: startX, y: y1 + 0.05, w: colW, h: 1.0, align: 'center', fontSize: 16, color: COLORS.dark, fontFace: FONT.face });

    // 中列：Controller -> Service -> Repo
    const midX = startX + colW + colGap;
    slide.addShape(pptx.ShapeType.roundRect, { x: midX, y: y1, w: colW, h: 1.0, fill: 'DCEAFE', line: { color: COLORS.gray } });
    slide.addText('REST Controller', { x: midX, y: y1 + 0.35, w: colW, h: 0.6, align: 'center', fontSize: 16, color: COLORS.dark, fontFace: FONT.face });
    slide.addShape(pptx.ShapeType.roundRect, { x: midX, y: y2, w: colW, h: 1.0, fill: 'E0F2FE', line: { color: COLORS.gray } });
    slide.addText('Service 层', { x: midX, y: y2 + 0.35, w: colW, h: 0.6, align: 'center', fontSize: 16, color: COLORS.dark, fontFace: FONT.face });
    slide.addShape(pptx.ShapeType.roundRect, { x: midX, y: y3, w: colW, h: 1.0, fill: 'ECFEFF', line: { color: COLORS.gray } });
    slide.addText('Repository/JPA', { x: midX, y: y3 + 0.35, w: colW, h: 0.6, align: 'center', fontSize: 16, color: COLORS.dark, fontFace: FONT.face });

    // 右列：外部、Redis、DB
    const rightX = midX + colW + colGap;
    slide.addShape(pptx.ShapeType.roundRect, { x: rightX, y: y1, w: colW, h: 1.0, fill: 'E9D5FF', line: { color: COLORS.gray } });
    slide.addText('支付/短信/邮箱', { x: rightX, y: y1 + 0.35, w: colW, h: 0.6, align: 'center', fontSize: 16, color: COLORS.dark, fontFace: FONT.face });
    slide.addShape(pptx.ShapeType.roundRect, { x: rightX, y: y2, w: colW, h: 0.9, fill: 'FFE4E6', line: { color: COLORS.gray } });
    slide.addText('Redis/Redisson', { x: rightX, y: y2 + 0.25, w: colW, h: 0.6, align: 'center', fontSize: 16, color: COLORS.dark, fontFace: FONT.face });
    slide.addShape(pptx.ShapeType.roundRect, { x: rightX, y: y3, w: colW, h: 0.9, fill: 'FEF3C7', line: { color: COLORS.gray } });
    slide.addText('PostgreSQL', { x: rightX, y: y3 + 0.25, w: colW, h: 0.6, align: 'center', fontSize: 16, color: COLORS.dark, fontFace: FONT.face });

    // 箭头（水平居中放置）
    const arrow = (x, y, w, h) => slide.addShape(pptx.ShapeType.line, { x, y, w, h, line: { color: COLORS.gray, width: 2, endArrowHead: 'triangle' } });
    arrow(startX + colW, y1 + 0.5, midX - (startX + colW), 0);      // 前端 -> Controller
    arrow(midX + colW / 2, y1 + 1.0, 0, y2 - y1 - 0.2);             // Controller -> Service
    arrow(midX + colW / 2, y2 + 1.0, 0, y3 - y2 - 0.2);             // Service -> Repo
    arrow(midX + colW, y2 + 0.45, rightX - (midX + colW), -0.6);    // Service -> 外部
    arrow(midX + colW, y2 + 0.60, rightX - (midX + colW), 0.0);     // Service -> Redis
    arrow(midX + colW, y3 + 0.45, rightX - (midX + colW), 0.0);     // Repo -> DB

    // 说明文（居中范围）
    const budget = MAX_CHARS - 40;
    const note = supplementFor('系统架构', ['前端','Controller','Service','Repository','Redis','PostgreSQL','外部集成'], Math.max(40, budget));
    slide.addText(note, { x: area.x + 0.4, y: area.y + area.h - 1.7, w: area.w - 0.8, h: 1.5, fontSize: 16, color: COLORS.gray, align: 'center', fontFace: FONT.face });
    addFooter(slide);
  }

  // 封底
  function addThankYou() {
    const slide = pptx.addSlide();
    applyBackground(slide, 'content');
    if (!BG_DEFAULT) slide.background = { fill: COLORS.dark };
    slide.addText(END_TITLE || '谢谢大家', {
      x: 0.8, y: 2.6, w: 11.2, h: 1.4,
      bold: true, fontSize: 38, color: COLORS.light, align: 'center', fontFace: FONT.face,
    });
    slide.addText(`主讲人：${SPEAKER}    时间：${DISPLAY_DATE}`, {
      x: 0.8, y: 4.0, w: 11.2, h: 0.8,
      fontSize: 16, color: COLORS.light, align: 'center', fontFace: FONT.face,
    });
    addFooter(slide);
  }

  // 收集截图（本地图片 or 现场抓图）
  async function collectScreenshots() {
    const baseDir = path.resolve('docs/presentations/screenshots');
    const collected = [];
    const onlyAuto = (process.env.PPT_SHOTS_ONLY_AUTO === '1' || process.env.PPT_SHOTS_ONLY_AUTO === 'true');
    // 可选：清空旧的 auto 截图目录
    if (process.env.PPT_CLEAR_OLD_SHOTS === '1' || process.env.PPT_CLEAR_OLD_SHOTS === 'true') {
      const autoDir = path.join(baseDir, 'auto');
      if (fs.existsSync(autoDir)) {
        fs.rmSync(autoDir, { recursive: true, force: true });
      }
    }
    // 读取根 .env（用于推断 portal 端口）
    const envPath = path.resolve('.env');
    let envPortal = '';
    try {
      if (fs.existsSync(envPath)) {
        const envRaw = fs.readFileSync(envPath, 'utf8');
        const m = envRaw.match(/^\s*ALIPAY_RETURN_URL\s*=\s*(.+)\s*$/m);
        if (m) {
          const raw = m[1].trim().replace(/['"]/g,'');
          try {
            const u = new URL(raw);
            envPortal = u.origin; // 只取协议+主机+端口
          } catch {
            envPortal = raw;
          }
        }
      }
    } catch { /* ignore */ }
    // 1) 可选：Puppeteer 截图（优先）
    if (puppeteer) {
      const ensureDir = (d) => { if (!fs.existsSync(d)) fs.mkdirSync(d, { recursive: true }); };
      const snapDir = path.join(baseDir, 'auto');
      ensureDir(snapDir);
      // 默认端口：Portal 8220、Admin 8210（可被环境变量覆盖）
      const defaultPortal = envPortal || 'http://localhost:8220';
      const portalUrl = process.env.PPT_PORTAL_URL || defaultPortal;
      const adminUrl = process.env.PPT_ADMIN_URL || 'http://localhost:8210';
      const parseList = (v, def) => (v ? v.split(',').map(s => s.trim()).filter(Boolean) : def);
      const portalPaths = parseList(process.env.PPT_PORTAL_PATHS, ['/']);
      const adminPaths = parseList(process.env.PPT_ADMIN_PATHS, ['/']);
      // 统一 16:9 视口（默认 1600×900），可通过环境变量覆盖
      const parseSize = (txt) => {
        if (!txt) return null;
        const m = /^(\d+)\s*[xX]\s*(\d+)$/.exec(String(txt).trim());
        return m ? { width: parseInt(m[1], 10), height: parseInt(m[2], 10) } : null;
      };
      const sizeFromEnv = parseSize(process.env.PPT_SHOT_VIEWPORT) || null;
      const shotWidth = parseInt(process.env.PPT_VIEWPORT_WIDTH || (sizeFromEnv?.width ?? '1920'), 10);
      const shotHeight = parseInt(process.env.PPT_VIEWPORT_HEIGHT || (sizeFromEnv?.height ?? '1080'), 10);
      const dpr = parseFloat(process.env.PPT_SHOT_DPR || '1');
      const fullPage = (process.env.PPT_SHOT_FULLPAGE === '1' || process.env.PPT_SHOT_FULLPAGE === 'true') ? true : false; // 默认非全屏以保证 16:9
      const browser = await puppeteer.launch({
        headless: 'new',
        defaultViewport: { width: shotWidth, height: shotHeight, deviceScaleFactor: dpr },
      }).catch(() => null);
      if (browser) {
        const page = await browser.newPage();
        const tryShot = async (base, segs, prefix) => {
          const delay = (ms) => new Promise((r) => setTimeout(r, ms));
          for (const seg of segs) {
            const url = base.replace(/\/$/, '') + seg;
            try {
              const resp = await page.goto(url, { waitUntil: 'networkidle2', timeout: 20000 });
              const status = resp?.status?.() ?? 0;
              if (status >= 400) {
                console.warn(`⏭️ 跳过：${url} 返回状态 ${status}`);
                continue;
              }
              // 可选：校验选择器存在（确保是已渲染页面）
              const sel = process.env.PPT_VALIDATE_SELECTOR;
              if (sel) {
                try {
                  await page.waitForSelector(sel, { timeout: 3000 });
                } catch {
                  console.warn(`⏭️ 跳过：${url} 未找到选择器 ${sel}`);
                  continue;
                }
              }
              await delay(1200);
              const safe = seg === '/' ? 'home' : seg.replace(/[\\/#?&=:]/g, '_').slice(0,50);
              const png = path.join(snapDir, `${prefix}_${safe}.png`);
              await page.screenshot({ path: png, fullPage });
              collected.push(png);
              console.log(`🖼️ 截图完成：${url} -> ${png}`);
            } catch (e) {
              console.warn(`⚠️ 截图失败：${url} ${e.message}`);
            }
          }
        };
        await tryShot(portalUrl, portalPaths, 'portal');
        await tryShot(adminUrl, adminPaths, 'admin');
        await browser.close();
      } else {
        console.warn('⚠️ Puppeteer 启动失败，跳过自动截图（可安装 puppeteer 后重试）');
      }
    }
    // 2) 本地已有图片（仅在未限制仅 auto 时加载）
    if (!onlyAuto && fs.existsSync(baseDir)) {
      const exts = new Set(['.png', '.jpg', '.jpeg', '.webp']);
      const walk = (dir) => {
        for (const f of fs.readdirSync(dir)) {
          const p = path.join(dir, f);
          const st = fs.statSync(p);
          if (st.isDirectory()) {
            // 避免重复收集 auto（已在上面推送）
            if (path.basename(p) !== 'auto') walk(p);
          } else if (exts.has(path.extname(p).toLowerCase())) {
            collected.push(p);
          }
        }
      };
      walk(baseDir);
    }
    // 3) 限制最大截图数
    const maxShots = parseInt(process.env.PPT_MAX_SCREENSHOTS || '0', 10);
    if (maxShots > 0 && collected.length > maxShots) {
      return collected.slice(0, maxShots);
    }
    return collected;
  }

  // 将截图插入 PPT
  async function addScreenshotSlides(pptx, shots) {
    if (!shots.length) return;
    // 标题页
    addTitleBullets({ title: '界面截图', bullets: ['以下为 Portal/Admin 关键界面', '用于演示与评审'] });
    for (const img of shots) {
      const slide = pptx.addSlide();
      applyBackground(slide, 'content');
      addHeaderBar(slide, path.basename(img));
      addContentCard(slide);
      slide.addImage({ path: img, x: CONTENT_CARD.x + 0.2, y: CONTENT_CARD.y + 0.2, w: CONTENT_CARD.w - 0.4, h: CONTENT_CARD.h - 0.4, sizing: { type: 'contain', w: CONTENT_CARD.w - 0.4, h: CONTENT_CARD.h - 0.4 } });
      addFooter(slide);
    }
  }

  // 先下载主题图片（可选），再收集截图，便于总页数裁剪
  const imageMap = await fetchThemeImages(slides);
  const shots = await collectScreenshots();
  // 若设置最大总页数，则对基础 slides 进行裁剪
  const targetTotal = parseInt(process.env.PPT_MAX_SLIDES || '0', 10); // 0 表示不裁剪
  let baseSlides = slides.slice();
  const projectedTotal = baseSlides.length + (shots.length ? (1 + shots.length) : 0);
  if (targetTotal > 0 && projectedTotal > targetTotal) {
    const removableOrder = [
      'Q & A',
      'Demo 演示脚本',
      '成果与指标',
      '目录',
      '面向人群与价值',
      '报表与导出',
      '配置与环境',
      '前端组件亮点',
      '已交付里程碑',
    ];
    let needDrop = projectedTotal - targetTotal;
    const dropSet = new Set();
    for (const name of removableOrder) {
      if (needDrop <= 0) break;
      const idx = baseSlides.findIndex(s => s.title === name);
      if (idx !== -1) { dropSet.add(idx); needDrop--; }
    }
    for (let i = baseSlides.length - 1; needDrop > 0 && i >= 0; i--) {
      if (dropSet.has(i)) continue;
      const s = baseSlides[i];
      if (s.type === 'cover' || s.type === 'diagram-architecture' || s.type === 'thankyou') continue;
      dropSet.add(i); needDrop--;
    }
    baseSlides = baseSlides.filter((_, i) => !dropSet.has(i));
  }
  // 渲染
  for (let i = 0; i < baseSlides.length; i++) {
    const s = baseSlides[i];
    if (s.type === 'cover') addCover(s);
    else if (s.type === 'diagram-architecture') addArchitectureDiagram();
    else if (s.type === 'thankyou') addThankYou();
    else addTitleBullets(s, imageMap, i);
  }

  // 插入截图页（如有）
  await addScreenshotSlides(pptx, shots);

  await pptx.writeFile({ fileName: outFile });
  console.log(`✅ 已生成：${outFile}`);
}

build().catch((err) => {
  console.error('❌ 生成失败：', err);
  process.exit(1);
});
