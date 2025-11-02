/**
 * 居中组件
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React from 'react';
import styled from '@emotion/styled';

interface CenterProps {
  children: React.ReactNode;
  style?: React.CSSProperties;
}

const StyledCenter = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
`;

const Center: React.FC<CenterProps> = ({ children, style }) => {
  return <StyledCenter style={style}>{children}</StyledCenter>;
};

export default Center;
