import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import styles from './LisatietotyypinMuokkaus.module.css';
import { LanguageContext } from '../../../../contexts/contexts';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import PohjaSivu from '../../PohjaSivu/PohjaSivu';
import Icon from '@iconify/react';
import homeIcon from '@iconify/icons-fa-solid/home';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import Axios from 'axios';
import useKoodisto from '../../../../api/useKoodisto';

const KAIKKIVALITTU = '1',
    RAJATUT_VALITTU = '0';

type Props = {
    match: { params: { nimi: string } };
};

export default function LisatietotyypinMuokkaus(props: Props) {
    const { i18n, language } = useContext(LanguageContext);
    const [lisatietotyyppi, setLisatietotyyppi] = useState<undefined | any>();
    const { data: oppilaitosTyypit, loading: oppilaitosTyypitLoading, error: oppilaitosTyypitError } = useKoodisto(
        'OPPILAITOSTYYPPI',
        true
    );
    const {
        match: { params },
    } = props;
    useEffect(() => {
        async function fetch() {
            try {
                const response = await Axios.get(`/organisaatio/lisatieto/lisatietotyyppi/${params.nimi}`);
                const lisatietotyyppi = response.data;
                setLisatietotyyppi(lisatietotyyppi);
            } catch (error) {
                console.error('error fetching', error);
            }
        }
        fetch();
    }, [params.nimi]);
    const [isKaikkiValittu, setIsKaikkiValittu] = useState(KAIKKIVALITTU);

    if (oppilaitosTyypitLoading || oppilaitosTyypitError) {
        return <Spin />;
    }

    const oppilaitosTyypitOptions = oppilaitosTyypit.map((k) => ({
        value: k.uri,
        label: k.nimi[language] || k.nimi['fi'] || k.nimi['sv'] || k.nimi['en'] || '',
    }));
    const valitutRajoitteet = lisatietotyyppi.rajoitteet
        .filter((r: any) => r.rajoitetyyppi === 'OPPILAITOSTYYPPI')
        .map((r: any) => {
            return oppilaitosTyypitOptions.find((oT) => oT.value === r.arvo);
        });
    if (valitutRajoitteet.length > 0 && isKaikkiValittu === KAIKKIVALITTU) {
        setIsKaikkiValittu(RAJATUT_VALITTU);
    }

    console.log('otopts', oppilaitosTyypitOptions);
    return (
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <a href="/organisaatio/lisatietotyypit">
                        <Icon icon={homeIcon} />
                    </a>
                </div>
                <div>
                    <a href="/organisaatio/lisatietotyypit">{i18n.translate('TYYPIT_KAIKKI_LISATIETOTYYPIT')}</a>
                </div>
            </div>
            <div className={styles.PaaKehys}>
                <div className={styles.ValiContainer}>
                    <div className={styles.ValiOtsikko}>
                        <h3>{i18n.translate('TYYPIT_LISATIETOTYYPPI')}</h3>
                        <h1>{i18n.translate('TYYPIT_UUDEN_LISATIETOTYYPIN_LISAAMINEN')}</h1>
                    </div>
                </div>
                <div className={styles.PaaOsio}>
                    <div className={styles.OtsikkoRivi}>
                        <div className={styles.Otsikko}>
                            <h3>{i18n.translate('TYYPIT_LISATIETOTYYPPI_LISAA_UUSI_OTSIKKO')}</h3>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('TYYPIT_LISATIETOTYYPPI_NIMI')}</label>
                            <Input value={lisatietotyyppi ? lisatietotyyppi.nimi : ''} />
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <div>{i18n.translate('TYYPIT_SALLITUT_OPPILAITOSTYYPIT')}</div>
                            <div className={styles.MonivalintaKentta}>
                                <RadioGroup
                                    value={isKaikkiValittu}
                                    options={[
                                        { value: '1', label: i18n.translate('TYYPIT_KAIKKI') },
                                        { value: '0', label: i18n.translate('TYYPIT_RAJATUT_OPPILAITOSTYYPIT') },
                                    ]}
                                    onChange={(e) => setIsKaikkiValittu(e.target.value)}
                                />

                                <Select
                                    isMulti
                                    value={lisatietotyyppi.rajoitteet
                                        .filter((r: any) => r.rajoitetyyppi === 'OPPILAITOSTYYPPI')
                                        .map((r: any) => {
                                            return oppilaitosTyypitOptions.find((oT) => oT.value === r.arvo);
                                        })}
                                    options={oppilaitosTyypitOptions}
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div className={styles.AlaBanneri}>
                <div className={styles.VersioContainer}>
                    <Button variant="outlined" className={styles.Versionappula}>
                        <span className="material-icons">timeline</span>
                        <span className={styles.VersionappulanTeksti}>{i18n.translate('BUTTON_VERSIOHISTORIA')}</span>
                    </Button>
                    <div className={styles.MuokattuKolumni}>
                        <span>{i18n.translate('VERSIOHISTORIA_MUOKATTU_VIIMEKSI')}</span>
                        <span>01.01.2020 16:39 ingo Schimpff</span>
                    </div>
                </div>
                <div>
                    <Button variant="outlined" className={styles.Versionappula}>
                        {i18n.translate('BUTTON_SULJE')}
                    </Button>
                    <Button className={styles.Versionappula}>{i18n.translate('BUTTON_TALLENNA')}</Button>
                </div>
            </div>
        </PohjaSivu>
    );
}
