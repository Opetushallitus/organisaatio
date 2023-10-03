import * as React from 'react';
import { useEffect, useState } from 'react';
import styles from './Ryhmat.module.css';
import TyypitJaRyhmatKehys from '../TyypitJaRyhmatKehys/TyypitJaRyhmatKehys';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { dropKoodiVersionSuffix } from '../../../tools/mappers';
import { getRyhmat } from '../../../api/ryhma';
import { Ryhma } from '../../../types/types';
import NormaaliTaulukko from '../../Taulukot/NormaaliTaulukko';
import { Column, Row } from 'react-table';
import { Link, useHistory } from 'react-router-dom';
import Loading from '../../Loading/Loading';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../api/lokalisaatio';
import { kayttoRyhmatKoodistoAtom, ryhmaTyypitKoodistoAtom } from '../../../api/koodisto';

const Ryhmat = () => {
    const [i18n] = useAtom(languageAtom);
    const [ryhmaTyypitKoodisto] = useAtom(ryhmaTyypitKoodistoAtom);
    const [kayttoRyhmatKoodisto] = useAtom(kayttoRyhmatKoodistoAtom);
    const [ryhmat, setRyhmat] = useState<Ryhma[]>([]);
    const history = useHistory();
    const RyhmatColumns: Column<Ryhma>[] = React.useMemo(
        () => [
            {
                Header: i18n.translate('RYHMAT_RYHMAN_NIMI'),
                collapse: true,
                id: 'Nimi',
                Cell: ({ row }: { row: Row<Ryhma> }) => {
                    return (
                        <Link to={`/ryhmat/${row.original.oid}`} className={styles.nimenMaksimiPituus}>
                            {i18n.translateNimi(row.original.nimi)}
                        </Link>
                    );
                },
                accessor: (values) => {
                    return i18n.translateNimi(values.nimi);
                },
            },
            {
                Header: i18n.translate('RYHMAT_RYHMAN_TYYPPI'),
                id: 'Tyyppi',
                Cell: ({ row }: { row: Row<Ryhma> }) => {
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
                Header: i18n.translate('RYHMAT_KAYTTOTARKOITUS'),
                id: 'Kayttotarkoitus',
                Cell: ({ row }: { row: Row<Ryhma> }) => {
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
                Header: i18n.translate('RYHMAT_RYHMAN_TILA'),
                accessor: 'status',
            },
            {
                Header: i18n.translate('LABEL_OID'),
                accessor: 'oid',
            },
        ],
        [i18n, kayttoRyhmatKoodisto, ryhmaTyypitKoodisto]
    );

    useEffect(() => {
        const fetch = async () => {
            try {
                const ryhmatData = await getRyhmat();
                setRyhmat([...ryhmatData] as Ryhma[]);
            } catch (error) {
                console.error('error fetching', error);
            }
        };
        fetch();
    }, []);

    const handleLisaaRyhma = () => {
        return history.push('/ryhmat/uusi');
    };

    if (ryhmat.length === 0) {
        return <Loading />;
    }

    return (
        <TyypitJaRyhmatKehys>
            <div className={styles.OtsikkoRivi}>
                <div className={styles.Otsikko}>
                    <h3>{i18n.translate('RYHMAT_OTSIKKO')}</h3>
                </div>
                <div>
                    <Button onClick={handleLisaaRyhma} className={styles.LisaaUusiBtn}>
                        {' '}
                        + {i18n.translate('RYHMAT_LISAA_UUSI')}
                    </Button>
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
