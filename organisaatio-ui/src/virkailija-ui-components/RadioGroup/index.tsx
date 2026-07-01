import * as React from 'react';
import styled, { css } from 'styled-components';
import Radio from '../Radio';

const Container = styled.div<{ isLast: boolean }>`
    ${({ isLast }) =>
        !isLast &&
        css`
            margin-bottom: 4px;
        `}
`;

type RadioGroupOption = { value: string; label: React.ReactNode };

type RadioGroupChild = React.ReactElement<{
    checked?: boolean;
    value: string;
    onChange?: React.ChangeEventHandler<HTMLInputElement>;
    disabled?: boolean;
    error?: boolean;
}>;

export type RadioGroupProps = {
    children?: RadioGroupChild[];
    value: string;
    onChange?: React.ChangeEventHandler<HTMLInputElement>;
    disabled?: boolean;
    error?: boolean;
    options?: RadioGroupOption[];
    getIsDisabled?: (value: string) => boolean;
};

export const RadioGroup = ({
    value,
    onChange,
    disabled = false,
    options,
    error = false,
    children: childrenProp,
    getIsDisabled = () => false,
}: RadioGroupProps) => {
    let children: React.ReactNode = null;

    if (childrenProp) {
        const validChildren = React.Children.toArray(childrenProp).filter((c) =>
            React.isValidElement(c)
        ) as RadioGroupChild[];

        const childrenCount = React.Children.count(validChildren);

        children = validChildren.map((child, index) => {
            const checked = value !== undefined && child?.props?.value === value;
            const element = React.cloneElement(child, {
                checked,
                onChange,
                disabled: disabled || getIsDisabled(child.props?.value) || child.props?.disabled,
                error,
            });

            return (
                <Container key={child?.props?.value} isLast={index === childrenCount - 1}>
                    {element}
                </Container>
            );
        });
    } else if (Array.isArray(options)) {
        children = options.map(({ value: optionValue, label }, index) => (
            <Container isLast={index === options.length - 1} key={optionValue}>
                <Radio
                    checked={value !== undefined && value === optionValue}
                    onChange={onChange}
                    value={optionValue}
                    error={error}
                    disabled={disabled || getIsDisabled(optionValue)}
                >
                    {label}
                </Radio>
            </Container>
        ));
    }

    return <>{children}</>;
};

export default RadioGroup;
