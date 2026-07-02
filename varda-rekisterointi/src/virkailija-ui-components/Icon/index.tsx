import * as React from 'react';
import styled from 'styled-components';

import { color, ColorProps, space, SpaceProps, typography, TypographyProps } from '../system';

type SystemProps = ColorProps & SpaceProps & TypographyProps;

const IconBase = styled.i<SystemProps>`
    font-size: 1.5rem;
    ${color};
    ${space};
    ${typography};
`;

type MaterialIconTheme = 'outlined' | 'two-tone' | 'round' | 'sharp' | undefined;

export type IconProps = React.ComponentProps<typeof IconBase> & {
    type: string;
    variant: MaterialIconTheme;
};

const Icon = React.forwardRef<HTMLSpanElement, IconProps>(({ type, variant, className = '', ...props }, ref) => {
    const iconClassName = `material-icons${variant ? '-' + variant : ''} ${className}`;

    return (
        <IconBase ref={ref} className={iconClassName} {...props}>
            {type}
        </IconBase>
    );
});

Icon.displayName = 'Icon';

export default Icon;
