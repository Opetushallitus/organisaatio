import * as React from 'react';
import { useContext } from 'react';
import styles from './OrganisaaatioHistoriaLomake.module.css';
import YksinkertainenTaulukko from '../../../../Taulukot/YksinkertainenTaulukko';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { LanguageContext } from '../../../../../contexts/contexts';
import { Link } from 'react-router-dom';
import { OrganisaatioBase, OrganisaatioLiitos, OrganisaatioSuhde } from '../../../../../types/types';
import useOrganisaatioHistoria from '../../../../../api/useOrganisaatioHistoria';

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

const historiaSorter = (a: OrganisaatioSuhde | OrganisaatioLiitos, b: OrganisaatioSuhde | OrganisaatioLiitos) => {
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

const historiaMapperSuhde = (a: OrganisaatioSuhde, key: 'child' | 'parent') => {
    return {
        oid: a[key].oid,
        nimiHref: <OrganisaatioLink organisaatio={a[key]} />,
        alkuPvm: a.alkuPvm,
        status: a[key].status,
    };
};

const historiaMapperLiitos = (a: OrganisaatioLiitos, key: 'organisaatio' | 'kohde') => {
    return {
        oid: a[key].oid,
        nimiHref: <OrganisaatioLink organisaatio={a[key]} />,
        alkuPvm: a.alkuPvm,
        loppuPvm: a.loppuPvm,
        status: a[key].status,
    };
};

export default function OrganisaatioHistoriaLomake(props: { oid: string }) {
    const { i18n } = useContext(LanguageContext);
    const { historia, historiaLoading, historiaError } = useOrganisaatioHistoria(props.oid);

    const loading = () => {
        return !historia || historiaLoading || historiaError;
    };
    if (loading()) {
        return <Spin />;
    }

    const columnMapper = (column: string[]) => ({ Header: i18n.translate(column[0]), accessor: column[1] });

    const liittyneetData = historia.liitokset.sort(historiaSorter).map((a) => historiaMapperLiitos(a, 'organisaatio'));

    const yhdistettyData = historia.liittymiset.sort(historiaSorter).map((a) => historiaMapperLiitos(a, 'kohde'));

    const ylemmanTasonData = historia.parentSuhteet.sort(historiaSorter).map((a) => historiaMapperSuhde(a, 'parent'));

    const sisaltyvatData = historia.childSuhteet.sort(historiaSorter).map((a) => historiaMapperSuhde(a, 'child'));

    return (
        <div className={styles.UloinKehys}>
            {(liittyneetData.length > 0 || yhdistettyData.length > 0) && (
                <div className={styles.Rivi}>
                    <div>
                        <h2>{i18n.translate('RAKENNE_LIITOKSET_OTSIKKO')}</h2>
                        {liittyneetData.length > 0 && (
                            <YksinkertainenTaulukko
                                data={liittyneetData}
                                tableColumns={liittyneetColumns.map(columnMapper)}
                            />
                        )}
                        {yhdistettyData.length > 0 && (
                            <YksinkertainenTaulukko
                                data={yhdistettyData}
                                tableColumns={yhdistettyColumns.map(columnMapper)}
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
                            tableColumns={ylemmanTasonColumns.map(columnMapper)}
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
                            tableColumns={sisaltyvatColumns.map(columnMapper)}
                        />
                    </div>
                </div>
            )}
        </div>
    );
}
