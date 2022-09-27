import React, { useContext, useEffect, useState } from 'react';
import Axios from 'axios';

import Box from '@opetushallitus/virkailija-ui-components/Box';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

import { LanguageContext } from '../contexts';
import { Rekisterointihakemus, Tila } from './rekisterointihakemus';
import { Rekisterointi } from '../types/types';
import RekisterointiListaOtsikko from './RekisterointiListaOtsikko';
import YksittainenPaatos from './YksittainenPaatos/YksittainenPaatos';
import RekisterointiListaRivi, { ListaRivi } from './RekisterointiListaRivi';
import styles from './RekisterointiLista.module.css';

const rekisteroinnitUrl = '/varda-rekisterointi/virkailija/api/rekisteroinnit';
const tyhjaHakemusLista: Rekisterointihakemus[] = [];

type Props = {
    tila?: Tila;
    hakutermi?: string;
    statusCallback: (hyvaksytty: boolean, lukumaara: number) => void;
};

export default function RekisterointiLista({ tila = 'KASITTELYSSA', hakutermi, statusCallback }: Props) {
    const { i18n } = useContext(LanguageContext);
    const [rekisteroinnit, asetaRekisteroinnit] = useState(tyhjaHakemusLista);
    const [yksiRekisterointi, asetaYksiRekisterointi] = useState<Rekisterointi | null>(null);
    const [latausKesken, asetaLatausKesken] = useState(true);
    const [latausVirhe, asetaLatausVirhe] = useState(false);
    const [kaikkiValittu, asetaKaikkiValittu] = useState(false);
    const [valitutHakemukset, asetaValitutHakemukset] = useState(tyhjaHakemusLista);
    const [naytaYksittainenInfo, asetaNaytaYksittainenInfo] = useState(false);

    const VALINTA_KAYTOSSA = tila === 'KASITTELYSSA';

    useEffect(() => {
        async function lataa() {
            try {
                asetaLatausKesken(true);
                asetaLatausVirhe(false);
                const params = { tila, hakutermi };
                const response = await Axios.get(rekisteroinnitUrl, { params });
                asetaRekisteroinnit(response.data);
            } catch (e) {
                asetaLatausVirhe(true);
                console.error(e); // TODO: vain dev modessa?
            } finally {
                asetaLatausKesken(false);
            }
        }
        lataa();
    }, [tila, hakutermi]);

    function vaihdaKaikkiValittu(valitseKaikki: boolean) {
        asetaKaikkiValittu(valitseKaikki);
        asetaValitutHakemukset(valitseKaikki ? [...rekisteroinnit] : tyhjaHakemusLista);
    }

    function vaihdaHakemusValittu(hakemus: Rekisterointihakemus, valittu: boolean) {
        asetaKaikkiValittu(false);
        asetaValitutHakemukset(
            valittu ? valitutHakemukset.filter((h) => h.id !== hakemus.id) : valitutHakemukset.concat(hakemus)
        );
    }

    function valitutKasiteltyCallback(hyvaksytty: boolean) {
        vaihdaKaikkiValittu(false);
        asetaRekisteroinnit((vanhat) =>
            vanhat.filter((rekisterointi) => !valitutHakemukset.some((valittu) => rekisterointi.id === valittu.id))
        );
        const lukumaara = valitutHakemukset.length;
        asetaValitutHakemukset(tyhjaHakemusLista);
        statusCallback(hyvaksytty, lukumaara);
    }

    function infoValittuCallback(rekisterointi: Rekisterointi) {
        asetaYksiRekisterointi(rekisterointi);
        asetaNaytaYksittainenInfo(true);
    }

    function suljeInfoCallback() {
        asetaNaytaYksittainenInfo(false);
        asetaYksiRekisterointi(null);
    }

    if (latausKesken) {
        return <Spin />;
    }

    if (latausVirhe) {
        return <div className="virhe">{i18n.translate('REKISTEROINNIT_LATAUSVIRHE')}</div>;
    }

    return (
        <Box>
            <table className={styles.vardaRekisterointiLista}>
                <RekisterointiListaOtsikko
                    valintaKaytossa={tila === 'KASITTELYSSA'}
                    kaikkiValittu={kaikkiValittu}
                    kaikkiValittuCallback={vaihdaKaikkiValittu}
                />
                <tbody>
                    {rekisteroinnit.map((rekisterointi) => (
                        <RekisterointiListaRivi
                            key={rekisterointi.id}
                            rekisterointi={new ListaRivi(rekisterointi)}
                            valintaKaytossa={VALINTA_KAYTOSSA}
                            riviValittu={valitutHakemukset.some((h) => h.id === rekisterointi.id)}
                            valitseHakemusCallback={vaihdaHakemusValittu}
                            valitseInfoCallback={infoValittuCallback}
                        />
                    ))}
                </tbody>
            </table>
            {VALINTA_KAYTOSSA && <div></div>}
            {naytaYksittainenInfo && yksiRekisterointi && (
                <YksittainenPaatos valittu={yksiRekisterointi} suljeCallback={suljeInfoCallback} />
            )}
        </Box>
    );
}
