import * as React from 'react';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { ThemeProvider } from 'styled-components';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

const theme = createTheme();
const Loading = () => {
    return (
        <ThemeProvider theme={theme}>
            <Spin />
        </ThemeProvider>
    );
};
export default Loading;
