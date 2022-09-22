import React, { useContext } from 'react';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import { LanguageContext } from '../contexts';
import styles from './RekisterointiLista.module.css';

type Props = {
    valintaKaytossa: boolean;
    kaikkiValittu: boolean;
    kaikkiValittuCallback: (valitseKaikki: boolean) => void;
};

export default function RekisterointiListaOtsikko({ valintaKaytossa, kaikkiValittu, kaikkiValittuCallback }: Props) {
    const { i18n } = useContext(LanguageContext);

    return (
        <thead>
            <tr key="otsikko">
                {valintaKaytossa && (
                    <th id="valitseKaikki">
                        <Checkbox checked={kaikkiValittu} onChange={() => kaikkiValittuCallback(!kaikkiValittu)} />
                    </th>
                )}
                <th>{i18n.translate('ORGANISAATION_NIMI')}</th>
                <th>{i18n.translate('PUHELINNUMERO')}</th>
                <th>{i18n.translate('YTUNNUS')}</th>
                <th>{i18n.translate('KASITTELEVAT_KUNNAT')}</th>
                <th className={styles.aikaleima}>{i18n.translate('SAAPUMISAIKA')}</th>
                <th className={styles.rivinInfoNappi} />
            </tr>
        </thead>
    );
}
