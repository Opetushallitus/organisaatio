import React,  {useContext, useEffect, useState }  from 'react';
import Axios from "axios";
import styles from './TaulukkoSivu.module.css';
import { Icon } from '@iconify/react';
import chevronDown from '@iconify/icons-fa-solid/chevron-down';
import chevronUp from '@iconify/icons-fa-solid/chevron-up';
import Button from "@opetushallitus/virkailija-ui-components/Button";
import {LanguageContext, KoodistoContext} from '../../../contexts/contexts';
import PohjaSivu from "../PohjaSivu/PohjaSivu";
import OrganisaatioHakuTaulukko from "../../Taulukot/OrganisaatioHakuTaulukko/OrganisaatioHakuTaulukko";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";

import {ReactComponent as LippuIkoni} from '../../../img/outlined_flag-white-18dp.svg';

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '/organisaatio-ui';

const TaulukkoSivu = (props) => {
  const handleLisaaUusiToimija = () => {
    return props.history.push('/lomake/uusi');
  }
  const { i18n, language } = useContext(LanguageContext);
  const { kuntaKoodisto, organisaatioTyypitKoodisto } = useContext(KoodistoContext);

  const [organisaatiot, setOrganisaatiot] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [naytaPassivoidut, setNaytaPassivoidut] = React.useState(false);
  useEffect(() => {
    async function fetch() {
      try {
        setIsLoading(true);
        const response = await Axios.get(`${urlPrefix}/organisaatio/v4/hierarkia/hae?&aktiiviset=true&lakkautetut=${naytaPassivoidut}&searchstr=&suunnitellut=true`);
        const data = response.data;
        console.log('data', data);
        setOrganisaatiot([ ...data.organisaatiot ]);
        setIsLoading(false);
      } catch (error) {
        console.error('error fetching', error)
      }
    }
    fetch();
  }, [naytaPassivoidut]);

    const columns = [
          {
            // Build our expander column
            id: 'expander', // Make sure it has an ID
            collapse: true,
            Cell: ({ row }) =>
              // Use the row.canExpand and row.getToggleRowExpandedProps prop getter
              // to build the toggle for expanding a row
              row.canExpand ? (
                <span className={styles.Expander}
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
                Header: 'Nimi',
                id: 'Nimi',
                accessor: (values) => {
                  return values.nimi[language] || values.nimi.fi || values.nimi.sv || values.nimi.fi || values.nimi.en
                },
                Cell: ({ row }) => {
                  return <a
                    href={`/organisaatio-ui/lomake/${row.original.oid}`}>{row.original.nimi[language] || row.original.nimi.fi || row.original.nimi.sv || row.original.nimi.fi || row.original.nimi.en}</a>
                }
            },
            {
                Header: 'Kunta',
                accessor: (values) => {
                  const nimi = kuntaKoodisto.uri2Nimi(values.kotipaikkaUri);
                  return nimi || '';
                },
            },
            {
                Header: 'Tyyppi',
                accessor: (values) => values.organisaatiotyypit.map(ot =>ot),
                Cell: ({ row }) => <span>{row.original.organisaatiotyypit.map(ot => organisaatioTyypitKoodisto.uri2Nimi(ot)).join(', ')}</span>
            },
            {
                Header: 'Tunniste',
                accessor: 'ytunnus',
            },
            {
                Header: 'Oid',
                accessor: 'oid',
            },
            {
              Header: 'Tarkistus',
              id: 'tarkistus',
              Cell: ({ row }) => <div className={styles.LippuNappi}><LippuIkoni /></div>,
            }
        ];

    const data = organisaatiot;
    if(isLoading) {
      return <Spin/>;
    }
    return(
        <PohjaSivu>
            <div className={styles.PaaOsio} >
                <div className={styles.OtsikkoContainer}>
                    <h2>Organisaatiot</h2>
                    <Button style={{ height: '3rem'}}
                      onClick={handleLisaaUusiToimija}> + {i18n.translate('LISAA_UUSI_TOIMIJA')}
                    </Button>
                </div>
                <div className={styles.TaulukkoContainer}>
                    <OrganisaatioHakuTaulukko
                      data={data}
                      tableColumns={columns}
                      naytaPassivoidut={naytaPassivoidut}
                      setNaytaPassivoidut={setNaytaPassivoidut}
                    />
                </div>
            </div>
        </PohjaSivu>
    );
}

export default TaulukkoSivu;