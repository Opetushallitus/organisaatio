import React, { useContext, useState } from 'react';
import { LanguageContext, PermissionContext } from '../../../contexts';
import { Rekisterointihakemus, Tila } from '../../rekisterointihakemus';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import styles from './RekisteroinnitBase.module.css';
import Divider from '@opetushallitus/virkailija-ui-components/Divider';
import * as YtunnusValidator from '../../../YtunnusValidator';
import { Link } from 'react-router-dom';
import classNames from 'classnames/bind';
import RekisteroinnitTable from '../RekisteroinnitTable/RekisteroinnitTable';
import useAxios from 'axios-hooks';
import Spinner from '../../../Spinner';
import ErrorPage from '../../../virhe/VirheSivu';

const rekisteroinnitUrl = '/varda-rekisterointi/virkailija/api/rekisteroinnit';

export default function RekisteroinnitBase() {
    const { i18n } = useContext(LanguageContext);
    const { hasCreatePermission } = useContext(PermissionContext);
    const [ytunnus, setYtunnus] = useState('');
    const ytunnusTrimmed = ytunnus.trim();
    const ytunnusDisabled = !YtunnusValidator.validate(ytunnusTrimmed);
    const ytunnusClassNames = classNames(styles.nappi, { [styles.nappiDisabled]: ytunnusDisabled });

    const [{ data: rekisteroinnitData, loading, error }] = useAxios<Rekisterointihakemus[]>({
        url: rekisteroinnitUrl,
        params: { tila: Tila.KASITTELYSSA, hakutermi: '' },
    });

    if (loading || !rekisteroinnitData) {
        return <Spinner />;
    }

    if (error) {
        return <ErrorPage>{i18n.translate('REKISTEROINNIT_LATAUSVIRHE')}</ErrorPage>;
    }

    return (
        <div className={styles.pageBase}>
            <div className={styles.mainContent}>
                {/*<div className={styles.rekisterointiOsio}>
                    <div>
                        <h2>{i18n.translate('REKISTEROINNIT_OTSIKKO')}</h2>
                        <p>{i18n.translate('REKISTEROINNIT_KUVAUS')}</p>
                    </div>
                    <Status tila={statusTila} teksti={statusTeksti} asetaTila={asetaStatusTila} />
                </div>
                <div className={styles.rekisterointiOsio}>
                    <div>
                        <Input
                            className={styles.suodata}
                            type="text"
                            placeholder={i18n.translate('REKISTEROINNIT_SUODATA')}
                            value={hakutermiInput}
                            prefix={<FilterVariantIcon className={styles.suodataIcon} />}
                            onChange={(e: { target: HTMLInputElement }) => vaihdaHakutermi(e.target.value)}
                        />
                    </div>
                    <div>
                        <Tabs className={styles.tilaTabit} value={tila} onChange={vaihdaTila}>
                            <Tab value={Tila.KASITTELYSSA}>{i18n.translate(`REKISTEROINNIT_TILA_KASITTELYSSA`)}</Tab>
                            <Tab value={Tila.HYVAKSYTTY}>{i18n.translate(`REKISTEROINNIT_TILA_HYVAKSYTTY`)}</Tab>
                            <Tab value={Tila.HYLATTY}>{i18n.translate(`REKISTEROINNIT_TILA_HYLATTY`)}</Tab>
                        </Tabs>
                    </div>
                </div>
                <RekisterointiLista tila={tila} hakutermi={hakutermi} statusCallback={statusCallback} />*/}
                <RekisteroinnitTable rekisteroinnit={rekisteroinnitData} />
                {hasCreatePermission && (
                    <div>
                        <Divider />
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
        </div>
    );
}
