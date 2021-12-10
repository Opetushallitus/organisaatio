import * as React from 'react';
import { useContext } from 'react';
import { ThemeProvider } from 'styled-components';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { registerLocale } from 'react-datepicker';
import { enGB, fi, sv } from 'date-fns/locale';
import {
    BASE_PATH,
    I18nImpl,
    KoodistoContext,
    KoodistoImpl,
    LanguageContext,
    SearchFilterContext,
    SearchFiltersImpl,
} from './contexts/contexts';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import ErrorPage from './components/Sivut/VirheSivu/VirheSivu';
import LomakeSivu from './components/Sivut/LomakeSivu/LomakeSivu';
import TaulukkoSivu from './components/Sivut/TaulukkoSivu/TaulukkoSivu';
import Ryhmat from './components/Sivut/Ryhmat/Ryhmat';
import Tyypit from './components/Sivut/Tyypit/Tyypit';
import LisatietotyypinMuokkaus from './components/Sivut/Tyypit/Muokkaus/LisatietotyypinMuokkaus';
import YhteystietotyypinMuokkaus from './components/Sivut/Tyypit/Muokkaus/YhteystietotyypinMuokkaus';
import RyhmanMuokkaus from './components/Sivut/Ryhmat/Muokkaus/RyhmanMuokkaus';
import UusiToimijaLomake from './components/Sivut/LomakeSivu/UusiToimija/UusiToimijaLomake';
import { useCAS } from './api/kayttooikeus';
import Loading from './components/Loading/Loading';
import useKoodisto from './api/koodisto';
import Notification from './components/Notification/Notification';
import useLokalisaatio from './api/lokalisaatio';

const theme = createTheme();
const Error = () => {
    const { i18n } = useContext(LanguageContext);
    return <ErrorPage>{i18n.translate('LABEL_ERROR_LOADING_DATA')}</ErrorPage>;
};
const OrganisaatioApp: React.FC = () => {
    registerLocale('fi', fi);
    registerLocale('sv', sv);
    registerLocale('en', enGB);

    const { data: casData, loading: casDataLoading, error: casDataError } = useCAS();
    console.log(casData?.roles);
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
        return <Error />;
    }
    const i18n = new I18nImpl(lokalisointi, casData.lang);
    const kuntaKoodisto = new KoodistoImpl(kunnat, casData.lang);
    const ryhmaTyypitKoodisto = new KoodistoImpl(ryhmaTyypit, casData.lang);
    const kayttoRyhmatKoodisto = new KoodistoImpl(kayttoRyhmat, casData.lang);
    const organisaatioTyypitKoodisto = new KoodistoImpl(organisaatioTyypit, casData.lang);
    const ryhmanTilaKoodisto = new KoodistoImpl(ryhmanTilat, casData.lang);
    const maatJaValtiotKoodisto = new KoodistoImpl(maatJaValtiot, casData.lang);
    const oppilaitoksenOpetuskieletKoodisto = new KoodistoImpl(oppilaitoksenOpetuskielet, casData.lang);
    const postinumerotKoodisto = new KoodistoImpl(postinumerot, casData.lang);
    const vuosiluokatKoodisto = new KoodistoImpl(vuosiluokat, casData.lang);
    const oppilaitostyyppiKoodisto = new KoodistoImpl(oppilaitostyyppi, casData.lang);

    return (
        <Router basename={BASE_PATH}>
            <ThemeProvider theme={theme}>
                <LanguageContext.Provider value={{ language: casData.lang, i18n }}>
                    <SearchFilterContext.Provider value={{ searchFilters: new SearchFiltersImpl() }}>
                        <Notification />
                        <KoodistoContext.Provider
                            value={{
                                postinumerotKoodisto,
                                oppilaitoksenOpetuskieletKoodisto,
                                maatJaValtiotKoodisto,
                                kuntaKoodisto,
                                ryhmaTyypitKoodisto,
                                kayttoRyhmatKoodisto,
                                organisaatioTyypitKoodisto,
                                ryhmanTilaKoodisto,
                                vuosiluokatKoodisto,
                                oppilaitostyyppiKoodisto,
                            }}
                        >
                            <Switch>
                                <Route path={'/organisaatiot'} exact component={TaulukkoSivu} />
                                <Route exact path={'/lomake/uusi'} component={UusiToimijaLomake} />
                                <Route path={'/lomake/:oid'} component={LomakeSivu} />
                                <Route path={'/ryhmat'} exact component={Ryhmat} />
                                <Route
                                    path={'/yhteystietotyypit'}
                                    exact
                                    component={() => <Tyypit tyyppi={'yhteystietojentyyppi'} />}
                                />
                                <Route path={'/lisatietotyypit/muokkaus/:nimi'} component={LisatietotyypinMuokkaus} />
                                <Route path={'/yhteystietotyypit/muokkaus'} component={YhteystietotyypinMuokkaus} />
                                <Route
                                    exact
                                    path={'/ryhmat/uusi'}
                                    component={(props) => <RyhmanMuokkaus {...props} isNew />}
                                />
                                <Route path={'/ryhmat/:oid'} component={RyhmanMuokkaus} />
                                <Route path={'*'}>
                                    <ErrorPage>{'ERROR_404'}</ErrorPage>
                                </Route>
                            </Switch>
                        </KoodistoContext.Provider>
                    </SearchFilterContext.Provider>
                </LanguageContext.Provider>
            </ThemeProvider>
        </Router>
    );
};

export default OrganisaatioApp;
