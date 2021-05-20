import * as React from 'react';
import styles from './Ryhmat.module.css';
import TyypitJaRyhmatKehys from '../TyypitJaRyhmatKehys/TyypitJaRyhmatKehys';
import { useContext } from 'react';
import { KoodistoContext, LanguageContext } from '../../../contexts/contexts';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { useEffect } from 'react';
import Axios, { AxiosResponse } from 'axios';
import { useState } from 'react';
import { Ryhma } from '../../../types/types';
import NormaaliTaulukko from '../../Taulukot/NormaaliTaulukko';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { Column } from 'react-table';

export const dropKoodiVersionSuffix = (koodi: string) => {
    const hasVersioningHashtag = koodi.search('#');
    if (hasVersioningHashtag && hasVersioningHashtag) {
        return koodi.slice(0, hasVersioningHashtag);
    }
    return koodi;
};

const Ryhmat: React.FC = () => {
    const { i18n, language } = useContext(LanguageContext);
    const { ryhmaTyypitKoodisto, kayttoRyhmatKoodisto } = useContext(KoodistoContext);
    const [ryhmat, setRyhmat] = useState<Ryhma[]>([]);
    // TODO TS jutut ja haku mapperit tyyppien ja käyttötarkoituksen osalta
    const RyhmatColumns: Column<Ryhma>[] = React.useMemo(
        () => [
            {
                Header: i18n.translate('RYHMAN_NIMI'),
                id: 'Nimi',
                Cell: ({ row }) => {
                    return (
                        <a href={`/ryhmat/muokkaus/${row.original.oid}`} className={styles.nimenMaksimiPituus}>
                            {row.original.nimi[language] ||
                                row.original.nimi['fi'] ||
                                row.original.nimi['sv'] ||
                                row.original.nimi['en']}
                        </a>
                    );
                },
                accessor: (values) => {
                    return values.nimi[language] || values.nimi.fi || values.nimi.sv || values.nimi.en || '';
                },
                //collapse: true,
            },
            {
                Header: i18n.translate('RYHMAN_TYYPPI'),
                id: 'Tyyppi',
                Cell: ({ row }) => {
                    if (row.original.ryhmatyypit.length > 0) {
                        return row.original.ryhmatyypit
                            .map((tyyppi: string) => ryhmaTyypitKoodisto.uri2Nimi(dropKoodiVersionSuffix(tyyppi)))
                            .join(', ');
                    }
                    return '';
                },
                accessor: (row) => {
                    if (row.ryhmatyypit.length > 0) {
                        return row.ryhmatyypit
                            .map((tyyppi: string) => ryhmaTyypitKoodisto.uri2Nimi(dropKoodiVersionSuffix(tyyppi)))
                            .join(', ');
                    }
                    return '';
                },
            },
            {
                Header: i18n.translate('KAYTTOTARKOITUS'),
                id: 'Kayttotarkoitus',
                Cell: ({ row }) => {
                    if (row.original.kayttoryhmat.length > 0) {
                        return row.original.kayttoryhmat
                            .map((tyyppi: string) => kayttoRyhmatKoodisto.uri2Nimi(dropKoodiVersionSuffix(tyyppi)))
                            .join(', ');
                    }
                    return '';
                },
                accessor: (row) => {
                    if (row.kayttoryhmat.length > 0) {
                        return row.kayttoryhmat
                            .map((tyyppi: string) => kayttoRyhmatKoodisto.uri2Nimi(dropKoodiVersionSuffix(tyyppi)))
                            .join(', ');
                    }
                    return '';
                },
            },
            {
                Header: i18n.translate('RYHMAN_TILA'),
                accessor: 'status',
            },
            {
                Header: i18n.translate('OID'),
                accessor: 'oid',
            },
        ],
        [i18n, kayttoRyhmatKoodisto, language, ryhmaTyypitKoodisto]
    );

    useEffect(() => {
        const fetch = async () => {
            try {
                const response = (await Axios.get(
                    `/organisaatio/organisaatio/v3/ryhmat?aktiivinen=true`
                )) as AxiosResponse;
                const ryhmatData = response.data;
                setRyhmat([...ryhmatData]);
            } catch (error) {
                console.error('error fetching', error);
            }
        };
        fetch();
    }, []);

    if (ryhmat.length === 0) {
        return <Spin />;
    }

    return (
        <TyypitJaRyhmatKehys>
            <div className={styles.OtsikkoRivi}>
                <div className={styles.Otsikko}>
                    <h3>{i18n.translate('RYHMAT_OTSIKKO')}</h3>
                </div>
                <div>
                    <Button className={styles.LisaaUusiBtn}> + {i18n.translate('RYHMAT_LISAA_UUSI')}</Button>
                </div>
            </div>
            <div>
                <div className={styles.TaulukkoKehys}>
                    <NormaaliTaulukko useHakuFiltteri ryhmatData={ryhmat} ryhmatColumns={RyhmatColumns} />
                </div>
            </div>
        </TyypitJaRyhmatKehys>
    );
};

export default Ryhmat;
