import * as React from 'react';
import styled, { css, DefaultTheme } from 'styled-components';
import { disabledStyle } from '../system';
import HtmlButton from '../HtmlButton';
import { notIn } from '../utils/notIn';
import Spin from '../Spin';

type ButtonVariant = 'contained' | 'outlined' | 'text';

type ButtonSize = 'small' | 'medium';

type ButtonColor = 'primary' | 'success' | 'danger' | 'secondary';

const paddings = {
    contained: {
        medium: '6px 16px',
        small: '4px 10px',
    },
    outlined: {
        medium: '4px 16px',
        small: '2px 10px',
    },
    text: {
        medium: '6px 8px',
        small: '4px 5px',
    },
} as const;

const getOutlinedColorStyle = ({ color, theme }: { color: ButtonColor; theme: DefaultTheme }) => {
    const { outlineColor, hoverOutlineColor, focusOutlineColor } = theme.buttonVariants.outlined[color];

    return `
    padding: 4px 16px;
    border: 2px solid ${outlineColor};
    color: ${outlineColor};

    &:hover, &:active {
      border-color: ${hoverOutlineColor};
      color: ${hoverOutlineColor};
    }

    &:focus {
      box-shadow: 0 0 0 3px ${focusOutlineColor};
    }
  `;
};

const getContainedColorStyle = ({ color, theme }: { color: ButtonColor; theme: DefaultTheme }) => {
    const {
        backgroundColor,
        color: fontColor,
        hoverBackgroundColor,
        focusOutlineColor,
    } = theme.buttonVariants.contained[color];

    return `
    border-color: ${backgroundColor};
    background-color: ${backgroundColor};
    color: ${fontColor};

    &:hover, &:active {
      border-color: ${hoverBackgroundColor};
      background-color: ${hoverBackgroundColor};
    }

    &:focus {
      box-shadow: 0 0 0 3px ${focusOutlineColor};
    }
  `;
};

const getLoadingColorStyle = ({ theme }: { theme: DefaultTheme }) => {
    const { outlineColor, hoverOutlineColor } = theme.buttonVariants.outlined['secondary'];

    return `
    background-color: transparent;
    padding: 4px 16px;
    border: 2px solid ${outlineColor};
    color: ${outlineColor};

    &:hover, &:active {
      background-color: transparent;
      border-color: ${hoverOutlineColor};
      color: ${hoverOutlineColor};
    }
    &:focus {
      box-shadow: none;
    }
    & > div.Oph-Spinner {
      margin-left: 0.5rem;
    }
  `;
};

const getTextVariantColorStyle = ({ color, theme }: { color: ButtonColor; theme: DefaultTheme }) => {
    const { color: fontColor, hoverColor, focusOutlineColor } = theme.buttonVariants.text[color];

    return `
    border-color: transparent;
    background-color: transparent;
    color: ${fontColor};

    &:hover, &:active {
      color: ${hoverColor}
    }

    &:focus {
      box-shadow: 0 0 0 3px ${focusOutlineColor};
    }
  `;
};

const getVariantStyle = ({ variant }: { variant: ButtonVariant }) => {
    if (variant === 'outlined') {
        return css`
            background-color: transparent;
            ${getOutlinedColorStyle}
        `;
    } else if (variant === 'contained') {
        return css`
            ${getContainedColorStyle}
        `;
    } else if (variant === 'text') {
        return css`
            ${getTextVariantColorStyle}
            padding: 6px 8px;
        `;
    }
    return undefined;
};

const getLoadingStyle = ({ loading }: { loading: boolean }) =>
    loading &&
    css`
        cursor: progress;
        ${getLoadingColorStyle}
        & > div {
            margin-left: 0.5rem;
        }
    `;

const getSizeStyle = ({ size, variant, theme }: { size: ButtonSize; variant: ButtonVariant; theme: DefaultTheme }) => {
    return {
        fontSize: size === 'small' ? '0.85rem' : theme.fontSizes.body,
        padding: paddings[variant][size],
    };
};

const ButtonBase = styled.button.withConfig({
    shouldForwardProp: notIn(['fullWidth', 'loading']),
})<{
    size: ButtonSize;
    variant: ButtonVariant;
    disabled: boolean;
    color: ButtonColor;
    fullWidth: boolean;
    loading: boolean;
}>`
    cursor: pointer;
    border: 0px none;
    box-shadow: 0 0 0 0 transparent;
    outline: none;
    padding: 6px 16px;
    border-radius: ${({ theme }) => theme.radii[1]}px;
    font-family: ${({ theme }) => theme.fonts.main};
    font-size: ${({ theme }) => theme.fontSizes.body};
    line-height: ${({ theme }) => theme.lineHeights.body};
    white-space: nowrap;
    display: inline-flex;
    align-items: center;
    box-sizing: border-box;
    transition:
        box-shadow 0.25s,
        background-color 0.25s,
        border-color 0.25s,
        color 0.25s;
    font-weight: ${({ theme }) => theme.fontWeights.bold};
    text-decoration: none;

    ${getVariantStyle}
    ${getSizeStyle}
  ${disabledStyle}
  ${getLoadingStyle}

  ${({ fullWidth }) =>
        fullWidth &&
        css`
            width: 100%;
            justify-content: center;
        `}
`;

type ButtonBaseProps = {
    size?: ButtonSize;
    variant?: ButtonVariant;
    color?: ButtonColor;
    disabled?: boolean;
    fullWidth?: boolean;
    loading?: boolean;
    children: React.ReactNode;
};

export type ButtonProps = ButtonBaseProps & Omit<React.ComponentProps<typeof ButtonBase>, keyof ButtonBaseProps>;

const Button = React.forwardRef<HTMLElement, ButtonProps>(
    (
        {
            size = 'medium',
            variant = 'contained',
            color = 'primary',
            as: asProp = HtmlButton,
            disabled = false,
            fullWidth = false,
            loading = false,
            children,
            ...props
        },
        ref
    ) => {
        return (
            <ButtonBase
                ref={ref as React.Ref<HTMLButtonElement>}
                size={size}
                variant={variant}
                disabled={disabled || loading}
                color={color}
                as={asProp}
                fullWidth={fullWidth}
                loading={loading}
                {...props}
            >
                {children} {loading && <Spin size={'small'} />}
            </ButtonBase>
        );
    }
);

Button.displayName = 'Button';

export default Button;
