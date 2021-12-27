import { MuokattuKolumni } from '../Sivut/LomakeSivu/LomakeFields/LomakeFields';
import * as React from 'react';
import { useContext, useEffect } from 'react';
import { useOrganisaatioPaivittaja } from '../../api/organisaatio';
import { LanguageContext } from '../../contexts/LanguageContext';
import Loading from '../Loading/Loading';
import VirheSivu from '../Sivut/VirheSivu/VirheSivu';
import { getUiDateStr } from '../../tools/mappers';

const Muokattu = ({ oid, muokattu = 0 }: { oid: string; muokattu?: number }) => {
    const { i18n } = useContext(LanguageContext);
    const { data, error, loading, execute } = useOrganisaatioPaivittaja(oid);
    useEffect(() => {
        execute();
    }, [execute, muokattu]);
    if (loading) return <Loading />;
    if (error) return <VirheSivu />;

    return (
        <MuokattuKolumni>
            <span style={{ color: '#999999' }}>{i18n.translate('VERSIOHISTORIA_MUOKATTU_VIIMEKSI')}</span>
            <span onClick={execute}>
                {data?.paivitysPvm ? getUiDateStr(new Date(data.paivitysPvm), undefined, true) : ''} {data?.etuNimet}{' '}
                {data?.sukuNimi}
            </span>
        </MuokattuKolumni>
    );
};
export default Muokattu;
