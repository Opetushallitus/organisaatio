import * as React from 'react';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { ThemeProvider } from 'styled-components';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import styles from './Loading.module.css';
const theme = createTheme();
const Loading = () => {
    return (
        <ThemeProvider theme={theme}>
            <div className={styles.LoadingContainer}>
                <Spin />
            </div>
        </ThemeProvider>
    );
};
export default Loading;
