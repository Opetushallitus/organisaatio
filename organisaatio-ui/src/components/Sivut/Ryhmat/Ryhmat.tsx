import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import styles from './Ryhmat.module.css';
import TyypitJaRyhmatKehys from '../TyypitJaRyhmatKehys/TyypitJaRyhmatKehys';
import { KoodistoContext } from '../../../contexts/KoodistoContext';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { dropKoodiVersionSuffix, mapLocalizedKoodiToLang } from '../../../tools/mappers';
import { getRyhmat } from '../../../api/ryhma';
import { Ryhma } from '../../../types/types';
import NormaaliTaulukko from '../../Taulukot/NormaaliTaulukko';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { Column } from 'react-table';
import { Link, useHistory } from 'react-router-dom';
import { LanguageContext } from '../../../contexts/LanguageContext';

const Ryhmat = () => {
    const { i18n, language } = useContext(LanguageContext);
    const { ryhmaTyypitKoodisto, kayttoRyhmatKoodisto } = useContext(KoodistoContext);
    const [ryhmat, setRyhmat] = useState<Ryhma[]>([]);
    const history = useHistory();
    const RyhmatColumns: Column<Ryhma>[] = React.useMemo(
        () => [
            {
                Header: i18n.translate('RYHMAT_RYHMAN_NIMI'),
                collapse: true,
                id: 'Nimi',
                Cell: ({ row }) => {
                    return (
                        <Link to={`/ryhmat/${row.original.oid}`} className={styles.nimenMaksimiPituus}>
                            {mapLocalizedKoodiToLang(language, 'nimi', row.original)}
                        </Link>
                    );
                },
                accessor: (values) => {
                    return mapLocalizedKoodiToLang(language, 'nimi', values);
                },
            },
            {
                Header: i18n.translate('RYHMAT_RYHMAN_TYYPPI'),
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
                Header: i18n.translate('RYHMAT_KAYTTOTARKOITUS'),
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
                Header: i18n.translate('RYHMAT_RYHMAN_TILA'),
                accessor: 'status',
            },
            {
                Header: i18n.translate('LABEL_OID'),
                accessor: 'oid',
            },
        ],
        [i18n, kayttoRyhmatKoodisto, language, ryhmaTyypitKoodisto]
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
        return <Spin />;
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
