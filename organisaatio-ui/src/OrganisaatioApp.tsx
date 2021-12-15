import * as React from 'react';
import { ThemeProvider } from 'styled-components';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { registerLocale } from 'react-datepicker';
import { enGB, fi, sv } from 'date-fns/locale';
import { BASE_PATH } from './contexts/constants';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import VirheSivu from './components/Sivut/VirheSivu/VirheSivu';
import LomakeSivu from './components/Sivut/LomakeSivu/LomakeSivu';
import TaulukkoSivu from './components/Sivut/TaulukkoSivu/TaulukkoSivu';
import Ryhmat from './components/Sivut/Ryhmat/Ryhmat';
import RyhmanMuokkaus from './components/Sivut/Ryhmat/Muokkaus/RyhmanMuokkaus';
import UusiToimijaLomake from './components/Sivut/LomakeSivu/UusiToimija/UusiToimijaLomake';
import { useCAS } from './api/kayttooikeus';
import Loading from './components/Loading/Loading';
import useKoodisto from './api/koodisto';
import Notification from './components/Notification/Notification';
import useLokalisaatio from './api/lokalisaatio';
import { I18nImpl, LanguageContext } from './contexts/LanguageContext';
import { SearchFilterContext, SearchFiltersImpl } from './contexts/SearchFiltersContext';
import { KoodistoContext, KoodistoImpl } from './contexts/KoodistoContext';
import { CasMeContext, CASMeImpl } from './contexts/CasMeContext';
import Raamit from './components/Raamit/Raamit';

const theme = createTheme();

const OrganisaatioApp: React.FC = () => {
    registerLocale('fi', fi);
    registerLocale('sv', sv);
    registerLocale('en', enGB);

    const { data: casData, loading: casDataLoading, error: casDataError } = useCAS();
    const { data: lokalisointi, loading: lokalisointiLoading, error: lokalisointiError } = useLokalisaatio();
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
    const { data: postinumerot, loading: postinumerotLoading, error: postinumerotError } = useKoodisto('POSTI', true);
    if (
        oppilaitoksenOpetuskieletLoading ||
        maatJaValtiotLoading ||
        casDataLoading ||
        lokalisointiLoading ||
        kunnatLoading ||
        ryhmaTyypitLoading ||
        kayttoRyhmatLoading ||
        organisaatioTyypitLoading ||
        ryhmanTilatLoading ||
        organisaatioTyypitLoading ||
        postinumerotLoading ||
        vuosiluokatLoading ||
        oppilaitostyyppiLoading
    ) {
        return <Loading />;
    }
    if (
        oppilaitoksenOpetuskieletError ||
        casDataError ||
        lokalisointiError ||
        kunnatError ||
        ryhmaTyypitError ||
        kayttoRyhmatError ||
        organisaatioTyypitError ||
        ryhmanTilatError ||
        maatJaValtiotError ||
        organisaatioTyypitError ||
        postinumerotError ||
        vuosiluokatError ||
        oppilaitostyyppiError
    ) {
        return <VirheSivu />;
    }

    return (
        <Router basename={BASE_PATH}>
            <ThemeProvider theme={theme}>
                <CasMeContext.Provider value={{ me: new CASMeImpl(casData) }}>
                    <LanguageContext.Provider
                        value={{ language: casData.lang, i18n: new I18nImpl(lokalisointi, casData.lang) }}
                    >
                        <SearchFilterContext.Provider value={{ searchFilters: new SearchFiltersImpl() }}>
                            <Notification />
                            <KoodistoContext.Provider
                                value={{
                                    postinumerotKoodisto: new KoodistoImpl(postinumerot, casData.lang),
                                    oppilaitoksenOpetuskieletKoodisto: new KoodistoImpl(
                                        oppilaitoksenOpetuskielet,
                                        casData.lang
                                    ),
                                    maatJaValtiotKoodisto: new KoodistoImpl(maatJaValtiot, casData.lang),
                                    kuntaKoodisto: new KoodistoImpl(kunnat, casData.lang),
                                    ryhmaTyypitKoodisto: new KoodistoImpl(ryhmaTyypit, casData.lang),
                                    kayttoRyhmatKoodisto: new KoodistoImpl(kayttoRyhmat, casData.lang),
                                    organisaatioTyypitKoodisto: new KoodistoImpl(organisaatioTyypit, casData.lang),
                                    ryhmanTilaKoodisto: new KoodistoImpl(ryhmanTilat, casData.lang),
                                    vuosiluokatKoodisto: new KoodistoImpl(vuosiluokat, casData.lang),
                                    oppilaitostyyppiKoodisto: new KoodistoImpl(oppilaitostyyppi, casData.lang),
                                }}
                            >
                                <Raamit />
                                <Switch>
                                    <Route path={'/organisaatiot'} exact component={TaulukkoSivu} />
                                    <Route exact path={'/lomake/uusi'} component={UusiToimijaLomake} />
                                    <Route path={'/lomake/:oid'} component={LomakeSivu} />
                                    <Route path={'/ryhmat'} exact component={Ryhmat} />
                                    <Route
                                        exact
                                        path={'/ryhmat/uusi'}
                                        component={(props) => <RyhmanMuokkaus {...props} isNew />}
                                    />
                                    <Route path={'/ryhmat/:oid'} component={RyhmanMuokkaus} />
                                    <Route path={'*'}>
                                        <VirheSivu>{'ERROR_404'}</VirheSivu>
                                    </Route>
                                </Switch>
                            </KoodistoContext.Provider>
                        </SearchFilterContext.Provider>
                    </LanguageContext.Provider>
                </CasMeContext.Provider>
            </ThemeProvider>
        </Router>
    );
};

export default OrganisaatioApp;
