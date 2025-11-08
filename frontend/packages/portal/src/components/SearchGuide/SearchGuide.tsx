/**
 * 搜索引导组件 - 新手导航员！🧭
 *
 * @author BaSui 😎
 * @description 为首次使用搜索功能的用户提供引导
 * @date 2025-11-07
 */

import React, { useState, useEffect } from 'react';
import { SearchBar } from '../SearchBar';
import { useLocalStorage } from '@campus/shared/hooks';

interface SearchGuideProps {
  /** 引导完成回调 */
  onComplete?: () => void;
  /** 是否显示 */
  visible?: boolean;
  /** 强制显示引导 */
  forceShow?: boolean;
}

/**
 * 搜索引导步骤
 */
const GUIDE_STEPS = [
  {
    id: 'welcome',
    title: '👋 欢迎使用智能搜索',
    description: '让我帮您快速掌握搜索技巧，找到您需要的内容！',
    tips: [
      '支持关键词搜索',
      '可以使用筛选条件',
      '支持搜索历史记录'
    ]
  },
  {
    id: 'basic',
    title: '🔍 基础搜索技巧',
    description: '掌握基础搜索方法，快速定位目标内容',
    tips: [
      '输入商品名称、分类或描述',
      '使用空格分隔多个关键词',
      '大小写不敏感'
    ]
  },
  {
    id: 'advanced',
    title: '⚡ 高级搜索功能',
    description: '使用高级功能提升搜索精度',
    tips: [
      '使用筛选器精确过滤',
      '查看搜索历史',
      '使用快捷搜索标签'
    ]
  },
  {
    id: 'shortcuts',
    title: '🎯 快捷搜索技巧',
    description: '使用预定义标签快速搜索',
    tips: [
      '点击热门搜索标签',
      '自定义个人收藏',
      '查看最近搜索'
    ]
  }
];

/**
 * 搜索引导组件
 */
export const SearchGuide: React.FC<SearchGuideProps> = ({
  onComplete,
  visible: propVisible = true,
  forceShow = false
}) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [isVisible, setIsVisible] = useState(propVisible);
  const [showHighlight, setShowHighlight] = useState(false);

  const [guideCompleted, setGuideCompleted] = useLocalStorage('search-guide-completed', false);

  // 检查是否应该显示引导
  useEffect(() => {
    const shouldShow = forceShow || (propVisible && !guideCompleted);
    setIsVisible(shouldShow);

    if (shouldShow) {
      setCurrentStep(0);
      setShowHighlight(true);

      // 3秒后隐藏高亮
      setTimeout(() => setShowHighlight(false), 3000);
    }
  }, [propVisible, guideCompleted, forceShow]);

  const nextStep = () => {
    if (currentStep < GUIDE_STEPS.length - 1) {
      setCurrentStep(currentStep + 1);
      setShowHighlight(true);
      setTimeout(() => setShowHighlight(false), 3000);
    } else {
      completeGuide();
    }
  };

  const prevStep = () => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1);
      setShowHighlight(true);
      setTimeout(() => setShowHighlight(false), 3000);
    }
  };

  const skipGuide = () => {
    completeGuide();
  };

  const completeGuide = () => {
    setGuideCompleted(true);
    setIsVisible(false);
    setShowHighlight(false);
    onComplete?.();
  };

  const currentGuideStep = GUIDE_STEPS[currentStep];

  if (!isVisible) {
    return null;
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg shadow-2xl max-w-md w-full max-h-[80vh] overflow-y-auto">
        {/* 头部 */}
        <div className="sticky top-0 bg-white border-b border-gray-200 p-6 rounded-t-lg">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-bold text-gray-900">
              搜索功能引导
            </h2>
            <button
              onClick={skipGuide}
              className="text-gray-400 hover:text-gray-600 text-sm"
            >
              跳过
            </button>
          </div>

          {/* 进度指示器 */}
          <div className="mt-4">
            <div className="flex space-x-2">
              {GUIDE_STEPS.map((_, index) => (
                <div
                  key={index}
                  className={`h-2 flex-1 rounded-full transition-colors duration-300 ${
                    index <= currentStep ? 'bg-blue-600' : 'bg-gray-200'
                  }`}
                />
              ))}
            </div>
            <div className="mt-2 text-sm text-gray-600">
              步骤 {currentStep + 1} / {GUIDE_STEPS.length}
            </div>
          </div>
        </div>

        {/* 内容区域 */}
        <div className="p-6">
          {/* 步骤图标和标题 */}
          <div className="text-center mb-6">
            <div className="text-4xl mb-3">{currentGuideStep.title.split(' ')[0]}</div>
            <h3 className="text-lg font-semibold text-gray-900">
              {currentGuideStep.title.replace(/^[^\s]+\s/, '')}
            </h3>
            <p className="text-gray-600 mt-2">
              {currentGuideStep.description}
            </p>
          </div>

          {/* 技巧列表 */}
          <div className="space-y-3 mb-6">
            {currentGuideStep.tips.map((tip, index) => (
              <div key={index} className="flex items-start space-x-3">
                <span className="text-blue-500 text-sm mt-0.5">✓</span>
                <span className="text-gray-700">{tip}</span>
              </div>
            ))}
          </div>

          {/* 快捷搜索演示 */}
          {currentGuideStep.id === 'shortcuts' && (
            <div className="bg-gray-50 rounded-lg p-4 mb-6">
              <div className="text-sm font-medium text-gray-700 mb-3">
                快捷搜索示例：
              </div>
              <div className="flex flex-wrap gap-2">
                {['热门商品', '我的订单', '未付款', '待评价', '二手手机'].map((tag, index) => (
                  <button
                    key={index}
                    className="px-3 py-1 bg-white border border-gray-300 rounded-full text-sm hover:bg-blue-50 hover:border-blue-300 transition-colors"
                  >
                    {tag}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* 搜索演示 */}
          {currentGuideStep.id === 'basic' && (
            <div className="mb-6">
              <SearchBar
                searchState={{
                  keyword: '',
                  isLoading: false,
                  filters: {},
                  results: [],
                  totalCount: 0,
                  hasSearched: false
                }}
                onSearch={() => {}}
                onClear={() => {}}
                onQuickSearch={() => {}}
                searchHistory={[]}
                suggestions={[]}
                showQuickSearch={false}
                className={`border-2 ${showHighlight ? 'border-blue-500 shadow-lg' : 'border-gray-300'} transition-all duration-300`}
                placeholder="试试输入'iPhone' 或 '书籍'..."
                disabled={false}
              />
            </div>
          )}

          {/* 高级筛选演示 */}
          {currentGuideStep.id === 'advanced' && (
            <div className="bg-gray-50 rounded-lg p-4 mb-6">
              <div className="text-sm font-medium text-gray-700 mb-3">
                高级筛选示例：
              </div>
              <div className="grid grid-cols-2 gap-3 text-sm">
                <div className="flex items-center space-x-2">
                  <input type="checkbox" className="rounded text-blue-600" />
                  <span>仅显示有货商品</span>
                </div>
                <div className="flex items-center space-x-2">
                  <input type="checkbox" className="rounded text-blue-600" />
                  <span>价格：100-500元</span>
                </div>
                <div className="flex items-center space-x-2">
                  <input type="checkbox" className="rounded text-blue-600" />
                  <span>评分4星以上</span>
                </div>
                <div className="flex items-center space-x-2">
                  <input type="checkbox" className="rounded text-blue-600" />
                  <span>同校卖家</span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* 底部按钮 */}
        <div className="sticky bottom-0 bg-white border-t border-gray-200 p-6 rounded-b-lg flex justify-between">
          <button
            onClick={prevStep}
            disabled={currentStep === 0}
            className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            上一步
          </button>

          <div className="space-x-3">
            <button
              onClick={skipGuide}
              className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
            >
              跳过引导
            </button>
            <button
              onClick={nextStep}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              {currentStep === GUIDE_STEPS.length - 1 ? '完成' : '下一步'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SearchGuide;