import { MuokattuKolumni } from '../Sivut/LomakeSivu/LomakeFields/LomakeFields';
import * as React from 'react';
import { useEffect } from 'react';
import { useOrganisaatioPaivittaja } from '../../api/organisaatio';
import { languageAtom } from '../../contexts/LanguageContext';
import Loading from '../Loading/Loading';
import { getUiDateStr } from '../../tools/mappers';
import { useAtom } from 'jotai';

const Muokattu = ({ oid, muokattu = 0 }: { oid: string; muokattu?: number }) => {
    const [i18n] = useAtom(languageAtom);
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
                {data?.paivitysPvm ? getUiDateStr(new Date(data.paivitysPvm), undefined, true) : ''} {data?.etuNimet}{' '}
                {data?.sukuNimi}
            </span>
        </MuokattuKolumni>
    );
};
export default Muokattu;
