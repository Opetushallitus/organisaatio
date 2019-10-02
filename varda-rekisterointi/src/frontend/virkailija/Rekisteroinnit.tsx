import React, {useContext} from "react";
import {LanguageContext} from "../contexts";
import RekisterointiLista from "./RekisterointiLista";
import Box from "@opetushallitus/virkailija-ui-components/Box";
import createTheme from "@opetushallitus/virkailija-ui-components/createTheme";
import {ThemeProvider} from "styled-components";

const theme = createTheme();

export default function Rekisteroinnit() {
    const { i18n } = useContext(LanguageContext);


    return (
        <ThemeProvider theme={theme}>
            <Box className="varda-rekisteroinnit">
                <h2>{i18n.translate('REKISTEROINNIT_OTSIKKO')}</h2>
                <p>{i18n.translate('REKISTEROINNIT_KUVAUS')}</p>
                <RekisterointiLista />
            </Box>
        </ThemeProvider>
    );
}
