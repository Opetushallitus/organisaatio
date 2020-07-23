import * as React from 'react';
import styles from './LisatietotyypinMuokkaus.module.css';
import {useContext} from "react";
import {LanguageContext} from "../../../../contexts/contexts";
import Input from "@opetushallitus/virkailija-ui-components/Input";
import PohjaSivu from "../../PohjaSivu/PohjaSivu";
import Icon from "@iconify/react";
import homeIcon from "@iconify/icons-fa-solid/home";
import Button from "@opetushallitus/virkailija-ui-components/Button";
import RadioGroup from "@opetushallitus/virkailija-ui-components/RadioGroup";
import {useState} from "react";
import Select from "@opetushallitus/virkailija-ui-components/Select";
import useAxios from "axios-hooks";
import {Koodi} from "../../../../types/types";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";

const KAIKKIVALITTU = '1', RAJATUT_VALITTU = '0';

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '';

type Props = {
}

export default function LisatietotyypinMuokkaus(props: Props) {
    const { i18n, language } = useContext(LanguageContext);
    const [{ data: oppilaitosTyypit, loading: oppilaitosTyypitLoading, error: oppilaitosTyypitError}] = useAxios<Koodi[]>(
        `${urlPrefix}/koodisto/OPPILAITOSTYYPPI/koodi?noCache=1595328878067&onlyValidKoodis=true`);
    const [isKaikkiValittu, setIsKaikkiValittu ] = useState(KAIKKIVALITTU);

    if (oppilaitosTyypitLoading || oppilaitosTyypitError) {
        return <Spin />;
    }

    const oppilaitosTyypitOptions = oppilaitosTyypit.map((k: any) => ({
        value: k.uri,
        label: k.nimi[language] || k.nimi['fi'] || k.nimi['sv'] || k.nimi['en'],
    }));
    console.log('otopts', oppilaitosTyypitOptions);
    return(
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <a href="/lisatietotyypit"><Icon icon={homeIcon} /></a>
                </div>
                <div>
                    <a href="/lisatietotyypit">{i18n.translate('KAIKKI_LISATIETOTYYPIT')}</a>
                </div>
            </div>
            <div className={styles.PaaKehys}>
                <div className={styles.ValiContainer}>
                    <div className={styles.ValiOtsikko}>
                        <h3>{i18n.translate('LISATIETOTYYPPI')}</h3>
                        <h1>{i18n.translate('UUDEN_LISATIETOTYYPIN_LISAAMINEN')}</h1>
                    </div>
                </div>
                <div className={styles.PaaOsio} >
                    <div className={styles.OtsikkoRivi}>
                        <div className={styles.Otsikko}>
                            <h3>
                                {i18n.translate('LISATIETOTYYPPI_LISAA_UUSI_OTSIKKO')}
                            </h3>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('LISATIETOTYYPPI_NIMI')}</label>
                            <Input value={''}/>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <div>{i18n.translate('SALLITUT_OPPILAITOSTYYPIT')}</div>
                            <div className={styles.MonivalintaKentta}>
                                <RadioGroup
                                    value={isKaikkiValittu}
                                    options={[
                                        { value: '1', label: i18n.translate('KAIKKI') },
                                        { value: '0', label: i18n.translate('RAJATUT_OPPILAITOSTYYPIT') },
                                    ]}
                                    onChange={e => setIsKaikkiValittu(e.target.value)}
                                />

                                <Select
                                    isMulti
                                    value={[]}
                                    options={oppilaitosTyypitOptions}/>
                            </div>
                        </div>
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
                        <span>01.01.2020 16:39 ingo Schimpff</span>
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