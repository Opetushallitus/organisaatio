import * as React from 'react';
import styled, { css } from 'styled-components';
import { hideVisually } from 'polished';

import { disabledStyle } from '../system';

const RadioContainer = styled.div`
    position: relative;
    flex: 0;
    line-height: 0;
    top: 0.15em;
`;

const Icon = styled.svg`
    background-color: white;
    border-radius: 50%;
    width: 0.4em;
    height: 0.4em;
`;

const HiddenRadio = styled.input.attrs({ type: 'radio' })`
    ${hideVisually}
`;

const StyledRadio = styled.div<{ checked: boolean; error: boolean }>`
    position: relative;
    display: inline-block;
    position: relative;
    width: 1em;
    height: 1em;
    background-color: white;
    border: 1px solid ${({ theme }) => theme.colors.inputBorder};
    box-shadow: 0 0 0 0 transparent;
    border-radius: 50%;
    transition:
        box-shadow 0.25s,
        border-color 0.25s,
        background-color 0.25s;
    display: flex;
    justify-content: center;
    align-items: center;

    &:hover,
    ${HiddenRadio}:focus + & {
        border-color: ${({ theme, error }) => (error ? theme.colors.danger.main : theme.colors.primary.main)};
    }

    ${HiddenRadio}:focus + & {
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
            ${HiddenRadio}:focus + & {
                border-color: ${({ theme }) => theme.colors.danger.main};
            }

            ${HiddenRadio}:focus + & {
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
        & ${StyledRadio} {
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

type RadioBaseProps = {
    disabled?: boolean;
    checked?: boolean;
    children?: React.ReactNode;
    error?: boolean;
    className?: string;
    fullWidth?: boolean;
};

export type RadioProps = RadioBaseProps & Omit<React.ComponentProps<typeof HiddenRadio>, keyof RadioBaseProps>;

const Radio = React.forwardRef<HTMLInputElement, RadioProps>(
    ({ className, checked = false, children, error = false, disabled = false, ...props }: RadioProps, ref) => (
        <Label disabled={disabled} error={error} className={className}>
            <RadioContainer>
                <HiddenRadio checked={checked} disabled={disabled} ref={ref} {...props} />
                <StyledRadio checked={checked} error={error}>
                    <Icon />
                </StyledRadio>
            </RadioContainer>
            {children && <LabelWrapper>{children}</LabelWrapper>}
        </Label>
    )
);

Radio.displayName = 'Radio';

export default Radio;
