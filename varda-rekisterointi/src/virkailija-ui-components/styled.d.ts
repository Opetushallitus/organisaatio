/* eslint-disable @typescript-eslint/consistent-type-definitions, @typescript-eslint/no-empty-object-type */
import 'styled-components';

import { Theme } from './createTheme';

declare module 'styled-components' {
    export interface DefaultTheme extends Theme {}
}
