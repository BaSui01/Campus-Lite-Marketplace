/**
 * 关于我们页面
 * @author BaSui 😎
 * @description 介绍平台背景、团队、愿景
 * @date 2025-11-08
 */

import React from 'react';
import { LegalPage } from '../../components/LegalPage';

/**
 * 关于我们页面
 */
const AboutUs: React.FC = () => {
  return (
    <LegalPage title="关于我们" lastUpdated="2025年11月8日">
      <section>
        <h2>🎓 平台简介</h2>
        <p>
          <strong>校园轻享集市</strong>是一个专为大学生打造的校园二手交易与社交平台。
          我们致力于让校园内的闲置物品流动起来,帮助同学们轻松买卖二手商品、
          分享生活点滴、结识志同道合的朋友。
        </p>
        <p>
          在这里,你可以找到性价比超高的二手教材、电子产品、生活用品;
          也可以发布自己的闲置物品,让它们找到新主人;
          更可以参与校园活动、加入兴趣社区,体验丰富多彩的校园生活。
        </p>
      </section>

      <section>
        <h2>🚀 我们的愿景</h2>
        <blockquote>
          <strong>"让每一件闲置物品都能找到它的新归宿,让每一位同学都能享受便捷的校园生活。"</strong>
        </blockquote>
        <p>
          我们相信,通过技术的力量,可以让校园生活更加便利、环保、有趣。
          我们的目标是成为最受大学生喜爱的校园服务平台,
          为同学们创造一个安全、可信、活跃的校园社区。
        </p>
      </section>

      <section>
        <h2>💡 核心价值观</h2>
        <ul>
          <li>
            <strong>诚信为本</strong> - 我们建立完善的信用体系,保障每一笔交易的安全可靠
          </li>
          <li>
            <strong>用户至上</strong> - 我们倾听用户的声音,持续优化产品体验
          </li>
          <li>
            <strong>绿色环保</strong> - 我们倡导物品循环利用,为环保贡献一份力量
          </li>
          <li>
            <strong>开放共享</strong> - 我们鼓励分享与交流,构建温馨的校园社区
          </li>
          <li>
            <strong>创新驱动</strong> - 我们不断探索新技术,提升服务质量
          </li>
        </ul>
      </section>

      <section>
        <h2>✨ 平台特色</h2>
        <h3>🛡️ 安全保障</h3>
        <ul>
          <li>实名认证 - 校园邮箱验证,确保用户真实身份</li>
          <li>信用评分 - 多维度信用体系,筛选优质卖家</li>
          <li>交易担保 - 平台担保交易,保障买卖双方权益</li>
          <li>纠纷仲裁 - 专业客服团队,快速处理交易纠纷</li>
        </ul>

        <h3>💬 社交互动</h3>
        <ul>
          <li>实时聊天 - 买卖双方即时沟通,议价更方便</li>
          <li>校园社区 - 发帖互动,分享校园生活</li>
          <li>活动中心 - 参与校园活动,结识新朋友</li>
          <li>兴趣圈子 - 加入兴趣小组,找到同好</li>
        </ul>

        <h3>🎯 智能推荐</h3>
        <ul>
          <li>个性化推荐 - 基于浏览历史的智能推荐</li>
          <li>智能搜索 - 强大的搜索功能,快速找到心仪商品</li>
          <li>价格提醒 - 关注商品降价,第一时间通知</li>
          <li>新品推送 - 订阅感兴趣的分类,不错过好物</li>
        </ul>
      </section>

      <section>
        <h2>👥 关于团队</h2>
        <p>
          我们是一群热爱校园、热爱技术的年轻人。团队成员来自国内外知名高校,
          拥有丰富的产品设计和技术开发经验。我们深知大学生的需求和痛点,
          因此致力于打造最符合大学生使用习惯的产品。
        </p>
        <p>
          <strong>技术负责人:</strong> BaSui 😎 - 全栈工程师,擅长Java后端、React前端开发
        </p>
        <p>
          我们的团队充满激情与创造力,始终坚持用户第一的原则,
          不断优化产品功能,为同学们提供更好的服务。
        </p>
      </section>

      <section>
        <h2>📊 发展历程</h2>
        <table>
          <thead>
            <tr>
              <th>时间</th>
              <th>里程碑</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>2024年10月</td>
              <td>项目立项,开始需求调研与产品设计</td>
            </tr>
            <tr>
              <td>2024年12月</td>
              <td>完成核心功能开发,进入内测阶段</td>
            </tr>
            <tr>
              <td>2025年1月</td>
              <td>在首批合作高校开展试运营</td>
            </tr>
            <tr>
              <td>2025年3月</td>
              <td>正式上线,用户数突破1万</td>
            </tr>
            <tr>
              <td>2025年6月</td>
              <td>新增社区功能,用户日活突破3千</td>
            </tr>
            <tr>
              <td>2025年9月</td>
              <td>完成A轮融资,开启全国高校拓展</td>
            </tr>
            <tr>
              <td>2025年11月</td>
              <td>推出管理后台,优化平台治理能力</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section>
        <h2>📞 联系我们</h2>
        <p>如果你有任何问题、建议或合作意向,欢迎随时联系我们:</p>
        <ul>
          <li>
            <strong>客服邮箱:</strong>{' '}
            <a href="mailto:support@campus-market.com">support@campus-market.com</a>
          </li>
          <li>
            <strong>商务合作:</strong>{' '}
            <a href="mailto:business@campus-market.com">business@campus-market.com</a>
          </li>
          <li>
            <strong>技术支持:</strong>{' '}
            <a href="mailto:tech@campus-market.com">tech@campus-market.com</a>
          </li>
          <li>
            <strong>微信公众号:</strong> 校园轻享集市
          </li>
          <li>
            <strong>官方微博:</strong> @校园轻享集市
          </li>
        </ul>
      </section>

      <section>
        <h2>🙏 致谢</h2>
        <p>
          感谢所有支持和信任我们的同学们!你们的反馈和建议是我们前进的动力。
          感谢合作的高校和机构,为我们提供了宝贵的资源和支持。
          感谢团队的每一位成员,是你们的辛勤付出成就了今天的平台。
        </p>
        <p>
          让我们一起,用科技的力量让校园生活更美好!💪✨
        </p>
      </section>
    </LegalPage>
  );
};

export default AboutUs;
