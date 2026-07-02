import React, { ReactNode, useCallback, useContext, useMemo, useState } from 'react';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { ThemeProvider } from 'styled-components';
import useAxios from 'axios-hooks';

import {
    PermissionContext,
    KoodistoImpl,
    LanguageContext,
    MaatJaValtiotKoodistoContext,
    ModalContext,
} from '../../contexts';
import { Koodi, Permission } from '../../types/types';
import Spinner from '../../Spinner';
import ErrorPage from '../../virhe/VirheSivu';
import { Raamit } from '../Raamit';
import RekisteroinnitBase from '../components/RekisteroinnitBase/RekisteroinnitBase';

const theme = createTheme();

export default function VirkailijaLandingPage() {
    const { language } = useContext(LanguageContext);
    const [modal, setModal] = useState<ReactNode>();
    const [{ data: maatJaValtiot, loading: maatJaValtiotLoading, error: maatJaValtiotError }] = useAxios<Koodi[]>(
        '/varda-rekisterointi/api/koodisto/MAAT_JA_VALTIOT_1/koodi?onlyValid=true'
    );
    const [{ data: permission, loading: permissionLoading, error: permissionError }] = useAxios<Permission>(
        '/varda-rekisterointi/virkailija/api/permission/rekisterointi'
    );
    const sortedPermission = useMemo(
        () =>
            permission
                ? {
                      ...permission,
                      registrationTypes: [...permission.registrationTypes].sort(),
                  }
                : undefined,
        [permission]
    );
    const maatJaValtiotKoodisto = useMemo(
        () => new KoodistoImpl(maatJaValtiot ?? [], language),
        [maatJaValtiot, language]
    );
    const maatJaValtiotContextValue = useMemo(() => ({ koodisto: maatJaValtiotKoodisto }), [maatJaValtiotKoodisto]);
    const closeModal = useCallback(() => setModal(undefined), []);
    const modalContextValue = useMemo(() => ({ modal, setModal, closeModal }), [modal, closeModal]);

    if (maatJaValtiotLoading || permissionLoading) {
        return <Spinner />;
    }

    if (maatJaValtiotError || permissionError || !maatJaValtiot || !sortedPermission) {
        return <ErrorPage>Tietojen lataaminen epäonnistui. Yritä myöhemmin uudelleen</ErrorPage>;
    }

    return (
        <PermissionContext.Provider value={sortedPermission}>
            <MaatJaValtiotKoodistoContext.Provider value={maatJaValtiotContextValue}>
                <Raamit>
                    <ModalContext.Provider value={modalContextValue}>
                        <ThemeProvider theme={theme}>
                            <RekisteroinnitBase />
                        </ThemeProvider>
                    </ModalContext.Provider>
                </Raamit>
            </MaatJaValtiotKoodistoContext.Provider>
        </PermissionContext.Provider>
    );
}
