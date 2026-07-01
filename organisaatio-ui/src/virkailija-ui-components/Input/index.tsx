import * as React from 'react';
import styled, { css } from 'styled-components';

import { disabledStyle } from '../system';

export const InputBase = styled.input.attrs({ type: 'text' })<{
    error: boolean;
    disabled: boolean;
    hasPrefix?: boolean;
    hasSuffix?: boolean;
}>`
    border: 1px solid ${({ theme }) => theme.colors.inputBorder};
    color: ${({ theme }) => theme.colors.text.primary};
    font-family: ${({ theme }) => theme.fonts.main};
    line-height: ${({ theme }) => theme.lineHeights.body};
    border-radius: ${({ theme }) => theme.radii[1]}px;
    outline: none;
    padding: 6px 12px;
    font-size: ${({ theme }) => theme.fontSizes.body};
    transition:
        border-color 0.25s,
        box-shadow 0.25s;
    display: block;
    width: 100%;
    box-sizing: border-box;
    background-color: white;
    box-shadow: 0 0 0 0 transparent;

    &:hover,
    &:focus {
        border-color: ${({ theme }) => theme.colors.primary.main};
    }

    &:focus {
        box-shadow: 0 0 0 3px ${({ theme }) => theme.colors.primary.focusOutline};
    }

    ${({ error }) =>
        error &&
        css`
            border-color: ${({ theme }) => theme.colors.danger.main};

            &:hover,
            &:focus {
                border-color: ${({ theme }) => theme.colors.danger.main};
            }

            &:focus {
                box-shadow: 0 0 0 3px ${({ theme }) => theme.colors.danger.focusOutline};
            }
        `}

    ${disabledStyle}

  ${({ hasPrefix }) =>
        hasPrefix &&
        css`
            padding-left: 40px;
        `}

  ${({ hasSuffix }) =>
        hasSuffix &&
        css`
            padding-right: 40px;
        `}
`;

const InputWrapper = styled.div`
    position: relative;
`;

const AffixWrapper = styled.div<{ isPrefix?: boolean; isSuffix?: boolean }>`
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    line-height: 0;
    z-index: 2;

    ${({ isPrefix }) =>
        isPrefix &&
        css`
            left: 12px;
        `};

    ${({ isSuffix }) =>
        isSuffix &&
        css`
            right: 12px;
        `}
`;

type InputBaseProps = {
    error?: boolean;
    disabled?: boolean;
    prefix?: React.ReactNode;
    suffix?: React.ReactNode;
};

export type InputProps = InputBaseProps & Omit<React.ComponentProps<typeof InputBase>, keyof InputBaseProps>;

const Input = React.forwardRef<HTMLInputElement, InputProps>(
    ({ error = false, disabled = false, prefix, suffix, ...props }, ref) => {
        const hasPrefix = !!prefix;
        const hasSuffix = !!suffix;

        return (
            <InputWrapper>
                {hasPrefix && <AffixWrapper isPrefix={true}>{prefix}</AffixWrapper>}
                <InputBase
                    error={error}
                    disabled={disabled}
                    hasPrefix={hasPrefix}
                    hasSuffix={hasSuffix}
                    ref={ref}
                    {...props}
                />
                {hasSuffix && <AffixWrapper isSuffix={true}>{suffix}</AffixWrapper>}
            </InputWrapper>
        );
    }
);

Input.displayName = 'Input';

export default Input;
