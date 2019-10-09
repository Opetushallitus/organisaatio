import React, {useContext, useState} from "react";
import {LanguageContext} from "../contexts";
import {ConfigurationContext} from "../contexts";
import RekisterointiLista from "./RekisterointiLista";
import Box from "@opetushallitus/virkailija-ui-components/Box";
import createTheme from "@opetushallitus/virkailija-ui-components/createTheme";
import VirkailijaRaamit from "@opetushallitus/virkailija-ui-components/VirkailijaRaamit";
import {ThemeProvider} from "styled-components";
import {Tila} from "./rekisterointihakemus";
import Tabs from "@opetushallitus/virkailija-ui-components/Tabs";
import Tab from "@opetushallitus/virkailija-ui-components/Tab";

const theme = createTheme();

export default function Rekisteroinnit() {
    const { i18n } = useContext(LanguageContext);
    const configuration = useContext(ConfigurationContext);
    //const [hakutermi, asetaHakutermi] = useState("");
    const [tila, asetaTila] = useState(Tila.KASITTELYSSA);

    function vaihdaTila(uusiTila: string) {
        asetaTila(uusiTila as Tila);
    }

    return (
        <ConfigurationContext.Provider value={configuration}>
            <VirkailijaRaamit scriptUrl={configuration.virkailijaRaamitUrl} />
            <ThemeProvider theme={theme}>
                <Box className="varda-rekisteroinnit">
                    <h2>{i18n.translate('REKISTEROINNIT_OTSIKKO')}</h2>
                    <p>{i18n.translate('REKISTEROINNIT_KUVAUS')}</p>
                    <Tabs value={tila} onChange={vaihdaTila}>
                        <Tab value={Tila.KASITTELYSSA}>{ i18n.translate(`REKISTEROINNIT_TILA_KASITTELYSSA`) }</Tab>
                        <Tab value={Tila.HYVAKSYTTY}>{ i18n.translate(`REKISTEROINNIT_TILA_HYVAKSYTTY`) }</Tab>
                        <Tab value={Tila.HYLATTY}>{ i18n.translate(`REKISTEROINNIT_TILA_HYLATTY`) }</Tab>
                    </Tabs>
                    <RekisterointiLista tila={tila}/>
                </Box>
            </ThemeProvider>
        </ConfigurationContext.Provider>
    );
}
