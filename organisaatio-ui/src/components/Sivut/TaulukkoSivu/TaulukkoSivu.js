import React,  {useContext, useEffect, useState }  from 'react';
import Axios from "axios";
import styles from './TaulukkoSivu.module.css';
import { Icon } from '@iconify/react';
import chevronDown from '@iconify/icons-fa-solid/chevron-down';
import chevronUp from '@iconify/icons-fa-solid/chevron-up';
import searchIcon from '@iconify/icons-fa-solid/search';
import Button from "@opetushallitus/virkailija-ui-components/Button";
import Input from "@opetushallitus/virkailija-ui-components/Input";
import Checkbox from "@opetushallitus/virkailija-ui-components/Checkbox";
import {LanguageContext} from '../../../contexts/contexts';




import PohjaSivu from "../PohjaSivu/PohjaSivu";
import NormaaliTaulukko from "../../Taulukot/NormaaliTaulukko";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '/organisaatio-ui';

const TaulukkoSivu = (props) => {
  const { i18n, language } = useContext(LanguageContext);
  const [organisaatiot, setOrganisaatiot] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  useEffect(() => {
    async function fetch() {
      try {
        const response = await Axios.get(`${urlPrefix}/organisaatio/v4/hierarkia/hae?noCache=1593282230071&aktiiviset=true&lakkautetut=false&searchstr=haagan&suunnitellut=true`);
        const data = response.data;
        console.log('data', data);
        setOrganisaatiot([ ...data.organisaatiot ]);
        setIsLoading(false);
      } catch (error) {
        console.error('error fetching', error)
      }
    }
    fetch();
  }, []);

    const columns = [
          {
            // Build our expander column
            id: 'expander', // Make sure it has an ID
            Cell: ({ row }) =>
              // Use the row.canExpand and row.getToggleRowExpandedProps prop getter
              // to build the toggle for expanding a row
              row.canExpand ? (
                <span
                  {...row.getToggleRowExpandedProps({
                    style: {
                      // We can even use the row.depth property
                      // and paddingLeft to indicate the depth
                      // of the row
                      paddingLeft: `${row.depth * 2}rem`,
                    },
                  })}
                >
                {row.isExpanded ? <Icon icon={chevronUp} /> : <Icon icon={chevronDown} />}
                </span>
              ) : null,
          },
            {
                Header: 'Nimi',
                Cell: ({ row }) => <a href={`/organisaatio-ui/lomake/${row.original.oid}`}>{row.original.nimi[language] || row.original.nimi.fi || row.original.nimi.sv || row.original.nimi.fi || row.original.nimi.en}</a>
            },
            {
                Header: 'Kunta',
                accessor: 'kotipaikkaUri',
            },
            {
                Header: 'Tyyppi',
                Cell: ({ row }) => row.original.organisaatiotyypit.map(ot => <span>ot</span>)
            },
            {
                Header: 'Tunniste',
                accessor: 'ytunnus',
            },
            {
                Header: 'Oid',
                accessor: 'oid',
                collapse: true,
            },
            {
              Header: 'Tarkistus',
              id: 'tarkistus',
              Cell: ({ row }) => {
                return <span>Lippu</span>
              }
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
                      onClick={()=> {}}> + {i18n.translate('LISAA_UUSI_TOIMIJA')}
                    </Button>
                </div>
                <div className={styles.TaulukkoContainer}>
                    <div className={styles.FiltteriContainer}>
                      <div className={styles.FiltteriInputOsa}>
                        <Input placeholder={i18n.translate('TOIMIJA_HAKU_PLACEHOLDER')} suffix={<Icon color="#999999" icon={searchIcon} />} />
                        <Checkbox checked={true}>
                          {i18n.translate('CHECKBOX_NAYTA_PASSIVOIDUT')}
                        </Checkbox>
                      </div>
                      <Button variant="outlined" style={{ borderRadius: '100%', height: '2rem', width: '2rem'}}>?</Button>
                    </div>

                    <NormaaliTaulukko data={data} tableColumns={columns} />
                </div>
            </div>
        </PohjaSivu>
    );
}

export default TaulukkoSivu;