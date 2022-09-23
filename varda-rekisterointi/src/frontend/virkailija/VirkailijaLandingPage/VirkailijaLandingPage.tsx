import React, { useContext } from 'react';
import { PermissionContext, KoodistoImpl, LanguageContext, MaatJaValtiotKoodistoContext } from '../../contexts';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { ThemeProvider } from 'styled-components';
import useAxios from 'axios-hooks';
import { Koodi, Permission } from '../../types/types';
import Spinner from '../../Spinner';
import ErrorPage from '../../virhe/VirheSivu';
import { Raamit } from '../Raamit';
import RekisteroinnitBase from '../components/RekisteroinnitBase/RekisteroinnitBase';

const theme = createTheme();

export default function VirkailijaLandingPage() {
    const { language } = useContext(LanguageContext);
    const [{ data: maatJaValtiot, loading: maatJaValtiotLoading, error: maatJaValtiotError }] = useAxios<Koodi[]>(
        '/varda-rekisterointi/api/koodisto/MAAT_JA_VALTIOT_1/koodi?onlyValid=true'
    );
    const [{ data: permission, loading: permissionLoading, error: permissionError }] = useAxios<Permission>(
        '/varda-rekisterointi/virkailija/api/permission/rekisterointi'
    );
    if (maatJaValtiotLoading || permissionLoading) {
        return <Spinner />;
    }

    if (maatJaValtiotError || permissionError) {
        return <ErrorPage>Tietojen lataaminen epäonnistui. Yritä myöhemmin uudelleen</ErrorPage>;
    }

    const maatJaValtiotKoodisto = new KoodistoImpl(maatJaValtiot, language);
    return (
        <PermissionContext.Provider value={permission}>
            <MaatJaValtiotKoodistoContext.Provider value={{ koodisto: maatJaValtiotKoodisto }}>
                <Raamit>
                    <ThemeProvider theme={theme}>
                        <RekisteroinnitBase />
                    </ThemeProvider>
                </Raamit>
            </MaatJaValtiotKoodistoContext.Provider>
        </PermissionContext.Provider>
    );
}
