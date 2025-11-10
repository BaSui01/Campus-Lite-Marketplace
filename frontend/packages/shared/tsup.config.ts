import { defineConfig } from 'tsup';

export default defineConfig({
  // 入口文件
  entry: {
    index: 'src/index.ts',
    'api/index': 'src/api/index.ts',
    'components/index': 'src/components/index.ts',
    'utils/index': 'src/utils/index.ts',
  },

  // 输出格式：ESM + CJS
  format: ['esm', 'cjs'],

  // 生成类型声明文件
  dts: true,

  // 代码分割
  splitting: true,

  // Source Map
  sourcemap: true,

  // 清理输出目录
  clean: true,

  // 外部依赖（不打包）
  external: ['react', 'react-dom', 'react-router-dom'],

  // Tree Shaking
  treeshake: true,

  // 压缩代码（生产环境）
  minify: process.env.NODE_ENV === 'production',
});
