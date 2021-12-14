import { MuokattuKolumni } from '../Sivut/LomakeSivu/LomakeFields/LomakeFields';
import moment from 'moment';
import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import { OrganisaatioPaivittaja } from '../../types/types';
import { readOrganisaatioPaivittaja } from '../../api/organisaatio';
import { LanguageContext } from '../../contexts/LanguageContext';

export default function Muokattu(params: { oid?: string }) {
    console.log(params.oid);
    const { i18n } = useContext(LanguageContext);
    const [organisaatioPaivittaja, setOrganisaatioPaivittaja] = useState<OrganisaatioPaivittaja>({});
    useEffect(() => {
        (async function () {
            const paivittaja = await readOrganisaatioPaivittaja(params.oid);
            if (paivittaja) setOrganisaatioPaivittaja(paivittaja);
        })();
    }, [params.oid]);
    return (
        <MuokattuKolumni>
            <span style={{ color: '#999999' }}>{i18n.translate('VERSIOHISTORIA_MUOKATTU_VIIMEKSI')}</span>
            <span>
                {organisaatioPaivittaja?.paivitysPvm
                    ? moment(new Date(organisaatioPaivittaja.paivitysPvm)).format('D.M.yyyy HH:mm:ss')
                    : ''}{' '}
                {organisaatioPaivittaja?.etuNimet} {organisaatioPaivittaja?.sukuNimi}
            </span>
        </MuokattuKolumni>
    );
}
