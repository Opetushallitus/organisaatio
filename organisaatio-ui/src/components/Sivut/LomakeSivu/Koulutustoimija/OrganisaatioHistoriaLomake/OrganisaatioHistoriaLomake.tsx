import * as React from 'react';
import { useContext } from 'react';
import styles from './OrganisaaatioHistoriaLomake.module.css';
import YksinkertainenTaulukko from '../../../../Taulukot/YksinkertainenTaulukko';
import useAxios from 'axios-hooks';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { Organisaatio } from '../../../../../types/types';
import { LanguageContext } from '../../../../../contexts/contexts';
import { Link } from 'react-router-dom';

type organisaatioHistoriaProps = {
    oid?: string;
    handleOnChange: ({ name, value }: { name: keyof Organisaatio; value: any }) => void;
};

export default function OrganisaatioHistoriaLomake(props: organisaatioHistoriaProps) {
    const { i18n, language } = useContext(LanguageContext);
    const [{ data: historia, loading: historiaLoading, error: historiaError }, ,] = useAxios(
        `/organisaatio/organisaatio/v4/${props.oid}/historia`
    );
    console.log('hist', historiaLoading, historia);
    if (!historia || historiaLoading || historiaError) {
        return <Spin />;
    }

    const renderLink = (a) => {
        return (
            <Link to={`/lomake/${a.oid}`}>
                {(a.nimi[language] || a.nimi['fi'] || a.nimi['sv'] || a.nimi['en']) +
                    (a.status !== 'AKTIIVINEN' ? '(' + i18n.translate('LABEL_' + a.status.toUpperCase()) + ')' : '')}
            </Link>
        );
    };
    const historiaSorter = (a, b) => {
        return a.alkuPvm.localeCompare(b.alkuPvm);
    };
    const historiaMapper = (a, key) => {
        return {
            oid: a[key].oid,
            nimiHref: renderLink(a[key]),
            alkuPvm: a.alkuPvm,
            loppuPvm: a.loppuPvm,
            status: a[key].status,
        };
    };

    const liittyneetColumns = [
        {
            Header: i18n.translate('RAKENNE_LIITOSPVM'),
            accessor: 'alkuPvm',
        },
        {
            Header: i18n.translate('RAKENNE_LIITTYNEET_NIMI'),
            accessor: 'nimiHref',
        },
    ];
    const yhdistettyColumns = [
        {
            Header: i18n.translate('RAKENNE_LIITOSPVM'),
            accessor: 'alkuPvm',
        },
        {
            Header: i18n.translate('RAKENNE_YHDISTETTY_NIMI'),
            accessor: 'nimiHref',
        },
    ];
    const ylemmanTasonColumns = [
        {
            Header: i18n.translate('RAKENNE_ALKUPVM'),
            accessor: 'alkuPvm',
        },
        {
            Header: i18n.translate('RAKENNE_LOPPUPVM'),
            accessor: 'loppuPvm',
        },
        {
            Header: i18n.translate('RAKENNE_YLEMMAN_TASON_NIMI'),
            accessor: 'nimiHref',
        },
    ];

    const sisaltyvatColumns = [
        {
            Header: i18n.translate('RAKENNE_ALKUPVM'),
            accessor: 'alkuPvm',
        },
        {
            Header: i18n.translate('RAKENNE_LOPPUPVM'),
            accessor: 'loppuPvm',
        },
        {
            Header: i18n.translate('RAKENNE_SISALTYVAT_NIMI'),
            accessor: 'nimiHref',
        },
    ];

    const liittyneetData = historia.liitokset.map((a) => historiaMapper(a, 'organisaatio')).sort(historiaSorter);

    const yhdistettyData = historia.liittymiset.map((a) => historiaMapper(a, 'kohde')).sort(historiaSorter);

    const ylemmanTasonData = historia.parentSuhteet.map((a) => historiaMapper(a, 'parent')).sort(historiaSorter);

    const sisaltyvatData = historia.childSuhteet.map((a) => historiaMapper(a, 'child')).sort(historiaSorter);

    return (
        <div className={styles.UloinKehys}>
            {(liittyneetData.length > 0 || yhdistettyData.length > 0) && (
                <div className={styles.Rivi}>
                    <div>
                        <h2>{i18n.translate('RAKENNE_LIITOKSET_OTSIKKO')}</h2>
                        {liittyneetData.length > 0 && (
                            <YksinkertainenTaulukko data={liittyneetData} tableColumns={liittyneetColumns} />
                        )}
                        {yhdistettyData.length > 0 && (
                            <YksinkertainenTaulukko data={yhdistettyData} tableColumns={yhdistettyColumns} />
                        )}
                    </div>
                </div>
            )}
            {ylemmanTasonData.length > 0 && (
                <div className={styles.Rivi}>
                    <div>
                        <h2>{i18n.translate('RAKENNE_YLEMMAN_TASON_OTSIKKO')}</h2>
                        <YksinkertainenTaulukko data={ylemmanTasonData} tableColumns={ylemmanTasonColumns} />
                    </div>
                </div>
            )}
            {sisaltyvatData.length > 0 && (
                <div className={styles.Rivi}>
                    <div>
                        <h2>{i18n.translate('RAKENNE_SISALTYVAT_OTSIKKO')}</h2>
                        <YksinkertainenTaulukko data={sisaltyvatData} tableColumns={sisaltyvatColumns} />
                    </div>
                </div>
            )}
        </div>
    );
}
