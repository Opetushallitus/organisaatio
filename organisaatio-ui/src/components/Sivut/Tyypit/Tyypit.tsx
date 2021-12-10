import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import styles from './Tyypit.module.css';
import TyypitJaRyhmatKehys from '../TyypitJaRyhmatKehys/TyypitJaRyhmatKehys';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Axios from 'axios';
import { YhteystietoTyyppi } from '../../../types/types';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import NormaaliTaulukko from '../../Taulukot/NormaaliTaulukko';
import { Column } from 'react-table';
import { LanguageContext } from '../../../contexts/LanguageContext';

const nimiMapper = (nimi: any, language: string) => {
    if (nimi.teksti && nimi.teksti.length > 0) {
        let lokalisoituNimi = nimi.teksti.find((nT: any) => nT.kieliKoodi === language);
        if (!lokalisoituNimi) {
            lokalisoituNimi = nimi.teksti[0];
        }
        return lokalisoituNimi.value || 'Ei nimeä > muokkaa!';
    }
    return '';
};
type Props = {
    tyyppi: 'lisatietotyypit' | 'yhteystietojentyyppi';
};

const Tyypit = (props: Props) => {
    const { i18n, language } = useContext(LanguageContext);
    const [tyypit, setTyypit] = useState<YhteystietoTyyppi[]>([]);
    useEffect(() => {
        async function fetch() {
            try {
                const response = await Axios.get(
                    `/organisaatio/${
                        props.tyyppi === 'yhteystietojentyyppi' ? props.tyyppi : `lisatieto/${props.tyyppi}`
                    }`
                );
                const data = response.data;
                setTyypit([...data]);
            } catch (error) {
                console.error('error fetching', error);
            }
        }

        fetch();
    }, [props.tyyppi]);

    if (!tyypit) {
        return <Spin />;
    }
    const tyypitColumns = [
        {
            Header: i18n.translate('TYYPIT_NIMI'),
            Cell: ({ row }: { row: { original: string | { nimi: any } } }) => {
                if (typeof row.original === 'string') {
                    return (
                        <a
                            href={`/organisaatio/${
                                props.tyyppi === 'yhteystietojentyyppi' ? 'yhteystietotyypit' : 'lisatietotyypit'
                            }/muokkaus/${row.original}`}
                        >
                            {row.original}
                        </a>
                    );
                }
                return (
                    <a
                        href={`/organisaatio/${
                            props.tyyppi === 'yhteystietojentyyppi' ? 'yhteystietotyypit' : 'lisatietotyypit'
                        }/muokkaus/${nimiMapper(row.original.nimi, language)}`}
                    >
                        {nimiMapper(row.original.nimi, language)}
                    </a>
                );
            },
        },
    ] as Column<YhteystietoTyyppi>[];
    return (
        <TyypitJaRyhmatKehys>
            <div className={styles.OtsikkoRivi}>
                <div className={styles.Otsikko}>
                    <h3>
                        {props.tyyppi === 'yhteystietojentyyppi'
                            ? i18n.translate('TYYPIT_YHTEYSTIETOTYYPIT_OTSIKKO')
                            : i18n.translate('TYYPIT_LISÄTIETOTYYPIT_OTSIKKO')}
                    </h3>
                </div>
                <div>
                    <Button className={styles.LisaaUusiBtn}>
                        {' '}
                        +{' '}
                        {props.tyyppi === 'yhteystietojentyyppi'
                            ? i18n.translate('TYYPIT_YHTEYSTIETOTYYPIT_LISAA_UUSI')
                            : i18n.translate('TYYPIT_LISÄTIETOTYYPIT_LISAA_UUSI')}
                    </Button>
                </div>
            </div>
            <div className={styles.TaulukkoKehys}>
                <NormaaliTaulukko yhteystietoTyypitData={tyypit} yhteystietotyypitColumns={tyypitColumns} />
            </div>
        </TyypitJaRyhmatKehys>
    );
};

export default Tyypit;
