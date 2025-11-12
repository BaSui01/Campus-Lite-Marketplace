/**
 * Emoji引导组件 - 表情大师训练营！😎
 *
 * @author BaSui 😎
 * @description 为首次使用Emoji功能的用户提供引导
 * @date 2025-11-07
 */

import React, { useState, useEffect } from 'react';
import { EmojiPicker } from '../EmojiPicker';
import { useLocalStorage } from '@campus/shared/hooks';

interface EmojiGuideProps {
  /** 引导完成回调 */
  onComplete?: () => void;
  /** 是否显示 */
  visible?: boolean;
  /** 强制显示引导 */
  forceShow?: boolean;
}

/**
 * Emoji引导步骤
 */
const GUIDE_STEPS = [
  {
    id: 'welcome',
    title: '🎉 欢迎使用表情功能',
    description: '让我帮您快速掌握表情使用技巧，让聊天更有趣！',
    tips: [
      '快速插入表情符号',
      '分类浏览表情',
      '支持表情搜索',
      '收藏常用表情'
    ]
  },
  {
    id: 'picker',
    title: '🎯 表情选择器',
    description: '掌握表情选择器的使用方法',
    tips: [
      '点击表情图标打开选择器',
      '分类浏览不同类型的表情',
      '支持搜索表情名称',
      '点击表情即可插入'
    ]
  },
  {
    id: 'favorites',
    title: '⭐ 收藏功能',
    description: '管理您的常用表情收藏',
    tips: [
      '右键表情添加到收藏',
      '在收藏夹快速访问',
      '自定义表情分类',
      '导出分享收藏'
    ]
  },
  {
    id: 'shortcuts',
    title: '⚡ 快捷技巧',
    description: '使用快捷方式提升表情使用效率',
    tips: [
      '输入:表情名称快速搜索',
      '使用表情代码快速输入',
      '自定义表情快捷键',
      '批量发送表情包'
    ]
  }
];

/**
 * Emoji引导组件
 */
export const EmojiGuide: React.FC<EmojiGuideProps> = ({
  onComplete,
  visible: propVisible = true,
  forceShow = false
}) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [isVisible, setIsVisible] = useState(propVisible);
  const [showHighlight, setShowHighlight] = useState(false);

  const [guideCompleted, setGuideCompleted] = useLocalStorage('emoji-guide-completed', false);

  // 检查是否应该显示引导
  useEffect(() => {
    const shouldShow = forceShow || (visible && !guideCompleted);
    setIsVisible(shouldShow);

    if (shouldShow) {
      setCurrentStep(0);
      setShowHighlight(true);

      // 3秒后隐藏高亮
      setTimeout(() => setShowHighlight(false), 3000);
    }
  }, [visible, guideCompleted, forceShow]);

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
              表情功能引导
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

          {/* 表情选择器演示 */}
          {currentStep.id === 'picker' && (
            <div className="mb-6">
              <div className="text-sm font-medium text-gray-700 mb-3">
                试试点击下面的表情按钮：
              </div>
              <div className={`border-2 rounded-lg p-4 transition-all duration-300 ${
                showHighlight ? 'border-blue-500 shadow-lg bg-blue-50' : 'border-gray-300 bg-white'
              }`}>
                <EmojiPicker
                  onEmojiSelect={(emoji) => {
                    // 模拟选择表情
                    console.log('选中表情:', emoji);
                  }}
                  onClose={() => {}}
                  visible={true}
                  config={{
                    showFavoriteTab: true,
                    showSearch: true,
                    emojisPerRow: 6,
                    maxRows: 2,
                    theme: 'light'
                  }}
                  className="demo-emoji-picker"
                />
              </div>
            </div>
          )}

          {/* 收藏功能演示 */}
          {currentStep.id === 'favorites' && (
            <div className="bg-gray-50 rounded-lg p-4 mb-6">
              <div className="text-sm font-medium text-gray-700 mb-3">
                我的收藏示例：
              </div>
              <div className="flex flex-wrap gap-2">
                {['😂', '❤️', '👍', '🎉', '🔥', '😎', '🙏', '💪'].map((emoji, index) => (
                  <button
                    key={index}
                    className={`text-2xl p-2 rounded-lg transition-all duration-200 ${
                      showHighlight ? 'bg-yellow-100 border-2 border-yellow-400' : 'bg-white border border-gray-300'
                    } hover:bg-yellow-50`}
                  >
                    {emoji}
                  </button>
                ))}
              </div>
              <div className="text-xs text-gray-500 mt-2">
                提示：右键点击表情可以添加到收藏
              </div>
            </div>
          )}

          {/* 快捷键演示 */}
          {currentStep.id === 'shortcuts' && (
            <div className="bg-gray-50 rounded-lg p-4 mb-6">
              <div className="text-sm font-medium text-gray-700 mb-3">
                常用表情快捷键：
              </div>
              <div className="space-y-2 text-sm">
                <div className="flex items-center justify-between">
                  <span className="font-mono bg-white px-2 py-1 rounded border">:)</span>
                  <span>😊</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="font-mono bg-white px-2 py-1 rounded border">:D</span>
                  <span>😃</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="font-mono bg-white px-2 py-1 rounded border">:P</span>
                  <span>😛</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="font-mono bg-white px-2 py-1 rounded border">:heart:</span>
                  <span>❤️</span>
                </div>
              </div>
              <div className="text-xs text-gray-500 mt-2">
                输入代码后按空格键即可转换为表情
              </div>
            </div>
          )}

          {/* 热门表情包推荐 */}
          {currentStep.id === 'welcome' && (
            <div className="bg-blue-50 rounded-lg p-4 mb-6">
              <div className="text-sm font-medium text-blue-900 mb-3">
                🌟 热门表情包推荐：
              </div>
              <div className="grid grid-cols-3 gap-2">
                {[
                  { name: '可爱萌宠', emoji: '🐱🐶🐰' },
                  { name: '节日庆祝', emoji: '🎂🎊🎁' },
                  { name: '日常心情', emoji: '😊😅🤔' },
                  { name: '运动健身', emoji: '💪🏃🏋️' },
                  { name: '美食诱惑', emoji: '🍕🍔🍰' },
                  { name: '学习工作', emoji: '📚💻☕' }
                ].map((pack, index) => (
                  <button
                    key={index}
                    className="p-2 bg-white rounded-lg border border-blue-200 hover:bg-blue-100 transition-colors text-center"
                  >
                    <div className="text-lg">{pack.emoji}</div>
                    <div className="text-xs text-gray-600 mt-1">{pack.name}</div>
                  </button>
                ))}
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

export default EmojiGuide;