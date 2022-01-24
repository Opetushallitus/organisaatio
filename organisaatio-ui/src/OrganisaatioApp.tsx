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
import Loading from './components/Loading/Loading';
import useKoodisto from './api/koodisto';
import Notification from './components/Notification/Notification';
import { KoodistoContext, KoodistoImpl } from './contexts/KoodistoContext';
import { casMeAtom } from './contexts/CasMeContext';
import { BASE_PATH } from './contexts/constants';
import { useAtom } from 'jotai';
import { frontPropertiesAtom } from './api/config';

const theme = createTheme();

const OrganisaatioApp: React.FC = () => {
    useAtom(frontPropertiesAtom);
    registerLocale('fi', fi);
    registerLocale('sv', sv);
    registerLocale('en', enGB);

    const [casData] = useAtom(casMeAtom);

    const { data: kunnat, loading: kunnatLoading, error: kunnatError } = useKoodisto('KUNTA');
    const { data: ryhmaTyypit, loading: ryhmaTyypitLoading, error: ryhmaTyypitError } = useKoodisto('RYHMATYYPIT');

    const { data: kayttoRyhmat, loading: kayttoRyhmatLoading, error: kayttoRyhmatError } = useKoodisto('KAYTTORYHMAT');
    const { data: ryhmanTilat, loading: ryhmanTilatLoading, error: ryhmanTilatError } = useKoodisto('RYHMANTILA');
    const { data: vuosiluokat, loading: vuosiluokatLoading, error: vuosiluokatError } = useKoodisto('VUOSILUOKAT');
    const { data: oppilaitostyyppi, loading: oppilaitostyyppiLoading, error: oppilaitostyyppiError } = useKoodisto(
        'OPPILAITOSTYYPPI'
    );
    const {
        data: organisaatioTyypit,
        loading: organisaatioTyypitLoading,
        error: organisaatioTyypitError,
    } = useKoodisto('ORGANISAATIOTYYPPI');
    const { data: maatJaValtiot, loading: maatJaValtiotLoading, error: maatJaValtiotError } = useKoodisto(
        'MAATJAVALTIOT1'
    );

    const {
        data: oppilaitoksenOpetuskielet,
        loading: oppilaitoksenOpetuskieletLoading,
        error: oppilaitoksenOpetuskieletError,
    } = useKoodisto('OPPILAITOKSENOPETUSKIELI');
    const {
        data: vardatoimintamuoto,
        loading: vardatoimintamuotoLoading,
        error: vardatoimintamuotoError,
    } = useKoodisto('VARDATOIMINTAMUOTO');
    const {
        data: vardakasvatusopillinenjarjestelma,
        loading: vardakasvatusopillinenjarjestelmaLoading,
        error: vardakasvatusopillinenjarjestelmaError,
    } = useKoodisto('VARDAKASVATUSOPILLINENJARJESTELMA');
    const {
        data: vardatoiminnallinenpainotus,
        loading: vardatoiminnallinenpainotusLoading,
        error: vardatoiminnallinenpainotusError,
    } = useKoodisto('VARDATOIMINNALLINENPAINOTUS');
    const {
        data: vardajarjestamismuoto,
        loading: vardajarjestamismuotoLoading,
        error: vardajarjestamismuotoError,
    } = useKoodisto('VARDAJARJESTAMISMUOTO');
    const { data: kieli, loading: kieliLoading, error: kieliError } = useKoodisto('KIELI');
    const { data: postinumerot, loading: postinumerotLoading, error: postinumerotError } = useKoodisto('POSTI', true);
    if (
        oppilaitoksenOpetuskieletLoading ||
        maatJaValtiotLoading ||
        kunnatLoading ||
        ryhmaTyypitLoading ||
        kayttoRyhmatLoading ||
        organisaatioTyypitLoading ||
        ryhmanTilatLoading ||
        organisaatioTyypitLoading ||
        postinumerotLoading ||
        vuosiluokatLoading ||
        oppilaitostyyppiLoading ||
        vardatoimintamuotoLoading ||
        vardakasvatusopillinenjarjestelmaLoading ||
        vardatoiminnallinenpainotusLoading ||
        vardajarjestamismuotoLoading ||
        kieliLoading
    ) {
        return <Loading />;
    }
    if (
        oppilaitoksenOpetuskieletError ||
        kunnatError ||
        ryhmaTyypitError ||
        kayttoRyhmatError ||
        organisaatioTyypitError ||
        ryhmanTilatError ||
        maatJaValtiotError ||
        organisaatioTyypitError ||
        postinumerotError ||
        vuosiluokatError ||
        oppilaitostyyppiError ||
        oppilaitostyyppiError ||
        vardatoimintamuotoError ||
        vardakasvatusopillinenjarjestelmaError ||
        vardatoiminnallinenpainotusError ||
        vardajarjestamismuotoError ||
        kieliError
    ) {
        return <VirheSivu />;
    }

    return (
        <ThemeProvider theme={theme}>
            <Notification />
            <KoodistoContext.Provider
                value={{
                    postinumerotKoodisto: new KoodistoImpl(postinumerot, casData.lang),
                    oppilaitoksenOpetuskieletKoodisto: new KoodistoImpl(oppilaitoksenOpetuskielet, casData.lang),
                    maatJaValtiotKoodisto: new KoodistoImpl(maatJaValtiot, casData.lang),
                    kuntaKoodisto: new KoodistoImpl(kunnat, casData.lang),
                    ryhmaTyypitKoodisto: new KoodistoImpl(ryhmaTyypit, casData.lang),
                    kayttoRyhmatKoodisto: new KoodistoImpl(kayttoRyhmat, casData.lang),
                    organisaatioTyypitKoodisto: new KoodistoImpl(organisaatioTyypit, casData.lang),
                    ryhmanTilaKoodisto: new KoodistoImpl(ryhmanTilat, casData.lang),
                    vuosiluokatKoodisto: new KoodistoImpl(vuosiluokat, casData.lang),
                    oppilaitostyyppiKoodisto: new KoodistoImpl(oppilaitostyyppi, casData.lang),
                    vardatoimintamuotoKoodisto: new KoodistoImpl(vardatoimintamuoto, casData.lang),
                    vardakasvatusopillinenjarjestelmaKoodisto: new KoodistoImpl(
                        vardakasvatusopillinenjarjestelma,
                        casData.lang
                    ),
                    vardatoiminnallinenpainotusKoodisto: new KoodistoImpl(vardatoiminnallinenpainotus, casData.lang),
                    vardajarjestamismuotoKoodisto: new KoodistoImpl(vardajarjestamismuoto, casData.lang),
                    kielikoodisto: new KoodistoImpl(kieli, casData.lang),
                }}
            >
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
            </KoodistoContext.Provider>
        </ThemeProvider>
    );
};

export default OrganisaatioApp;
