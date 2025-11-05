/**
 * 首页 - 校园轻享集市门户首页 🏠
 * @author BaSui 😎
 * @description 整合Hero、HotGoods、Categories三大模块，充分利用共享层组件
 */

import React from 'react';
import Hero from './Hero';
import HotGoods from './HotGoods';
import Categories from './Categories';
import './Home.css';
import './Hero.css';
import './HotGoods.css';
import './Categories.css';

/**
 * 首页组件 - 整合三大模块
 * @description
 * - Hero区域：轮播图+搜索+快捷入口
 * - HotGoods区域：热门商品（使用共享层GoodsCard）
 * - Categories区域：分类导航
 */
const Home: React.FC = () => {
  return (
    <div className="home-page">
      {/* Hero区域：轮播图+搜索 */}
      <Hero />

      {/* 主内容容器 */}
      <div className="home-container">
        {/* 分类导航 */}
        <Categories />

        {/* 热门商品 */}
        <HotGoods />
      </div>
    </div>
  );
};



export default Home;
