import * as React from 'react';
import styles from './Ryhmat.module.css';
import TyypitJaRyhmatKehys from "../TyypitJaRyhmatKehys/TyypitJaRyhmatKehys";
import {useContext} from "react";
import {KoodistoContext, LanguageContext} from "../../../contexts/contexts";
import Button from "@opetushallitus/virkailija-ui-components/Button";
import Input from "@opetushallitus/virkailija-ui-components/Input";
import {Icon} from "@iconify/react";
import searchIcon from "@iconify/icons-fa-solid/search";
import Select from "@opetushallitus/virkailija-ui-components/Select";
import {useEffect} from "react";
import Axios from "axios";
import {useState} from "react";
import {Ryhma} from "../../../types/types";
import NormaaliTaulukko from "../../Taulukot/NormaaliTaulukko";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";

type Props = {
}

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '/organisaatio-ui';


const Ryhmat: React.FC = (props) => {
    const { ryhmaTyypitKoodisto, kayttoRyhmatKoodisto } = useContext(KoodistoContext);
    const [ryhmat, setRyhmat] = useState<Ryhma[] | undefined>(undefined);
    useEffect(() => {
        async function fetch() {
            try {
                const response = await Axios.get(`${urlPrefix}/organisaatio/v3/ryhmat?aktiivinen=true`);
                const data = response.data;
                console.log('data', data);
                setRyhmat([ ...data ]);
            } catch (error) {
                console.error('error fetching', error)
            }
        }
        fetch();
    }, []);
    const { i18n, language } = useContext(LanguageContext);

    if(!ryhmat) {
        return <Spin />;
    }
    const RyhmatColumns = [
        {
            Header: i18n.translate('RYHMAN_NIMI'),
            Cell: ({ row }: any) => {
                console.log(row);
                return <a href={`/ryhmat/muokkaus/${row.original.oid}`} className={styles.nimenMaksimiPituus}>{row.original.nimi[language] || row.original.nimi['fi'] || row.original.nimi['sv'] || row.original.nimi['en']}</a>
            },
            collapse: true,
        },
        {
            Header: i18n.translate('RYHMAN_TYYPPI'),
            Cell: ({ row }: any) => {
                if (row.original.tyypit.length > 0 ) {
                    return row.original.tyypit.map((tyyppi: string) => {
                        const koodityyppi = ryhmaTyypitKoodisto.koodit().find(rt => rt.uri === tyyppi.slice(0, -2));
                        if (koodityyppi) {
                            return koodityyppi.nimi[language] || koodityyppi.nimi['fi'] || koodityyppi.nimi['sv'] || koodityyppi.nimi['en']
                        }
                        return tyyppi
                    }).join(', ');
                }
                return '';
            },

        },
        {
            Header: i18n.translate('KAYTTOTARKOITUS'),
            Cell: ({ row }: any) => {
                if (row.original.kayttoryhmat.length > 0 ) {
                    return row.original.kayttoryhmat.map((tyyppi: string) => {
                        const koodiRyhma = kayttoRyhmatKoodisto.koodit().find(rt => rt.uri === tyyppi.slice(0, -2));
                        if (koodiRyhma) {
                            return koodiRyhma.nimi[language] || koodiRyhma.nimi['fi'] || koodiRyhma.nimi['sv'] || koodiRyhma.nimi['en']
                        }
                        return tyyppi
                    }).join(', ');
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
        }
    ];
    return(
        <TyypitJaRyhmatKehys>
            <div className={styles.OtsikkoRivi}>
                <div className={styles.Otsikko}>
                    <h3>
                        {i18n.translate('RYHMAT_OTSIKKO')}
                    </h3>
                </div>
                <div>
                    <Button className={styles.LisaaUusiBtn}> + {i18n.translate('RYHMAT_LISAA_UUSI')}</Button>
                </div>
            </div>
            <div className={styles.FiltteriContainer}>
                <div className={styles.FiltteriRivi}>
                    <div className={styles.FiltteriInputOsa}>
                        <Input placeholder={i18n.translate('RYHMAT_HAKU_PLACEHOLDER')} suffix={<Icon color="#999999" icon={searchIcon} />} />
                    </div>
                    <Button variant="outlined" style={{ borderRadius: '100%', height: '2rem', width: '2rem'}}>?</Button>
                </div>
                <div className={styles.FiltteriRivi}>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('RYHMAN_TYYPPI')}</label>
                        <Select
                            value={null}
                            options={[]}/>
                    </div>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('RYHMAN_KAYTTOTARKOITUS')}</label>
                        <Select
                            value={null}
                            options={[]}/>
                    </div>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('RYHMAT_TILA')}</label>
                        <Select
                            value={null}
                            options={[]}/>
                    </div>
                </div>
                <div className={styles.TaulukkoKehys}>
                    <NormaaliTaulukko data={ryhmat} tableColumns={RyhmatColumns} />
                </div>
            </div>
        </TyypitJaRyhmatKehys>
    );
}

export default Ryhmat;