import * as React from 'react';
import styled, { keyframes, css } from 'styled-components';

import Box from '../Box';
import Typography from '../Typography';

type SpinColor = 'primary' | 'white';

type SpinSize = 'small' | 'medium' | 'large';

const spin = keyframes`
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
`;

const baseSize = 35;
const baseBorderWidth = 4;

const sizeMultipliers = {
    small: 0.5,
    medium: 1,
    large: 1.5,
} as const;

const CenterContainer = styled.div`
    display: flex;
    justify-content: center;
`;

const SpinCircle = styled.div<{ size: SpinSize; color: SpinColor }>`
    width: ${baseSize}px;
    height: ${baseSize}px;
    border-style: solid;
    border-color: rgba(0, 0, 0, 0.1);
    border-top-color: ${({ theme, color }) => (color === 'white' ? theme.colors.bg_white : theme.colors.primary.main)};
    border-radius: 50%;
    animation: ${spin};
    animation-duration: 1s;
    animation-iteration-count: infinite;
    animation-timing-function: cubic-bezier(0.215, 0.61, 0.355, 1);
    display: inline-flex;
    flex-grow: 0;
    ${({ size }) => css`
        width: ${(sizeMultipliers[size] || 1) * baseSize}px;
        height: ${(sizeMultipliers[size] || 1) * baseSize}px;
        border-width: ${(sizeMultipliers[size] || 1) * baseBorderWidth}px;
    `};
`;

type SpinBaseProps = {
    color?: SpinColor;
    size?: SpinSize;
    center?: boolean;
    disableTypography?: boolean;
    children?: React.ReactNode;
};

export type SpinProps = Omit<React.ComponentProps<typeof SpinCircle>, keyof SpinBaseProps> & SpinBaseProps;

export const Spin = ({
    color = 'primary',
    size = 'medium',
    center = false,
    disableTypography = false,
    children,
    className = 'Oph-Spinner',
    ...props
}: SpinProps) => {
    const spinProps = { size, color, ...props, className };

    const content = children ? (
        <Box display="inline-flex" alignItems="center">
            <SpinCircle {...spinProps} />
            {disableTypography ? <Box pl={2}>{children}</Box> : <Typography pl={2}>{children}</Typography>}
        </Box>
    ) : (
        <SpinCircle {...spinProps} />
    );

    return center ? <CenterContainer className={className}>{content}</CenterContainer> : content;
};

export default Spin;
