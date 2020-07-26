import * as React from 'react';
import styles from './RyhmanMuokkaus.module.css';
import {useContext} from "react";
import {KoodistoContext, LanguageContext} from "../../../../contexts/contexts";
import Input from "@opetushallitus/virkailija-ui-components/Input";
import PohjaSivu from "../../PohjaSivu/PohjaSivu";
import Icon from "@iconify/react";
import homeIcon from "@iconify/icons-fa-solid/home";
import Button from "@opetushallitus/virkailija-ui-components/Button";
import {useState} from "react";
import Select from "@opetushallitus/virkailija-ui-components/Select";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";
import {useEffect} from "react";
import Axios from "axios";

// const KAIKKIVALITTU = '1';

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '';

type Props = {
    match: any
}

export default function RyhmanMuokkaus(props: Props) {
    const { i18n, language } = useContext(LanguageContext);
    const { ryhmaTyypitKoodisto, kayttoRyhmatKoodisto } = useContext(KoodistoContext);
    // const [isKaikkiValittu, setIsKaikkiValittu ] = useState(KAIKKIVALITTU);
    const [ryhma, setRyhma] = useState<undefined | any>();
    //const [isModaliAuki, setIsModaaliAuki ] = useState(false);
    const { match: { params } } = props;
    useEffect(() => {
        async function fetch() {
            try {
                const response = await Axios.get(`${urlPrefix}/organisaatio/v4/${params.oid}?includeImage=true`);
                const ryhma = response.data;
                setRyhma(ryhma);
            } catch (error) {
                console.error('error fetching', error)
            }
        }
        fetch();
    }, [params.oid]);


    if (!ryhma || !ryhmaTyypitKoodisto || !kayttoRyhmatKoodisto) {
        return <Spin />;
    }

    const ryhmaTyypitOptions = ryhmaTyypitKoodisto.koodit().map((k: any) => ({
        value: k.uri,
        label: k.nimi[language] || k.nimi['fi'] || k.nimi['sv'] || k.nimi['en'],
    }));

    const kayttoRyhmatOptions = kayttoRyhmatKoodisto.koodit().map((k: any) => ({
        value: k.uri,
        label: k.nimi[language] || k.nimi['fi'] || k.nimi['sv'] || k.nimi['en'],
    }));
    console.log('yhtopts', ryhmaTyypitOptions,kayttoRyhmatOptions, 'ryhma', ryhma);
    const kayttoRyhmat = kayttoRyhmatOptions.filter(rt => ryhma.kayttoryhmat.map((k: string) => k.slice(0, -2)).includes(rt.value) || ryhma.kayttoryhmat.includes(rt.value));
    const ryhmaTyypit = ryhmaTyypitOptions.filter(rt => ryhma.ryhmatyypit.map((k: string) => k.slice(0, -2)).includes(rt.value) || ryhma.ryhmatyypit.includes(rt.value));
    return(
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <a href="/ryhmat"><Icon icon={homeIcon} /></a>
                </div>
                <div>
                    <a href="/ryhmat">{i18n.translate('KAIKKI_RYHMAT')}</a>
                </div>
            </div>
            <div className={styles.PaaKehys}>
                <div className={styles.ValiContainer}>
                    <div className={styles.ValiOtsikko}>
                        <h3>{i18n.translate('RYHMA')}</h3>
                        <h1>{i18n.translate('UUDEN_RYHMAN_LISAAMINEN')}</h1>
                    </div>
                </div>
                <div className={styles.PaaOsio} >
                    <div className={styles.OtsikkoRivi}>
                        <div className={styles.Otsikko}>
                            <h3>
                                {i18n.translate('RYHMAN_TIEDOT_OTSIKKO')}
                            </h3>
                        </div>
                    </div>
                    <div className={styles.OidRivi}>
                        <span className={styles.AvainKevyestiBoldattu}> OID</span>
                        <span className={styles.ReadOnly}>{ryhma.oid || ''}</span>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('RYHMAN_NIMI')}</label>
                            <div className={styles.Rivi}>
                                <label>{i18n.translate('SUOMEKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input value={ryhma.nimi['fi'] || ''}/>
                                </div>
                            </div>
                            <div className={styles.Rivi}>
                                <label>{i18n.translate('RUOTSIKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input value={ryhma.nimi['sv'] || ''}/>

                                </div>
                            </div>
                            <div className={styles.Rivi}>
                                <label>{i18n.translate('ENGLANNIKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input value={ryhma.nimi['en'] || ''}/>

                                </div>
                            </div>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('RYHMAN_KUVAUS')}</label>
                            <div className={styles.Rivi}>
                                <label>{i18n.translate('SUOMEKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input value={ryhma.kuvaus2['kieli_fi#1'] || ''}/>
                                </div>
                            </div>
                            <div className={styles.Rivi}>
                                <label>{i18n.translate('RUOTSIKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input value={ryhma.kuvaus2['kieli_sv#1'] || ''}/>
                                </div>
                            </div>
                            <div className={styles.Rivi}>
                                <label>{i18n.translate('ENGLANNIKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input value={ryhma.kuvaus2['kieli_en#1'] || ''}/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('RYHMAN_TYYPPI')}</label>
                            <Select
                                isMulti
                                value={ryhmaTyypit}
                                options={ryhmaTyypitOptions}/>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('RYHMAN_KAYTTOTARKOITUS')}</label>
                            <Select
                                isMulti
                                value={kayttoRyhmat}
                                options={kayttoRyhmatOptions}/>
                        </div>
                    </div>
                    <div className={styles.AlinRivi}>
                        <Button variant="outlined">{i18n.translate('PASSIVOI_RYHMA')}</Button>
                        <Button variant="outlined">{i18n.translate('POISTA_RYHMA')}</Button>
                    </div>
                </div>
            </div>
            <div className={styles.AlaBanneri}>
                <div className={styles.VersioContainer}>
                    <Button variant="outlined" className={styles.Versionappula}>
                        <span className="material-icons">timeline</span>
                        <span className={styles.VersionappulanTeksti}>{i18n.translate('VERSIOHISTORIA')}</span>
                    </Button>
                    <div className={styles.MuokattuKolumni}>
                        <span>{i18n.translate('MUOKATTU_VIIMEKSI')}</span>
                        <span>01.01.2020 16:39 Ingo Schimpff</span>
                    </div>
                </div>
                <div>
                    <Button variant="outlined" className={styles.Versionappula}>{i18n.translate('SULJE_TIEDOT')}
                    </Button>
                    <Button className={styles.Versionappula}>{i18n.translate('TALLENNA')}
                    </Button>
                </div>
            </div>
        </PohjaSivu>
    );
}