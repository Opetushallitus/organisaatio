import * as React from 'react';
import styles from './Tyypit.module.css';
import TyypitJaRyhmatKehys from "../TyypitJaRyhmatKehys/TyypitJaRyhmatKehys";
import {useContext} from "react";
import {LanguageContext} from "../../../contexts/contexts";
import Button from "@opetushallitus/virkailija-ui-components/Button";
import {useEffect} from "react";
import Axios from "axios";
import {useState} from "react";
import {Koodi, YhteystietoTyyppi} from "../../../types/types";
import NormaaliTaulukko from "../../Taulukot/NormaaliTaulukko";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";


const nimiMapper = (nimi: any, language: string) => {
    if(nimi.teksti && nimi.teksti.length > 0) {
        let lokalisoituNimi = nimi.teksti.find((nT: any) => nT.kieliKoodi === language);
        if (!lokalisoituNimi) {
            lokalisoituNimi = nimi.teksti[0]
        }
        return lokalisoituNimi.value || 'Ei nimeä > muokkaa!'
    }
    return '';
}
type Props = {
    tyyppi: 'lisatietotyypit' | 'yhteystietojentyyppi'
}

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '';

const Tyypit = (props: Props) => {
    const { i18n, language } = useContext(LanguageContext);
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

    if(!tyypit) {
        return <Spin />;
    }
    const tyypitColumns = [
        {
            Header: i18n.translate('TYYPIT_NIMI'),
            Cell: ({row}: { row: any}) => {
                if(typeof row.original === 'string'){
                    return <a href={`/${props.tyyppi === 'yhteystietojentyyppi' ? 'yhteystietotyypit' : 'lisatietotyypit'}/muokkaus`}>{row.original}</a>
                }
                return <a href={`/${props.tyyppi === 'yhteystietojentyyppi' ? 'yhteystietotyypit' : 'lisatietotyypit'}/muokkaus`}>{nimiMapper(row.original.nimi, language)}</a>;
            },
        },
    ];
    return(
        <TyypitJaRyhmatKehys>
            <div className={styles.OtsikkoRivi}>
                <div className={styles.Otsikko}>
                    <h3>
                        {props.tyyppi === 'yhteystietojentyyppi' ? i18n.translate('YHTEYSTIETOTYYPIT_OTSIKKO') : i18n.translate('LISÄTIETOTYYPIT_OTSIKKO')}
                    </h3>
                </div>
                <div>
                    <Button className={styles.LisaaUusiBtn}> + {props.tyyppi === 'yhteystietojentyyppi' ? i18n.translate('YHTEYSTIETOTYYPIT_LISAA_UUSI') : i18n.translate('LISÄTIETOTYYPIT_LISAA_UUSI')}</Button>
                </div>
            </div>
            <div className={styles.TaulukkoKehys}>
                <NormaaliTaulukko data={tyypit} tableColumns={tyypitColumns} />
            </div>
        </TyypitJaRyhmatKehys>
    );
}

export default Tyypit;