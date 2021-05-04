import * as React from 'react';
import styles from './OrganisaaatioHistoriaLomake.module.css';
import YksinkertainenTaulukko from '../../../../Taulukot/YksinkertainenTaulukko';
import useAxios from 'axios-hooks';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { Organisaatio } from '../../../../../types/types';

type organisaatioHistoriaProps = {
    oid?: string;
    handleOnChange: ({ name, value }: { name: keyof Organisaatio; value: any }) => void;
};

export default function OrganisaatioHistoriaLomake(props: organisaatioHistoriaProps) {
    const [{ data: historia, loading: historiaLoading, error: historiaError }] = useAxios(
        `/organisaatio/organisaatio/v4/1.2.246.562.10.69981965515/historia`
    );
    console.log('hist', historia);
    const liittyneetColumns = [
        {
            Header: 'Nimen voimassaolo',
            accessor: 'alkuPvm',
        },
        {
            Header: 'Liittyneet organisaatiot',
            accessor: 'organisaatiot',
        },
    ];
    const liittettyYlempaanColumns = [
        {
            Header: 'Alkupäivämäärä',
            accessor: 'alkuPvm',
        },
        {
            Header: 'Loppupäivämäärä',
            accessor: 'loppuPvm',
        },
        {
            Header: 'Ylemmän tason organisaatio',
            accessor: 'organisaatiot',
        },
    ];
    const liittettyYlempaanData = [
        {
            alkuPvm: '1.1.2015',
            loppuPvm: '1.1.2016',
            organisaatiot: 'Testiparentti 1',
        },
        {
            alkuPvm: '1.1.2016',
            loppuPvm: '2.2.2016',
            organisaatiot: 'Testiparentti 2',
        },
    ];
    const liittyneetData = [
        {
            alkuPvm: '1.1.2011',
            organisaatiot: 'Miikan organisaatio',
        },
        {
            alkuPvm: '1.1.2012',
            organisaatiot: 'Miikan uudempi organisaatio',
        },
        {
            alkuPvm: '1.1.2018',
            organisaatiot: 'Miikan uusin organisaatio',
        },
    ];

    const liitetytData = [
        {
            alkuPvm: '1.1.2015',
            loppuPvm: '1.1.2016',
            organisaatiot: 'Testiparentti 1',
        },
        {
            alkuPvm: '1.1.2016',
            loppuPvm: '2.2.2016',
            organisaatiot: 'Testiparentti 2',
        },
    ];
    const liitetytColumns = [
        {
            Header: 'Alkupäivämäärä',
            accessor: 'alkuPvm',
        },
        {
            Header: 'Loppupäivämäärä',
            accessor: 'loppuPvm',
        },
        {
            Header: 'Sisältyvät organisaatiot',
            accessor: 'organisaatiot',
        },
    ];
    if (historiaLoading || historiaError) {
        return <Spin />;
    }
    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div>
                    <h2>Organisaatioon tehdyt liitokset</h2>
                    <YksinkertainenTaulukko data={liittyneetData} tableColumns={liittyneetColumns} />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div>
                    <h2>Organisaatio liitetty ylempään organisaatioon</h2>
                    <YksinkertainenTaulukko data={liittettyYlempaanData} tableColumns={liittettyYlempaanColumns} />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div>
                    <h2>Organisaation liitetyt toiset organisaatiot</h2>
                    <YksinkertainenTaulukko data={liitetytData} tableColumns={liitetytColumns} />
                </div>
            </div>
        </div>
    );
}
