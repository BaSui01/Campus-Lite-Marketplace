/**
 * 🏆 排行榜组件 - BaSui 搞笑专业版 😎
 *
 * 展示热门商品或活跃用户的排行榜
 *
 * @author BaSui
 * @date 2025-11-07
 */

import React from 'react';
import { List, Avatar, Tag, Empty } from 'antd';
import { TrophyOutlined, FireOutlined } from '@ant-design/icons';
import type { RankingItem } from '../../services/statistics';

interface RankingListProps {
  data: RankingItem[];
  type: 'goods' | 'users'; // 商品排行 或 用户排行
}

/**
 * 排行榜组件
 */
const RankingList: React.FC<RankingListProps> = ({ data, type }) => {
  if (!data || data.length === 0) {
    return <Empty description="暂无排行数据" />;
  }

  // 排名颜色
  const getRankColor = (index: number) => {
    if (index === 0) return '#ffd700'; // 金色
    if (index === 1) return '#c0c0c0'; // 银色
    if (index === 2) return '#cd7f32'; // 铜色
    return '#8c8c8c'; // 灰色
  };

  // 排名图标
  const getRankIcon = (index: number) => {
    if (index < 3) {
      return <TrophyOutlined style={{ color: getRankColor(index), fontSize: 20 }} />;
    }
    return <span style={{ color: '#8c8c8c', fontWeight: 600 }}>{index + 1}</span>;
  };

  return (
    <List
      itemLayout="horizontal"
      dataSource={data}
      renderItem={(item, index) => (
        <List.Item
          key={item.id}
          extra={
            <Tag color={index < 3 ? 'gold' : 'default'} icon={<FireOutlined />}>
              {type === 'goods' ? `${item.value} 次浏览` : `${item.value} 个物品`}
            </Tag>
          }
        >
          <List.Item.Meta
            avatar={
              <div style={{ width: 40, textAlign: 'center' }}>
                {getRankIcon(index)}
              </div>
            }
            title={
              <span style={{ fontWeight: index < 3 ? 600 : 400 }}>
                {item.name}
              </span>
            }
            description={
              type === 'goods' && item.category ? (
                <Tag color="blue">{item.category}</Tag>
              ) : null
            }
          />
        </List.Item>
      )}
      style={{ maxHeight: 400, overflow: 'auto' }}
    />
  );
};

export default RankingList;
