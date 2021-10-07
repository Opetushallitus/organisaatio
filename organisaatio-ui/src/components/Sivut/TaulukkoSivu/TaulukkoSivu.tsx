import React, { useContext } from 'react';
import styles from './TaulukkoSivu.module.css';
import { Icon } from '@iconify/react';
import chevronDown from '@iconify/icons-fa-solid/chevron-down';
import chevronUp from '@iconify/icons-fa-solid/chevron-up';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { KoodistoContext, LanguageContext, ROOT_OID } from '../../../contexts/contexts';
import PohjaSivu from '../PohjaSivu/PohjaSivu';
import OrganisaatioHakuTaulukko from '../../Taulukot/OrganisaatioHakuTaulukko/OrganisaatioHakuTaulukko';

import { ReactComponent as LippuIkoni } from '../../../img/outlined_flag-white-18dp.svg';
import { Link } from 'react-router-dom';

const tarkastaLipunVari = (tarkastusPvm) => {
    const date = new Date();
    date.setFullYear(date.getFullYear() - 1);
    return !!tarkastusPvm ? tarkastusPvm - date.getTime() > 0 : false;
};

const TaulukkoSivu = (props) => {
    const handleLisaaUusiToimija = () => {
        return props.history.push(`/lomake/uusi?parentOid=${ROOT_OID}`);
    };
    const { i18n, language } = useContext(LanguageContext);
    const { kuntaKoodisto, organisaatioTyypitKoodisto } = useContext(KoodistoContext);

    // const [organisaatiot, setOrganisaatiot] = useState<Organisaatio[]>([]);
    // const [isLoading, setIsLoading] = useState(true);
    const [isOPHVirkailija, setIsOPHVirkailija] = React.useState(true);
    // useEffect(() => {
    //     async function fetch() {
    //         // TODO tämä on vielä auki että mistä osoitteesta haetaan, kun haetaan omia organisaatioita?
    //         try {
    //             setIsLoading(true);
    //             const response =
    //                 isOPHVirkailija || (!isOPHVirkailija && !omatOrganisaatiotSelected)
    //                     ? await Axios.get(
    //                           `/organisaatio/organisaatio/v4/hierarkia/hae?&aktiiviset=true&lakkautetut=${naytaPassivoidut}&searchstr=&suunnitellut=true`
    //                       )
    //                     : await Axios.get(
    //                           `/organisaatio/organisaatio/v4/hierarkia/hae?&aktiiviset=true&lakkautetut=${naytaPassivoidut}&searchstr=&suunnitellut=true&oid=1.2.246.562.10.59347432821`
    //                       );
    //             const data = response.data;
    //             setOrganisaatiot([...data.organisaatiot]);
    //             setIsLoading(false);
    //         } catch (error) {
    //             console.error('error fetching', error);
    //         }
    //     }
    //     fetch();
    // }, [naytaPassivoidut, isOPHVirkailija, omatOrganisaatiotSelected]);

    const columns: any = [
        {
            // Build our expander column
            id: 'expander', // Make sure it has an ID
            collapse: true,
            Cell: ({ row }) =>
                // Use the row.canExpand and row.getToggleRowExpandedProps prop getter
                // to build the toggle for expanding a row
                row.canExpand ? (
                    <span
                        className={styles.Expander}
                        {...row.getToggleRowExpandedProps({
                            style: {
                                // We can even use the row.depth property
                                // and paddingLeft to indicate the depth
                                // of the row
                                paddingLeft: `${row.depth + 1}rem`,
                            },
                        })}
                    >
                        {row.isExpanded ? <Icon icon={chevronUp} /> : <Icon icon={chevronDown} />}
                    </span>
                ) : null,
        },
        {
            Header: i18n.translate('TAULUKKO_NIMI'),
            id: 'Nimi',
            accessor: (values) => {
                return values.nimi[language] || values.nimi.fi || values.nimi.sv || values.nimi.fi || values.nimi.en;
            },
            Cell: ({ row }) => {
                return (
                    <Link to={`/lomake/${row.original.oid}`}>
                        {row.original.nimi[language] ||
                            row.original.nimi.fi ||
                            row.original.nimi.sv ||
                            row.original.nimi.fi ||
                            row.original.nimi.en}
                    </Link>
                );
            },
        },
        {
            Header: i18n.translate('TAULUKKO_KUNTA'),
            accessor: (values) => {
                const nimi = kuntaKoodisto.uri2Nimi(values.kotipaikkaUri);
                return nimi || '';
            },
        },
        {
            Header: i18n.translate('TAULUKKO_TYYPPIX'),
            accessor: (values) => {
                return values.tyypit;
            },
            Cell: ({ row }) => (
                <span>
                    {row.original.organisaatiotyypit.map((ot) => organisaatioTyypitKoodisto.uri2Nimi(ot)).join(', ')}
                </span>
            ),
        },
        {
            Header: i18n.translate('TAULUKKO_TUNNISTE'),
            accessor: 'ytunnus',
        },
        {
            Header: i18n.translate('LABEL_OID'),
            accessor: 'oid',
        },
        {
            Header: i18n.translate('TAULUKKO_TARKISTUS'),
            id: 'tarkistus',
            //   accessor: (values) => (tarkastaLipunVari(values.tarkastusPvm) ? null : 'tarkistus'),
            Cell: ({ row }) => (
                <div
                    className={`${styles.LippuNappi} ${
                        tarkastaLipunVari(row.original.tarkastusPvm) ? styles.SininenTausta : styles.PunainenTausta
                    }`}
                >
                    <LippuIkoni />
                </div>
            ),
        },
    ];

    return (
        <PohjaSivu>
            <Button color={isOPHVirkailija ? 'success' : 'danger'} onClick={() => setIsOPHVirkailija(!isOPHVirkailija)}>
                {' '}
                OphVirkailija?{' '}
            </Button>
            <div className={styles.PaaOsio}>
                <div className={styles.OtsikkoContainer}>
                    <h2>Organisaatiot</h2>
                    {isOPHVirkailija && (
                        <Button style={{ height: '3rem' }} onClick={handleLisaaUusiToimija}>
                            {' '}
                            + {i18n.translate('TAULUKKO_LISAA_UUSI_TOIMIJA')}
                        </Button>
                    )}
                </div>
                <div className={styles.TaulukkoContainer}>
                    <OrganisaatioHakuTaulukko isOPHVirkailija={isOPHVirkailija} tableColumns={columns} />
                </div>
            </div>
        </PohjaSivu>
    );
};

export default TaulukkoSivu;
