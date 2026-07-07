import React, { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router';
import classNames from 'classnames';
import axios from 'axios';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { toast, ToastContainer } from 'react-toastify';

import { LanguageContext, PermissionContext, useModalContext } from '../../../contexts';
import { Rekisterointihakemus } from '../../rekisterointihakemus';
import * as YtunnusValidator from '../../../YtunnusValidator';
import RekisteroinnitTable from '../RekisteroinnitTable/RekisteroinnitTable';
import Spinner from '../../../Spinner';
import ErrorPage from '../../../virhe/VirheSivu';
import { Rekisterointityyppi } from '../../../types/types';
import { ButtonGroup } from '../../ButtonGroup';

import styles from './RekisteroinnitBase.module.css';
import 'react-toastify/dist/ReactToastify.css';

export default function RekisteroinnitBase() {
    const { i18n } = useContext(LanguageContext);
    const { hasCreatePermission, registrationTypes } = useContext(PermissionContext);
    const [ytunnus, setYtunnus] = useState('');
    const ytunnusTrimmed = ytunnus.trim();
    const ytunnusDisabled = !YtunnusValidator.validate(ytunnusTrimmed);
    const ytunnusClassNames = classNames(styles.nappi, { [`${styles.nappiDisabled}`]: ytunnusDisabled });
    const [registrationType, setRegistrationType] = useState<Rekisterointityyppi>(registrationTypes[0] ?? 'varda');
    const [rekisteroinnit, setRekisteroinnit] = useState<Rekisterointihakemus[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);
    const { modal } = useModalContext();

    useEffect(() => {
        const fetchRekisteroinnit = async () => {
            try {
                const { data: rekisteroinnit } = await axios.get<Rekisterointihakemus[]>(
                    '/varda-rekisterointi/virkailija/api/rekisterointi'
                );
                setRekisteroinnit(rekisteroinnit);
                setLoading(false);
            } catch (_e: unknown) {
                setError(true);
            }
        };

        void fetchRekisteroinnit();
    }, []);

    const approvalCallback = useCallback(
        (approvalRegistrations: Rekisterointihakemus[], hyvaksytty: boolean, perustelu?: string) => {
            const approvalIds = approvalRegistrations.map((r) => r.id);
            setRekisteroinnit((currentRekisteroinnit) =>
                currentRekisteroinnit.map((r) =>
                    approvalIds.includes(r.id)
                        ? {
                              ...r,
                              tila: hyvaksytty ? ('HYVAKSYTTY' as const) : ('HYLATTY' as const),
                              paatos: { hyvaksytty, paatetty: new Date().toISOString(), perustelu },
                          }
                        : r
                )
            );
            toast.success(i18n.translate(hyvaksytty ? 'REKISTEROINNIT_HYVAKSYTTY' : 'REKISTEROINNIT_HYLATTY'));
        },
        [i18n]
    );

    const filteredRekisteroinnit = useMemo(
        () => rekisteroinnit.filter((r) => r.tyyppi === registrationType),
        [rekisteroinnit, registrationType]
    );

    if (error) {
        return <ErrorPage>{i18n.translate('REKISTEROINNIT_LATAUSVIRHE')}</ErrorPage>;
    }

    return (
        <div className={styles.pageBase}>
            <ToastContainer
                theme="colored"
                hideProgressBar
                className={styles.toast}
                draggable={false}
                closeOnClick={false}
            />
            <div className={styles.mainContent}>
                {registrationTypes.length > 1 && (
                    <div className={styles.registrationTypeButtons}>
                        <ButtonGroup>
                            {registrationTypes.map((t, idx) => (
                                <Button
                                    key={`${t}_${idx}`}
                                    className={styles.registrationTypeButton}
                                    onClick={() => setRegistrationType(t)}
                                    variant={t === registrationType ? 'contained' : 'outlined'}
                                >
                                    <span className={styles.headerPrefix}>{t}</span>-
                                    {i18n.translate('REKISTEROINNIT_OTSIKKO_SUFFIX')}
                                </Button>
                            ))}
                        </ButtonGroup>
                    </div>
                )}
                <h1>
                    <span className={styles.headerPrefix}>{registrationType}</span>-
                    {i18n.translate('REKISTEROINNIT_OTSIKKO_SUFFIX')}
                </h1>
                <p className={styles.description}>{i18n.translate('REKISTEROINNIT_KUVAUS')}</p>
                {loading ? (
                    <Spinner />
                ) : (
                    <RekisteroinnitTable
                        rekisteroinnit={filteredRekisteroinnit}
                        rekisterointityyppi={registrationType ?? 'varda'}
                        approvalCallback={approvalCallback}
                    />
                )}
                {hasCreatePermission && registrationType === 'varda' && (
                    <div>
                        <div className={styles.lisaaHakemusOsio}>
                            <Input
                                type="text"
                                placeholder={i18n.translate('YTUNNUS')}
                                value={ytunnus}
                                onChange={(event: { currentTarget: HTMLInputElement }) =>
                                    setYtunnus(event.currentTarget.value)
                                }
                            />
                            <div>
                                <Link
                                    to={`/virkailija/rekisterointi/luonti/${ytunnusTrimmed}`}
                                    className={ytunnusClassNames}
                                    onClick={(event) => {
                                        if (ytunnusDisabled) {
                                            event.preventDefault();
                                        }
                                    }}
                                >
                                    {i18n.translate('REKISTEROINNIT_LUONTI')}
                                </Link>
                            </div>
                        </div>
                    </div>
                )}
            </div>
            {modal}
        </div>
    );
}
