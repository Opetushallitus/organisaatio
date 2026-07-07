import * as React from 'react';
import { ThemeProvider } from 'styled-components';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { BrowserRouter, Route, Routes, Navigate } from 'react-router';
import Notification from './components/Notification/Notification';
import { BASE_PATH } from './contexts/constants';
import Loading from './components/Loading/Loading';
import { ErrorBoundary } from './index';
import { frontPropertiesAtom } from './api/config';
import { useAtom } from 'jotai';

const theme = createTheme();
const VirheSivu = React.lazy(() => import('./components/Sivut/VirheSivu/VirheSivu'));
const LomakeSivu = React.lazy(() => import('./components/Sivut/LomakeSivu/LomakeSivu'));
const TaulukkoSivu = React.lazy(() => import('./components/Sivut/TaulukkoSivu/TaulukkoSivu'));
const Ryhmat = React.lazy(() => import('./components/Sivut/Ryhmat/Ryhmat'));
const RyhmanMuokkaus = React.lazy(() => import('./components/Sivut/Ryhmat/Muokkaus/RyhmanMuokkaus'));
const UusiToimijaLomake = React.lazy(() => import('./components/Sivut/LomakeSivu/UusiToimija/UusiToimijaLomake'));
const OsoitteetSivu = React.lazy(() => import('./components/Sivut/OsoitteetSivu/OsoitteetSivu'));

const OrganisaatioApp: React.FC = () => {
    const [frontProperties] = useAtom(frontPropertiesAtom);
    return (
        <ErrorBoundary>
            <React.Suspense fallback={<Loading />}>
                <ThemeProvider theme={theme}>
                    <Notification />
                    <BrowserRouter basename={BASE_PATH}>
                        <Routes>
                            <Route path="/" element={<Navigate to="/organisaatiot" replace />} />
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
