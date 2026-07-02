import * as React from 'react';
import styled, { css } from 'styled-components';

import { disabledStyle } from '../system';

const CheckboxContainer = styled.div`
    position: relative;
    flex: 0;
    line-height: 0;
    top: 0.15em;
`;

const Icon = styled.svg`
    fill: none;
    stroke: white;
    stroke-width: 2px;
`;
const HiddenCheckbox = styled.input.attrs({ type: 'checkbox' })`
    border: 0;
    clip: rect(0 0 0 0);
    clip-path: inset(50%);
    height: 1px;
    margin: -1px;
    overflow: hidden;
    padding: 0;
    position: absolute;
    white-space: nowrap;
    width: 1px;
`;

const StyledCheckbox = styled.div<{ checked: boolean; error: boolean }>`
    display: inline-block;
    position: relative;
    width: 1em;
    height: 1em;
    background-color: white;
    border: 1px solid ${({ theme }) => theme.colors.inputBorder};
    box-shadow: 0 0 0 0 transparent;
    border-radius: ${({ theme }) => theme.radii[1]}px;
    transition:
        box-shadow 0.25s,
        border-color 0.25s,
        background-color 0.25s;

    &:hover,
    ${HiddenCheckbox}:focus + & {
        border-color: ${({ theme, error }) => (error ? theme.colors.danger.main : theme.colors.primary.main)};
    }

    ${HiddenCheckbox}:focus + & {
        box-shadow: 0 0 0 3px ${({ theme }) => theme.colors.primary.focusOutline};
    }

    ${({ checked }) =>
        checked &&
        css`
            background-color: ${({ theme }) => theme.colors.primary.main};
            border-color: ${({ theme }) => theme.colors.primary.main};
        `}

    ${({ error }) =>
        error &&
        css`
            border-color: ${({ theme }) => theme.colors.danger.main};

            &:hover,
            ${HiddenCheckbox}:focus + & {
                border-color: ${({ theme }) => theme.colors.danger.main};
            }

            ${HiddenCheckbox}:focus + & {
                box-shadow: 0 0 0 3px ${({ theme }) => theme.colors.danger.focusOutline};
            }
        `}

  ${Icon} {
        visibility: ${(props) => (props.checked ? 'visible' : 'hidden')};
    }
`;

const Label = styled.label<{ disabled: boolean; error: boolean }>`
    cursor: pointer;
    font-family: ${({ theme }) => theme.fonts.main};
    font-size: 1rem;
    display: inline-flex;
    line-height: 1.5;
    color: ${({ theme }) => theme.colors.text.primary};
    align-items: flex-start;

    &:hover {
        & ${StyledCheckbox} {
            border-color: ${({ theme, error }) => (error ? theme.colors.danger.main : theme.colors.primary.main)};
        }
    }

    ${({ error, theme }) =>
        error && {
            color: theme.colors.danger.main,
        }}

    ${disabledStyle}
`;

const LabelWrapper = styled.div`
    flex: 1;
    margin-left: 9px;
`;

const IndeterminateIndicator = styled.div`
    top: 50%;
    left: 50%;
    width: 0.5em;
    height: 0.5em;
    background-color: ${({ theme }) => theme.colors.primary.main};
    border-radius: 2px;
    position: absolute;
    transform: translate(-50%, -50%);
`;

type CheckboxBaseProps = {
    disabled?: boolean;
    checked?: boolean;
    children?: React.ReactNode;
    error?: boolean;
    className?: string;
    indeterminate?: boolean;
    fullWidth?: boolean;
};

export type CheckboxProps = CheckboxBaseProps &
    Omit<React.ComponentProps<typeof HiddenCheckbox>, keyof CheckboxBaseProps>;

const Checkbox = React.forwardRef<HTMLInputElement, CheckboxProps>(
    (
        { className, checked = false, children, error = false, disabled = false, indeterminate = false, ...props },
        ref
    ) => (
        <Label disabled={disabled} error={error} className={className}>
            <CheckboxContainer>
                <HiddenCheckbox checked={checked} disabled={disabled} ref={ref} {...props} />
                <StyledCheckbox checked={checked} error={error}>
                    <Icon viewBox="0 0 24 24">
                        <polyline points="20 6 9 17 4 12" />
                    </Icon>
                    {indeterminate && !checked ? <IndeterminateIndicator /> : null}
                </StyledCheckbox>
            </CheckboxContainer>
            {children && <LabelWrapper>{children}</LabelWrapper>}
        </Label>
    )
);

Checkbox.displayName = 'Checkbox';

export default Checkbox;
