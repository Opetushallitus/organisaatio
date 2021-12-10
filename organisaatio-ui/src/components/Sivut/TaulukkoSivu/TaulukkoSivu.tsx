import React, { useContext, useState } from 'react';
import styles from './TaulukkoSivu.module.css';
import { Icon } from '@iconify/react';
import chevronDown from '@iconify/icons-fa-solid/chevron-down';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { KoodistoContext, LanguageContext, ROOT_OID } from '../../../contexts/contexts';
import PohjaSivu from '../PohjaSivu/PohjaSivu';
import OrganisaatioHakuTaulukko from '../../Taulukot/OrganisaatioHakuTaulukko/OrganisaatioHakuTaulukko';

import { ReactComponent as LippuIkoni } from '../../../img/outlined_flag-white-18dp.svg';
import { Link } from 'react-router-dom';
import chevronRight from '@iconify/icons-fa-solid/chevron-right';
import { Column } from 'react-table';
import { ApiOrganisaatio } from '../../../types/apiTypes';

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
    const [isOPHVirkailija, setIsOPHVirkailija] = useState(true);

    const columns: (Column<ApiOrganisaatio> & { collapse?: boolean })[] = [
        {
            id: 'expander',
            collapse: true,
            Cell: ({ row }) =>
                row.canExpand ? (
                    <span
                        className={styles.Expander}
                        {...row.getToggleRowExpandedProps({
                            style: {
                                paddingLeft: `${row.depth + 1}rem`,
                            },
                        })}
                    >
                        {row.isExpanded ? <Icon icon={chevronDown} /> : <Icon icon={chevronRight} />}
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
                        {i18n.translateNimi(row.original.nimi)}
                        {row.original?.status !== 'AKTIIVINEN' && ` (${i18n.translate('LABEL_PASSIIVINEN')})`}
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
