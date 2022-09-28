import React, { useEffect, useState } from 'react';
import { format, parseISO } from 'date-fns';
import InfoOutlinedIcon from '@material-ui/icons/InfoOutlined';
import { Rekisterointihakemus } from './rekisterointihakemus';
import styles from './RekisterointiListaRivi.module.css';

import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import { useKoodistoContext } from '../contexts';

const saapumisAikaFormat = 'd.M.y HH:mm';

export class ListaRivi {
    constructor(readonly hakemus: Rekisterointihakemus) {}

    get organisaatio(): string {
        return this.hakemus.organisaatio.ytjNimi.nimi;
    }

    get puhelinnumero(): string {
        return this.hakemus.organisaatio.yhteystiedot.puhelinnumero || '';
    }

    get ytunnus(): string {
        return this.hakemus.organisaatio.ytunnus;
    }

    get vastaanotettu(): string {
        return this.hakemus.vastaanotettu ? format(parseISO(this.hakemus.vastaanotettu), saapumisAikaFormat) : '';
    }
    get kunnat(): string[] {
        return this.hakemus.kunnat;
    }
}

type Props = {
    valintaKaytossa: boolean;
    rekisterointi: ListaRivi;
    riviValittu: boolean;
    valitseHakemusCallback: (hakemus: Rekisterointihakemus, valittu: boolean) => void;
    valitseInfoCallback: (hakemus: Rekisterointihakemus) => void;
};

export default function RekisterointiListaRivi({
    valintaKaytossa,
    rekisterointi,
    riviValittu,
    valitseHakemusCallback,
    valitseInfoCallback,
}: Props) {
    const [valittu, asetaValittu] = useState(false);

    const { kunnat: kuntaKoodisto } = useKoodistoContext();

    useEffect(() => {
        asetaValittu(riviValittu);
    }, [valintaKaytossa, riviValittu]);

    function valitse() {
        asetaValittu((vanhaTila) => !vanhaTila);
        valitseHakemusCallback(rekisterointi.hakemus, valittu);
    }

    function koodit2kunnat(kunnatArr: string[]): string {
        return (kunnatArr || []).map((k) => kuntaKoodisto.uri2Nimi(k) || k).join(', ');
    }

    return (
        <tr>
            {valintaKaytossa && (
                <td className={styles.kapea}>
                    <Checkbox checked={valittu} onChange={() => valitse()} />
                </td>
            )}
            <td className={styles.nimi}>{rekisterointi.organisaatio}</td>
            <td className={styles.kapea}>{rekisterointi.puhelinnumero}</td>
            <td className={styles.ytunnus}>{rekisterointi.ytunnus}</td>
            <td className={styles.kunnat}>{koodit2kunnat(rekisterointi.kunnat)}</td>
            <td className={styles.aikaleima}>{rekisterointi.vastaanotettu}</td>
            <td className={styles.rivinInfoNappi} onClick={(_) => valitseInfoCallback(rekisterointi.hakemus)}>
                <InfoOutlinedIcon style={{ color: '#0A789C' }} />
            </td>
        </tr>
    );
}
