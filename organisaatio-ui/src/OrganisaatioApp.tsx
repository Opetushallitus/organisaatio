import * as React from 'react';
import { useState, useEffect } from 'react';
import { ThemeProvider } from 'styled-components';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
//import 'normalize.css';
// import 'oph-virkailija-style-guide/oph-styles.css'
import { registerLocale } from 'react-datepicker';
import { fi, sv, enGB } from 'date-fns/locale';
import { LanguageContext, I18nImpl, KoodistoImpl, KoodistoContext } from './contexts/contexts';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import { Koodi, Language, Lokalisointi } from './types/types';
import useAxios from 'axios-hooks';
import ErrorPage from './components/Sivut/VirheSivu/VirheSivu';
import axios from 'axios';
import LomakeSivu from './components/Sivut/LomakeSivu/LomakeSivu';
import TaulukkoSivu from './components/Sivut/TaulukkoSivu/TaulukkoSivu';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import Ryhmat from './components/Sivut/Ryhmat/Ryhmat';
import Tyypit from './components/Sivut/Tyypit/Tyypit';
import LisatietotyypinMuokkaus from './components/Sivut/Tyypit/Muokkaus/LisatietotyypinMuokkaus';
import YhteystietotyypinMuokkaus from './components/Sivut/Tyypit/Muokkaus/YhteystietotyypinMuokkaus';
import RyhmanMuokkaus from './components/Sivut/Ryhmat/Muokkaus/RyhmanMuokkaus';
import UusiToimijaLomake from './components/Sivut/LomakeSivu/UusiToimija/UusiToimijaLomake';

const theme = createTheme();

const OrganisaatioApp: React.FC = () => {
    registerLocale('fi', fi);
    registerLocale('sv', sv);
    registerLocale('en', enGB);
    const [language, setLanguage] = useState<Language>('fi');
    const [languageLoading, setLanguageLoading] = useState(true);
    useEffect(() => {
        async function fetchLanguage() {
            try {
                const response = await axios.get(`/organisaatio/lokalisointi/kieli`);
                setLanguage(response.data);
            } catch (error) {
                console.log(error);
            } finally {
                setLanguageLoading(false);
            }
        }
        fetchLanguage();
    }, []);
    const [{ data: lokalisointi, loading: lokalisointiLoading, error: lokalisointiError }] = useAxios<Lokalisointi>(
        `/organisaatio/lokalisointi`
    );
    const [{ data: kunnat, loading: kunnatLoading, error: kunnatError }] = useAxios<Koodi[]>(
        `/organisaatio/koodisto/KUNTA/koodi`
    );
    const [{ data: ryhmaTyypit, loading: ryhmaTyypitLoading, error: ryhmaTyypitError }] = useAxios<Koodi[]>(
        `/organisaatio/koodisto/RYHMATYYPIT/koodi`
    );
    const [{ data: kayttoRyhmat, loading: kayttoRyhmatLoading, error: kayttoRyhmatError }] = useAxios<Koodi[]>(
        `/organisaatio/koodisto/KAYTTORYHMAT/koodi`
    );
    const [{ data: organisaatioTyypit, loading: organisaatioTyypitLoading, error: organisaatioTyypitError }] = useAxios<
        Koodi[]
    >(`/organisaatio/koodisto/ORGANISAATIOTYYPPI/koodi`);
    const [{ data: ryhmanTilat, loading: ryhmanTilatLoading, error: ryhmanTilatError }] = useAxios<Koodi[]>(
        `/organisaatio/koodisto/RYHMANTILA/koodi`
    );
    if (
        languageLoading ||
        lokalisointiLoading ||
        kunnatLoading ||
        ryhmaTyypitLoading ||
        kayttoRyhmatLoading ||
        organisaatioTyypitLoading ||
        ryhmanTilatLoading
    ) {
        return (
            <ThemeProvider theme={theme}>
                <Spin />
            </ThemeProvider>
        );
    }
    if (
        lokalisointiError ||
        kunnatError ||
        ryhmaTyypitError ||
        kayttoRyhmatError ||
        organisaatioTyypitError ||
        ryhmanTilatError
    ) {
        return <ErrorPage>Tietojen lataaminen epäonnistui. Yritä myöhemmin uudelleen</ErrorPage>;
    }
    const i18n = new I18nImpl(lokalisointi, language);
    const kuntaKoodisto = new KoodistoImpl(kunnat, language);
    const ryhmaTyypitKoodisto = new KoodistoImpl(ryhmaTyypit, language);
    const kayttoRyhmatKoodisto = new KoodistoImpl(kayttoRyhmat, language);
    const organisaatioTyypitKoodisto = new KoodistoImpl(organisaatioTyypit, language);
    const ryhmanTilaKoodisto = new KoodistoImpl(ryhmanTilat, language);

    return (
        <Router basename="/organisaatio">
            <ThemeProvider theme={theme}>
                <LanguageContext.Provider value={{ language: language, setLanguage: setLanguage, i18n: i18n }}>
                    <KoodistoContext.Provider
                        value={{
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
                            <Route path="/ryhmat/muokkaus/:oid" component={RyhmanMuokkaus} />
                            <Route path="*">
                                <ErrorPage>{i18n.translate('ERROR_404')}</ErrorPage>
                            </Route>
                        </Switch>
                    </KoodistoContext.Provider>
                </LanguageContext.Provider>
            </ThemeProvider>
        </Router>
    );
};

export default OrganisaatioApp;
