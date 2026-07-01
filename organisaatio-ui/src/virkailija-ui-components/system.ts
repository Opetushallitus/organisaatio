export { space, color, flexbox, layout, typography, shadow } from 'styled-system';

export type { SpaceProps, ColorProps, FlexboxProps, LayoutProps, TypographyProps, ShadowProps } from 'styled-system';

import type { Theme } from './createTheme';

export const disabledStyle = ({ disabled, theme }: { disabled: boolean; theme: Theme }) => disabled && theme.disabled;
