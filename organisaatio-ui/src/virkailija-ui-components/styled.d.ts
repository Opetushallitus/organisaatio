import 'styled-components';

import { Theme } from './createTheme';

declare module 'styled-components' {
    // styled-components requires interface merging for theme augmentation.
    // eslint-disable-next-line @typescript-eslint/consistent-type-definitions, @typescript-eslint/no-empty-object-type
    export interface DefaultTheme extends Theme {}
}
