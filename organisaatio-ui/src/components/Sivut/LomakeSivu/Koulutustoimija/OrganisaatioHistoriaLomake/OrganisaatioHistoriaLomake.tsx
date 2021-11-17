import * as React from 'react';
import { useContext } from 'react';
import styles from './OrganisaaatioHistoriaLomake.module.css';
import YksinkertainenTaulukko from '../../../../Taulukot/YksinkertainenTaulukko';
import { LanguageContext } from '../../../../../contexts/contexts';
import { Link } from 'react-router-dom';
import {
    HistoriaTaulukkoData,
    OrganisaatioHistoria,
    OrganisaationNimetNimi,
    OrganisaatioSuhde,
} from '../../../../../types/types';
import { OrganisaatioBase } from '../../../../../types/apiTypes';
import { Column } from 'react-table';

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

const OrganisaatioLink = ({ organisaatio }: { organisaatio: OrganisaatioBase }) => {
    const { i18n, language } = useContext(LanguageContext);
    return (
        <Link to={`/lomake/${organisaatio.oid}`}>
            {(organisaatio.nimi[language] ||
                organisaatio.nimi['fi'] ||
                organisaatio.nimi['sv'] ||
                organisaatio.nimi['en']) +
                (organisaatio.status !== 'AKTIIVINEN'
                    ? `(${i18n.translate('LABEL_' + organisaatio.status.toUpperCase())})`
                    : '')}
        </Link>
    );
};

const historiaMapper = (a: OrganisaatioSuhde, key: 'child' | 'parent') => {
    return {
        oid: a[key].oid,
        nimiHref: <OrganisaatioLink organisaatio={a[key]} />,
        alkuPvm: a.alkuPvm,
        status: a[key].status,
    };
};

export const mapColumnsToTableFormat = (
    orginalColumns: string[][] = [],
    i18n
): Column<HistoriaTaulukkoData | OrganisaationNimetNimi>[] => {
    const columnMapper = (column: string[]) => ({
        Header: i18n.translate(column[0]),
        accessor: column[1] as 'alkuPvm' | 'loppuPvm' | 'nimiHref',
    });
    return orginalColumns.map(columnMapper) as Column<HistoriaTaulukkoData | OrganisaationNimetNimi>[];
};

export default function OrganisaatioHistoriaLomake({ historia }: { historia: OrganisaatioHistoria }) {
    const { i18n } = useContext(LanguageContext);

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
                                tableColumns={mapColumnsToTableFormat(liittyneetColumns, i18n)}
                            />
                        )}
                        {yhdistettyData.length > 0 && (
                            <YksinkertainenTaulukko
                                data={yhdistettyData}
                                tableColumns={mapColumnsToTableFormat(yhdistettyColumns, i18n)}
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
                            tableColumns={mapColumnsToTableFormat(ylemmanTasonColumns, i18n)}
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
                            tableColumns={mapColumnsToTableFormat(sisaltyvatColumns, i18n)}
                        />
                    </div>
                </div>
            )}
        </div>
    );
}
