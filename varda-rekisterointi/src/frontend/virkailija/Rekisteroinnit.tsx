import React, {useContext, useState} from "react";
import {KoodistoImpl, LanguageContext, MaatJaValtiotKoodistoContext} from "../contexts";
import {ConfigurationContext} from "../contexts";
import {useDebounce} from "use-debounce";
import RekisterointiLista from "./RekisterointiLista";
import Box from "@opetushallitus/virkailija-ui-components/Box";
import createTheme from "@opetushallitus/virkailija-ui-components/createTheme";
import VirkailijaRaamit from "@opetushallitus/virkailija-ui-components/VirkailijaRaamit";
import {ThemeProvider} from "styled-components";
import {Tila} from "./rekisterointihakemus";
import Tabs from "@opetushallitus/virkailija-ui-components/Tabs";
import Tab from "@opetushallitus/virkailija-ui-components/Tab";
import Input from "@opetushallitus/virkailija-ui-components/Input";
import FilterVariantIcon from "mdi-react/FilterVariantIcon";
import styles from "./Rekisteroinnit.module.css";
import Divider from "@opetushallitus/virkailija-ui-components/Divider";
import * as YtunnusValidator from '../YtunnusValidator';
import { Link } from "react-router-dom";
import classNames from "classnames/bind";
import useAxios from "axios-hooks";
import {Koodi} from "../types";
import Spinner from "../Spinner";
import ErrorPage from "../ErrorPage";

const theme = createTheme();

export default function Rekisteroinnit() {
    const { i18n, language } = useContext(LanguageContext);
    const configuration = useContext(ConfigurationContext);
    const [{data: maatJaValtiot, loading: maatJaValtiotLoading, error: maatJaValtiotError}]
        = useAxios<Koodi[]>('/varda-rekisterointi/api/koodisto/MAAT_JA_VALTIOT_1/koodi?onlyValid=true');
    const [{data: permission, loading: permissionLoading, error: permissionError}]
        = useAxios<boolean>('/varda-rekisterointi/virkailija/api/permission/rekisterointi/create');
    const [hakutermiInput, asetaHakutermiInput] = useState("");
    const [hakutermi] = useDebounce(hakutermiInput, 500);
    const [tila, asetaTila] = useState(Tila.KASITTELYSSA);
    const [ytunnus, setYtunnus] = useState('');
    const ytunnusDisabled = !YtunnusValidator.validate(ytunnus);
    const ytunnusClassNames = classNames(styles.nappi, { [styles.nappiDisabled]:  ytunnusDisabled });

    if (maatJaValtiotLoading) {
        return <Spinner />;
    }

    if (maatJaValtiotError) {
        return <ErrorPage>Tietojen lataaminen epäonnistui. Yritä myöhemmin uudelleen</ErrorPage>
    }

    const maatJaValtiotKoodisto = new KoodistoImpl(maatJaValtiot, language);

    function vaihdaTila(uusiTila: string) {
        asetaTila(uusiTila as Tila);
    }

    function vaihdaHakutermi(uusiHakutermi: string) {
        asetaHakutermiInput(uusiHakutermi);
    }

    return (
        <ConfigurationContext.Provider value={configuration}>
            <MaatJaValtiotKoodistoContext.Provider value={{ koodisto: maatJaValtiotKoodisto }}>
                <VirkailijaRaamit scriptUrl={configuration.virkailijaRaamitUrl} />
                <ThemeProvider theme={theme}>
                    <Box className={styles.rekisteroinnit}>
                        <h2>{i18n.translate('REKISTEROINNIT_OTSIKKO')}</h2>
                        <p>{i18n.translate('REKISTEROINNIT_KUVAUS')}</p>
                        <div className={styles.vasen}>
                            <Input className={styles.suodata} type="text"
                                   placeholder={i18n.translate('REKISTEROINNIT_SUODATA')}
                                   value={hakutermiInput}
                                   prefix={<FilterVariantIcon className={styles.suodataIcon}/>}
                                   onChange={e => vaihdaHakutermi(e.target.value)} />
                        </div>
                        <div className={styles.oikea}>
                            <Tabs className={styles.tilaTabit} value={tila} onChange={vaihdaTila}>
                                <Tab value={Tila.KASITTELYSSA}>{ i18n.translate(`REKISTEROINNIT_TILA_KASITTELYSSA`) }</Tab>
                                <Tab value={Tila.HYVAKSYTTY}>{ i18n.translate(`REKISTEROINNIT_TILA_HYVAKSYTTY`) }</Tab>
                                <Tab value={Tila.HYLATTY}>{ i18n.translate(`REKISTEROINNIT_TILA_HYLATTY`) }</Tab>
                            </Tabs>
                        </div>
                        <RekisterointiLista tila={tila} hakutermi={hakutermi}/>
                        {!permissionLoading && !permissionError && permission ?
                        <div>
                        <Divider />
                        <Input type="text"
                               placeholder={i18n.translate('YTUNNUS')}
                               value={ytunnus}
                               onChange={event => setYtunnus(event.currentTarget.value)} />
                        <Link to={`/virkailija/rekisterointi/luonti/${ytunnus}`}
                            className={ytunnusClassNames}
                            onClick={event => {
                                if (!YtunnusValidator.validate(ytunnus)) {
                                    event.preventDefault();
                                }
                            }}>
                            {i18n.translate('REKISTEROINNIT_LUONTI')}
                        </Link>
                        </div>
                        : null}
                    </Box>
                </ThemeProvider>
            </MaatJaValtiotKoodistoContext.Provider>
        </ConfigurationContext.Provider>
    );
}
