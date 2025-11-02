/**
 * 页面过渡动画组件
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React from 'react';
import { CSSTransition, TransitionGroup } from 'react-transition-group';
import styled from '@emotion/styled';

interface PageTransitionProps {
  children: React.ReactNode;
  animationType?: 'fade' | 'slide' | 'scale';
  duration?: number;
}

interface StyledPageProps {
  type: string;
  duration: number;
}

const StyledPage = styled.div<StyledPageProps>`
  .page-enter {
    opacity: 0;
    transform: translateY(20px);
  }

  .page-enter-active {
    opacity: 1;
    transform: translateY(0);
    transition: all ${({ duration })}ms ease-in-out;
  }

  .page-exit {
    opacity: 1;
    transform: translateY(0);
  }

  .page-exit-active {
    opacity: 0;
    transform: ${({ type }) => {
      switch (type) {
        case 'fade':
          return 'translateY(20px)';
        case 'slide':
          return 'translateX(-20px)';
        case 'scale':
          return 'scale(0.95)';
        default:
          return 'translateY(20px)';
      }
    }};
    transition: all ${({ duration })}ms ease-in-out;
  }
`;

const PageTransition: React.FC<PageTransitionProps> = ({
  children,
  animationType = 'fade',
  duration = 300,
}) => {
  return (
    <TransitionGroup>
      <CSSTransition
        timeout={duration}
        classNames={{
          enter: 'page-enter',
          enterActive: 'page-enter-active',
          exit: 'page-exit',
          exitActive: 'page-exit-active',
        }}
        // key={location.key} // 添加key以触发动画
      >
        <StyledPage type={animationType} duration={duration}>
          {children}
        </StyledPage>
      </CSSTransition>
    </TransitionGroup>
  );
};

export default PageTransition;
