import * as React from 'react';
import { useContext } from 'react';
import { ThemeProvider } from 'styled-components';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { registerLocale } from 'react-datepicker';
import { enGB, fi, sv } from 'date-fns/locale';
import { BASE_PATH, I18nImpl, KoodistoContext, KoodistoImpl, LanguageContext } from './contexts/contexts';
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
import { useCASLanguage } from './api/kayttooikeus';
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

    const { data: language, loading: languageLoading, error: languageError } = useCASLanguage();
    const { data: lokalisointi, loading: lokalisointiLoading, error: lokalisointiError } = useLokalisaatio();
    const { data: kunnat, loading: kunnatLoading, error: kunnatError } = useKoodisto('KUNTA');
    const { data: ryhmaTyypit, loading: ryhmaTyypitLoading, error: ryhmaTyypitError } = useKoodisto('RYHMATYYPIT');

    const { data: kayttoRyhmat, loading: kayttoRyhmatLoading, error: kayttoRyhmatError } = useKoodisto('KAYTTORYHMAT');
    const { data: ryhmanTilat, loading: ryhmanTilatLoading, error: ryhmanTilatError } = useKoodisto('RYHMANTILA');
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
        languageLoading ||
        lokalisointiLoading ||
        kunnatLoading ||
        ryhmaTyypitLoading ||
        kayttoRyhmatLoading ||
        organisaatioTyypitLoading ||
        ryhmanTilatLoading ||
        organisaatioTyypitLoading ||
        postinumerotLoading
    ) {
        return <Loading />;
    }
    if (
        oppilaitoksenOpetuskieletError ||
        languageError ||
        lokalisointiError ||
        kunnatError ||
        ryhmaTyypitError ||
        kayttoRyhmatError ||
        organisaatioTyypitError ||
        ryhmanTilatError ||
        maatJaValtiotError ||
        organisaatioTyypitError ||
        postinumerotError
    ) {
        return <Error />;
    }
    const i18n = new I18nImpl(lokalisointi, language);
    const kuntaKoodisto = new KoodistoImpl(kunnat, language);
    const ryhmaTyypitKoodisto = new KoodistoImpl(ryhmaTyypit, language);
    const kayttoRyhmatKoodisto = new KoodistoImpl(kayttoRyhmat, language);
    const organisaatioTyypitKoodisto = new KoodistoImpl(organisaatioTyypit, language);
    const ryhmanTilaKoodisto = new KoodistoImpl(ryhmanTilat, language);
    const maatJaValtiotKoodisto = new KoodistoImpl(maatJaValtiot, language);
    const oppilaitoksenOpetuskieletKoodisto = new KoodistoImpl(oppilaitoksenOpetuskielet, language);
    const postinumerotKoodisto = new KoodistoImpl(postinumerot, language);
    return (
        <Router basename={BASE_PATH}>
            <ThemeProvider theme={theme}>
                <LanguageContext.Provider value={{ language, i18n }}>
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
                        }}
                    >
                        <Switch>
                            <Route path="/" exact component={TaulukkoSivu} />
                            <Route exact path="/lomake/uusi" component={UusiToimijaLomake} />
                            <Route path="/lomake/:oid" component={LomakeSivu} />
                            <Route path="/ryhmat" exact component={Ryhmat} />
                            <Route
                                path="/yhteystietotyypit"
                                exact
                                component={() => <Tyypit tyyppi="yhteystietojentyyppi" />}
                            />
                            <Route path="/lisatietotyypit/muokkaus/:nimi" component={LisatietotyypinMuokkaus} />
                            <Route path="/yhteystietotyypit/muokkaus" component={YhteystietotyypinMuokkaus} />
                            <Route
                                exact
                                path="/ryhmat/uusi"
                                component={(props) => <RyhmanMuokkaus {...props} isNew />}
                            />
                            <Route path="/ryhmat/:oid" component={RyhmanMuokkaus} />
                            <Route path="*">
                                <ErrorPage>{'ERROR_404'}</ErrorPage>
                            </Route>
                        </Switch>
                    </KoodistoContext.Provider>
                </LanguageContext.Provider>
            </ThemeProvider>
        </Router>
    );
};

export default OrganisaatioApp;
