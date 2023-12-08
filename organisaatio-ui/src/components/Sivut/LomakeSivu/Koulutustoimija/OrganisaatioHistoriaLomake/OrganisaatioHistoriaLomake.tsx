import * as React from 'react';
import styles from './OrganisaaatioHistoriaLomake.module.css';
import YksinkertainenTaulukko from '../../../../Taulukot/YksinkertainenTaulukko';
import {
    HistoriaTaulukkoData,
    OrganisaatioHistoria,
    OrganisaatioSuhde,
    UiOrganisaationNimetNimi,
} from '../../../../../types/types';
import { Column } from 'react-table';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../../../api/lokalisaatio';
import { OrganisaatioLink } from '../../../../OrganisaatioComponents';
import { I18nImpl } from '../../../../../contexts/LanguageContext';

const liittyneetColumns = [
    ['RAKENNE_LIITOSPVM', 'alkuPvm'],
    ['RAKENNE_LIITTYNEET_NIMI', 'nimiHref'],
];
const yhdistettyColumns = [
    ['RAKENNE_LIITOSPVM', 'alkuPvm'],
    ['RAKENNE_YHDISTETTY_NIMI', 'nimiHref'],
];
const ylemmanTasonColumns = [
    ['RAKENNE_ALKUPVM', 'alkuPvm'],
    ['RAKENNE_LOPPUPVM', 'loppuPvm'],
    ['RAKENNE_YLEMMAN_TASON_NIMI', 'nimiHref'],
];
const sisaltyvatColumns = [
    ['RAKENNE_ALKUPVM', 'alkuPvm'],
    ['RAKENNE_LOPPUPVM', 'loppuPvm'],
    ['RAKENNE_SISALTYVAT_NIMI', 'nimiHref'],
];

const historiaSorter = (a: OrganisaatioSuhde, b: OrganisaatioSuhde) => {
    return a.alkuPvm.localeCompare(b.alkuPvm);
};

const historiaMapper = (a: OrganisaatioSuhde, key: 'child' | 'parent') => {
    return {
        oid: a[key].oid,
        nimiHref: <OrganisaatioLink oid={a[key].oid} nimi={a[key].nimi} status={a[key].status} />,
        alkuPvm: a.alkuPvm,
        loppuPvm: a.loppuPvm,
        status: a[key].status,
    };
};

export const mapColumnsToTableFormat = (
    i18n: I18nImpl,
    orginalColumns: string[][] = []
): Column<HistoriaTaulukkoData | UiOrganisaationNimetNimi>[] => {
    const columnMapper = (column: string[]) => ({
        Header: i18n.translate(column[0]),
        accessor: column[1] as 'alkuPvm' | 'loppuPvm' | 'nimiHref',
    });
    return orginalColumns.map(columnMapper) as Column<HistoriaTaulukkoData | UiOrganisaationNimetNimi>[];
};

export default function OrganisaatioHistoriaLomake({ historia }: { historia: OrganisaatioHistoria }) {
    const [i18n] = useAtom(languageAtom);

    const liittyneetData = historia.liitokset.sort(historiaSorter).map((a) => historiaMapper(a, 'parent'));

    const yhdistettyData = historia.liittymiset.sort(historiaSorter).map((a) => historiaMapper(a, 'child'));

    const ylemmanTasonData = historia.parentSuhteet.sort(historiaSorter).map((a) => historiaMapper(a, 'parent'));

    const sisaltyvatData = historia.childSuhteet.sort(historiaSorter).map((a) => historiaMapper(a, 'child'));

    return (
        <div className={styles.UloinKehys}>
            {(liittyneetData.length > 0 || yhdistettyData.length > 0) && (
                <div className={styles.Rivi}>
                    <div>
                        <h2>{i18n.translate('RAKENNE_LIITOKSET_OTSIKKO')}</h2>
                        {liittyneetData.length > 0 && (
                            <YksinkertainenTaulukko
                                data={liittyneetData}
                                tableColumns={mapColumnsToTableFormat(i18n, liittyneetColumns)}
                            />
                        )}
                        {yhdistettyData.length > 0 && (
                            <YksinkertainenTaulukko
                                data={yhdistettyData}
                                tableColumns={mapColumnsToTableFormat(i18n, yhdistettyColumns)}
                            />
                        )}
                    </div>
                </div>
            )}
            {ylemmanTasonData.length > 0 && (
                <div className={styles.Rivi}>
                    <div>
                        <h2>{i18n.translate('RAKENNE_YLEMMAN_TASON_OTSIKKO')}</h2>
                        <YksinkertainenTaulukko
                            data={ylemmanTasonData}
                            tableColumns={mapColumnsToTableFormat(i18n, ylemmanTasonColumns)}
                        />
                    </div>
                </div>
            )}
            {sisaltyvatData.length > 0 && (
                <div className={styles.Rivi}>
                    <div>
                        <h2>{i18n.translate('RAKENNE_SISALTYVAT_OTSIKKO')}</h2>
                        <YksinkertainenTaulukko
                            data={sisaltyvatData}
                            tableColumns={mapColumnsToTableFormat(i18n, sisaltyvatColumns)}
                        />
                    </div>
                </div>
            )}
        </div>
    );
}
