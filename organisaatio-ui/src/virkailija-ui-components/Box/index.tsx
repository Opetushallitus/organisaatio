import * as React from 'react';
import styled from 'styled-components';

import {
    space,
    SpaceProps,
    flexbox,
    FlexboxProps,
    color,
    ColorProps,
    layout,
    LayoutProps,
    typography,
    TypographyProps,
    shadow,
    ShadowProps,
} from '../system';

type SystemProps = SpaceProps & FlexboxProps & ColorProps & LayoutProps & TypographyProps & ShadowProps;

const Box = styled.div<SystemProps>`
    box-sizing: border-box;
    ${space}
    ${flexbox}
  ${color}
  ${layout}
  ${typography}
  ${shadow}
`;

export type BoxProps = React.ComponentProps<typeof Box>;

export default Box;
