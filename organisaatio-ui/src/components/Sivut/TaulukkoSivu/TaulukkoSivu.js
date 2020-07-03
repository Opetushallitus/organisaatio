import React,  {useContext, useEffect, useState }  from 'react';
import Axios from "axios";
import styles from './TaulukkoSivu.module.css';
import { Icon } from '@iconify/react';
import chevronDown from '@iconify/icons-fa-solid/chevron-down';
import chevronUp from '@iconify/icons-fa-solid/chevron-up';
import chevronLeft from '@iconify/icons-fa-solid/chevron-left';
import chevronRight from '@iconify/icons-fa-solid/chevron-right';
import searchIcon from '@iconify/icons-fa-solid/search';
import Button from "@opetushallitus/virkailija-ui-components/Button";
import Input from "@opetushallitus/virkailija-ui-components/Input";
import Checkbox from "@opetushallitus/virkailija-ui-components/Checkbox";
import {LanguageContext} from '../../../contexts/contexts';




import PohjaSivu from "../PohjaSivu/PohjaSivu";
import {
    useTable,
    useGroupBy,
    useFilters,
    useSortBy,
    useExpanded,
    usePagination,
} from 'react-table';
import Spinner from "../../Spinner/Spinner";

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '';

const mapPaginationSelectors = (index) => {
  if (index < 3) return [0, 5];
  return [index-2, index+3];
}
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

    const columns = React.useMemo(
        () => [
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
                Cell: ({ row }) => <a href={`/lomake/${row.original.oid}`}>{row.original.nimi[language] || row.original.nimi.fi || row.original.nimi.sv || row.original.nimi.fi || row.original.nimi.en}</a>
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
        ],
        []
    );

    const data = organisaatiot;

    const {
        getTableProps,
        getTableBodyProps,
        headerGroups,
      prepareRow,
      page, // Instead of using 'rows', we'll use page,
      // which has only the rows for the active page

      // The rest of these things are super handy, too ;)
      canPreviousPage,
      canNextPage,
      pageOptions,
      pageCount,
      gotoPage,
      nextPage,
      previousPage,
      setPageSize,
      state: { pageIndex, pageSize },
    } = useTable({ columns, data, initialState: { pageIndex: 0 } },
      useExpanded, usePagination);
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
                    <table {...getTableProps()} style={{ width: '100%', borderSpacing: 0 }}>
                        <thead>
                        {headerGroups.map(headerGroup => (
                            <tr {...headerGroup.getHeaderGroupProps()}>
                                {headerGroup.headers.map(column => (
                                    <th
                                        {...column.getHeaderProps()}
                                        style={{ textAlign: 'left', borderBottom: '1px solid rgba(151,151,151,0.5)'}}
                                    >
                                        {column.render('Header')}
                                    </th>
                                ))}
                            </tr>
                        ))}
                        </thead>
                        <tbody {...getTableBodyProps()}>
                        {page.map((row, index) => {
                            prepareRow(row);
                            return (
                                <tr {...row.getRowProps()}>
                                    {row.cells.map(cell => {
                                        return (
                                            <td
                                                {...cell.getCellProps()}
                                                style={{
                                                    background: index % 2 === 0 ? '#F5F5F5': '#FFFFFF',
                                                }}
                                            >
                                                {cell.render('Cell')}
                                            </td>
                                        )
                                    })}
                                </tr>
                            )
                        })}
                        </tbody>
                    </table>
                  <div className={styles.PaginationContainer}>
                    <div className={styles.PaginationSivunvaihto}>
                    <Button variant='text' color="secondary" onClick={() => previousPage()} disabled={!canPreviousPage}>
                      <Icon icon={chevronLeft} />
                    </Button>
                    {pageOptions.slice(...mapPaginationSelectors(pageIndex)).map(option => {
                      if (option === pageIndex) return (<Button onClick={() => gotoPage(option)}>
                        {option+1}
                      </Button>);
                      return (<Button variant='text' color="secondary" onClick={() => gotoPage(option)}>
                        {option+1}
                      </Button>);
                    })}
                    <Button variant="text" color="secondary" onClick={() => nextPage()} disabled={!canNextPage}>
                      <Icon icon={chevronRight} />
                    </Button>

                    </div>
                    <div className={styles.PaginationYhteensa}>
                      <span>{i18n.translate('TAULUKKO_NAYTA_SIVULLA')}:</span>
                      <select
                        className={styles.NaytaSivullaSelect}
                        value={pageSize}
                        onChange={e => {
                          setPageSize(Number(e.target.value))
                        }}
                      >
                        {[10, 20, 30, 40, 50].map(pageSize => (
                          <option key={pageSize} value={pageSize}>
                            {pageSize}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>
            </div>
        </PohjaSivu>
    );
}

export default TaulukkoSivu;