import * as React from 'react';
import styled from 'styled-components';

import { space, SpaceProps, color, ColorProps, typography, TypographyProps as SystemTypographyProps } from '../system';

type SystemProps = SpaceProps & ColorProps & SystemTypographyProps;

type TypographyVariant = 'body' | 'secondary' | 'h1' | 'h2' | 'h3' | 'h4' | 'h5' | 'h6';

const TypographyBase = styled.span<{ variant: TypographyVariant } & SystemProps>`
    margin: 0px;
    padding: 0px;
    ${({ theme, variant }) => theme.typography[variant]}
    ${space}
  ${color}
  ${typography}
`;

type TypographyBaseProps = {
    variant?: TypographyVariant;
    children: React.ReactNode;
} & SystemProps;

export type TypographyProps = TypographyBaseProps &
    Omit<React.ComponentProps<typeof TypographyBase>, keyof TypographyBaseProps>;

const variantAsMap = {
    body: 'span',
    secondary: 'span',
    h1: 'h1',
    h2: 'h2',
    h3: 'h3',
    h4: 'h4',
    h5: 'h5',
    h6: 'h6',
} as const;

const Typography = ({ as: asProp, variant = 'body', children, ...props }: TypographyProps) => {
    const as = asProp || variantAsMap[variant] || 'span';

    return (
        <TypographyBase as={as} variant={variant} {...props}>
            {children}
        </TypographyBase>
    );
};

export default Typography;
