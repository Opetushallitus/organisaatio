import React, { useContext, useEffect, useState } from 'react';
import { LanguageContext } from '../contexts';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import PaatosVahvistus from './PaatosVahvistus';
import { Rekisterointihakemus } from './rekisterointihakemus';
import Box from '@opetushallitus/virkailija-ui-components/Box';
import styles from './PaatosKontrollit.module.css';

type Props = {
    valitut: Rekisterointihakemus[];
    valitutKasiteltyCallback: (hyvaksytty: boolean) => void;
};

export default function PaatosKontrollit({ valitut, valitutKasiteltyCallback }: Props) {
    const { i18n } = useContext(LanguageContext);
    const [kaytossa, asetaKaytossa] = useState(false);
    const [hyvaksytty, asetaHyvaksytty] = useState(false);
    const [naytaVahvistus, asetaNaytaVahvistus] = useState(false);

    function vahvista(hyvaksytty: boolean) {
        asetaHyvaksytty(hyvaksytty);
        asetaNaytaVahvistus(true);
    }

    useEffect(() => {
        asetaKaytossa(valitut && valitut.length > 0);
    }, [valitut]);

    return (
        <Box className={styles.paatosKontrollit}>
            <Button
                id="hylkaaButton"
                className={styles.paatosKontrollit}
                disabled={!kaytossa}
                onClick={() => vahvista(false)}
                variant="outlined"
                color="secondary"
            >
                <i className="material-icons md-18">&#xe14c;</i> {i18n.translate('REKISTEROINNIT_HYLKAA_VALITUT')}
            </Button>
            <Button
                id="hyvaksyButton"
                className={styles.paatosKontrollit}
                disabled={!kaytossa}
                onClick={() => vahvista(true)}
            >
                <i className="material-icons md-18">&#xe5ca;</i> {i18n.translate('REKISTEROINNIT_HYVAKSY_VALITUT')}
            </Button>
            {naytaVahvistus && (
                <PaatosVahvistus
                    valitut={valitut}
                    hyvaksytty={hyvaksytty}
                    nayta={naytaVahvistus}
                    valitutKasiteltyCallback={valitutKasiteltyCallback}
                    suljeCallback={() => asetaNaytaVahvistus(false)}
                />
            )}
        </Box>
    );
}
