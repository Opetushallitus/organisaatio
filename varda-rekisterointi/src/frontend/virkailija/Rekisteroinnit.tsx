import React, {useContext, useState} from "react";
import {ConfigurationContext, KoodistoImpl, LanguageContext, MaatJaValtiotKoodistoContext} from "../contexts";
import {useDebounce} from "use-debounce";
import RekisterointiLista from "./RekisterointiLista";
import createTheme from "@opetushallitus/virkailija-ui-components/createTheme";
import {ThemeProvider} from "styled-components";
import {Tila} from "./rekisterointihakemus";
import Tabs from "@opetushallitus/virkailija-ui-components/Tabs";
import Tab from "@opetushallitus/virkailija-ui-components/Tab";
import Input from "@opetushallitus/virkailija-ui-components/Input";
import FilterVariantIcon from "mdi-react/FilterVariantIcon";
import styles from "./Rekisteroinnit.module.css";
import Divider from "@opetushallitus/virkailija-ui-components/Divider";
import * as YtunnusValidator from '../YtunnusValidator';
import {Link} from "react-router-dom";
import classNames from "classnames/bind";
import useAxios from "axios-hooks";
import {Koodi} from "../types";
import Spinner from "../Spinner";
import ErrorPage from "../virhe/VirheSivu";
import Status, {StatusTila} from "./Status";
import {Raamit} from "./Raamit";

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
    const [statusTila, asetaStatusTila] = useState(StatusTila.PASSIIVINEN);
    const [statusTeksti, asetaStatusTeksti] = useState('');
    const [ytunnus, setYtunnus] = useState('');
    const ytunnusTrimmed = ytunnus.trim();
    const ytunnusDisabled = !YtunnusValidator.validate(ytunnusTrimmed);
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

    function statusCallback(hyvaksytty: boolean, lukumaara: number) {
        asetaStatusTeksti(`${lukumaara} ${i18n.translate(hyvaksytty ? 'REKISTEROINTIA_HYVAKSYTTY' : 'REKISTEROINTIA_HYLATTY')}`);
        asetaStatusTila(StatusTila.NAKYVA);
    }

    return (
        <ConfigurationContext.Provider value={configuration}>
            <MaatJaValtiotKoodistoContext.Provider value={{ koodisto: maatJaValtiotKoodisto }}>
                <Raamit>
                <ThemeProvider theme={theme}>
                    <div className={styles.rekisteroinnit}>
                        <div className={styles.rekisterointiOsio}>
                            <div>
                                <h2>{i18n.translate('REKISTEROINNIT_OTSIKKO')}</h2>
                                <p>{i18n.translate('REKISTEROINNIT_KUVAUS')}</p>
                            </div>
                            <Status tila={statusTila}
                                    teksti={statusTeksti}
                                    asetaTila={asetaStatusTila} />
                        </div>
                        <div className={styles.rekisterointiOsio}>
                            <div>
                                <Input className={styles.suodata} type="text"
                                       placeholder={i18n.translate('REKISTEROINNIT_SUODATA')}
                                       value={hakutermiInput}
                                       prefix={<FilterVariantIcon className={styles.suodataIcon}/>}
                                       onChange={(e: { target: HTMLInputElement; }) => vaihdaHakutermi(e.target.value)} />
                            </div>
                            <div>
                                <Tabs className={styles.tilaTabit} value={tila} onChange={vaihdaTila}>
                                    <Tab value={Tila.KASITTELYSSA}>{ i18n.translate(`REKISTEROINNIT_TILA_KASITTELYSSA`) }</Tab>
                                    <Tab value={Tila.HYVAKSYTTY}>{ i18n.translate(`REKISTEROINNIT_TILA_HYVAKSYTTY`) }</Tab>
                                    <Tab value={Tila.HYLATTY}>{ i18n.translate(`REKISTEROINNIT_TILA_HYLATTY`) }</Tab>
                                </Tabs>
                            </div>
                        </div>
                        <RekisterointiLista tila={tila} hakutermi={hakutermi} statusCallback={statusCallback} />
                        {!permissionLoading && !permissionError && permission ?
                        <div>
                        <Divider />
                        <div className={styles.lisaaHakemusOsio}>
                            <Input type="text"
                                   placeholder={i18n.translate('YTUNNUS')}
                                   value={ytunnus}
                                   onChange={(event: { currentTarget: HTMLInputElement; }) => setYtunnus(event.currentTarget.value)} />
                           <div>
                                <Link to={`/virkailija/rekisterointi/luonti/${ytunnusTrimmed}`}
                                    className={ytunnusClassNames}
                                    onClick={event => {
                                        if (ytunnusDisabled) {
                                            event.preventDefault();
                                        }
                                    }}>
                                    {i18n.translate('REKISTEROINNIT_LUONTI')}
                                </Link>
                           </div>
                        </div>
                        </div>
                        : null}
                    </div>
                </ThemeProvider>
                </Raamit>
            </MaatJaValtiotKoodistoContext.Provider>
        </ConfigurationContext.Provider>
    );
}
