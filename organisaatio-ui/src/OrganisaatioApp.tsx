import * as React from 'react';
import { ThemeProvider } from 'styled-components';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { registerLocale } from 'react-datepicker';
import { enGB, fi, sv } from 'date-fns/locale';
import { BrowserRouter, Route, Switch } from 'react-router-dom';
import VirheSivu from './components/Sivut/VirheSivu/VirheSivu';
import LomakeSivu from './components/Sivut/LomakeSivu/LomakeSivu';
import TaulukkoSivu from './components/Sivut/TaulukkoSivu/TaulukkoSivu';
import Ryhmat from './components/Sivut/Ryhmat/Ryhmat';
import RyhmanMuokkaus from './components/Sivut/Ryhmat/Muokkaus/RyhmanMuokkaus';
import UusiToimijaLomake from './components/Sivut/LomakeSivu/UusiToimija/UusiToimijaLomake';
import Notification from './components/Notification/Notification';
import { BASE_PATH } from './contexts/constants';
import { useAtom } from 'jotai';
import { frontPropertiesAtom } from './api/config';

const theme = createTheme();

const OrganisaatioApp: React.FC = () => {
    useAtom(frontPropertiesAtom);
    registerLocale('fi', fi);
    registerLocale('sv', sv);
    registerLocale('en', enGB);

    return (
        <ThemeProvider theme={theme}>
            <Notification />
            <BrowserRouter basename={BASE_PATH}>
                <Switch>
                    <Route path={'/organisaatiot'} exact component={TaulukkoSivu} />
                    <Route exact path={'/lomake/uusi'} component={UusiToimijaLomake} />
                    <Route path={'/lomake/:oid'} component={LomakeSivu} />
                    <Route path={'/ryhmat'} exact component={Ryhmat} />
                    <Route exact path={'/ryhmat/uusi'} component={(props) => <RyhmanMuokkaus {...props} isNew />} />
                    <Route path={'/ryhmat/:oid'} component={RyhmanMuokkaus} />
                    <Route path={'*'}>
                        <VirheSivu>{'ERROR_404'}</VirheSivu>
                    </Route>
                </Switch>
            </BrowserRouter>
        </ThemeProvider>
    );
};

export default OrganisaatioApp;
