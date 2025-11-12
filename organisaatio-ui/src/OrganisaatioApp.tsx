import * as React from 'react';
import { ThemeProvider } from 'styled-components';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { registerLocale } from 'react-datepicker';
import { enGB, fi, sv } from 'date-fns/locale';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import VirheSivu from './components/Sivut/VirheSivu/VirheSivu';
import LomakeSivu from './components/Sivut/LomakeSivu/LomakeSivu';
import TaulukkoSivu from './components/Sivut/TaulukkoSivu/TaulukkoSivu';
import Ryhmat from './components/Sivut/Ryhmat/Ryhmat';
import RyhmanMuokkaus from './components/Sivut/Ryhmat/Muokkaus/RyhmanMuokkaus';
import UusiToimijaLomake from './components/Sivut/LomakeSivu/UusiToimija/UusiToimijaLomake';
import Notification from './components/Notification/Notification';
import { BASE_PATH } from './contexts/constants';
import Loading from './components/Loading/Loading';
import { ErrorBoundary } from './index';
import OsoitteetSivu from './components/Sivut/OsoitteetSivu/OsoitteetSivu';
import { frontPropertiesAtom } from './api/config';
import { useAtom } from 'jotai';

const theme = createTheme();

const OrganisaatioApp: React.FC = () => {
    registerLocale('fi', fi);
    registerLocale('sv', sv);
    registerLocale('en', enGB);
    const [frontProperties] = useAtom(frontPropertiesAtom);
    return (
        <ErrorBoundary>
            <React.Suspense fallback={<Loading />}>
                <ThemeProvider theme={theme}>
                    <Notification />
                    <BrowserRouter basename={BASE_PATH}>
                        <Routes>
                            <Route path="organisaatiot" element={<TaulukkoSivu />} />
                            <Route path="osoitteet/*" element={<OsoitteetSivu frontProperties={frontProperties} />} />
                            <Route path="lomake/uusi" element={<UusiToimijaLomake />} />
                            <Route path="lomake/:oid" element={<LomakeSivu />} />
                            <Route path="ryhmat" element={<Ryhmat />} />
                            <Route path="ryhmat/uusi" element={<RyhmanMuokkaus isNew />} />
                            <Route path="ryhmat/:oid" element={<RyhmanMuokkaus />} />
                            <Route path="*" element={<VirheSivu>{'ERROR_404'}</VirheSivu>} />
                        </Routes>
                    </BrowserRouter>
                </ThemeProvider>
            </React.Suspense>
        </ErrorBoundary>
    );
};

export default OrganisaatioApp;
