import * as React from 'react';
import styles from './Tyypit.module.css';
import TyypitJaRyhmatKehys from "../TyypitJaRyhmatKehys/TyypitJaRyhmatKehys";
import {useContext} from "react";
import {LanguageContext} from "../../../contexts/contexts";
import Button from "@opetushallitus/virkailija-ui-components/Button";
import {useEffect} from "react";
import Axios from "axios";
import {useState} from "react";
import {YhteystietoTyyppi} from "../../../types/types";
import NormaaliTaulukko from "../../Taulukot/NormaaliTaulukko";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";


type Props = {
    tyyppi: 'lisatietotyypit' | 'yhteystietojentyyppi'
}

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '';


const Tyypit = (props: Props) => {
    const [tyypit, setTyypit] = useState<string[] | YhteystietoTyyppi[] |undefined>(undefined);
    useEffect(() => {
        async function fetch() {
            try {
                const response = await Axios.get(`${urlPrefix}/${props.tyyppi === 'yhteystietojentyyppi' ? props.tyyppi : `lisatieto/${props.tyyppi}`}`);
                const data = response.data;
                console.log('data', data);
                setTyypit([ ...data ]);
            } catch (error) {
                console.error('error fetching', error)
            }
        }
        fetch();
    }, []);
    const { i18n, language } = useContext(LanguageContext);

    if(!tyypit) {
        return <Spin />;
    }
    const yhteystietotyypitColumns = [
        {
            Header: i18n.translate('TYYPIT_NIMI'),
            accessor: 'nimi',
        },
    ];
    return(
        <TyypitJaRyhmatKehys>
            <div className={styles.OtsikkoRivi}>
                <div className={styles.Otsikko}>
                    <h3>
                        {i18n.translate('YHTEYSTIETOTYYPIT_OTSIKKO')}
                    </h3>
                </div>
                <div>
                    <Button className={styles.LisaaUusiBtn}> + {i18n.translate('YHTEYSTIETOTYYPIT_LISAA_UUSI')}</Button>
                </div>
            </div>
            <div className={styles.TaulukkoKehys}>
                <NormaaliTaulukko data={tyypit} tableColumns={yhteystietotyypitColumns} />
            </div>
        </TyypitJaRyhmatKehys>
    );
}

export default Tyypit;