import { MuokattuKolumni } from '../Sivut/LomakeSivu/LomakeFields/LomakeFields';
import moment from 'moment';
import * as React from 'react';
import { useContext, useEffect } from 'react';
import { useOrganisaatioPaivittaja } from '../../api/organisaatio';
import { LanguageContext } from '../../contexts/LanguageContext';
import Loading from '../Loading/Loading';

const Muokattu = ({ oid, muokattu = 0 }: { oid: string; muokattu?: number }) => {
    const { i18n } = useContext(LanguageContext);
    const { data, error, loading, execute } = useOrganisaatioPaivittaja(oid);
    useEffect(() => {
        execute();
    }, [execute, muokattu]);
    if (loading) return <Loading />;
    if (error) return <div>{i18n.translate('PAIVITTAJA_KAYTTOOIKEUS_PUUTTUU')}</div>;

    return (
        <MuokattuKolumni>
            <span style={{ color: '#999999' }}>{i18n.translate('VERSIOHISTORIA_MUOKATTU_VIIMEKSI')}</span>
            <span onClick={execute}>
                {data?.paivitysPvm ? moment(data.paivitysPvm).format('D.M.yyyy HH:mm:ss') : ''} {data?.etuNimet}{' '}
                {data?.sukuNimi}
            </span>
        </MuokattuKolumni>
    );
};
export default Muokattu;
