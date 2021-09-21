import * as React from 'react';
import { useContext, useState } from 'react';
import styles from './YhteystietotyypinMuokkaus.module.css';
import { LanguageContext } from '../../../../contexts/contexts';
import PohjaModaali from '../../../Modaalit/PohjaModaali/PohjaModaali';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import PohjaSivu from '../../PohjaSivu/PohjaSivu';
import Icon from '@iconify/react';
import homeIcon from '@iconify/icons-fa-solid/home';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { Koodi } from '../../../../types/types';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import UOTHeader from '../../../Modaalit/UusiOsoiteTyyppi/UOTHeader';
import UOTBody from '../../../Modaalit/UusiOsoiteTyyppi/OUTBody';
import UOTFooter from '../../../Modaalit/UusiOsoiteTyyppi/UOTFooter';
import useKoodisto from '../../../../api/koodisto';

const KAIKKIVALITTU = '1';

export default function YhteystietotyypinMuokkaus() {
    const { i18n, language } = useContext(LanguageContext);
    const { data: oppilaitosTyypit, loading: oppilaitosTyypitLoading, error: oppilaitosTyypitError } = useKoodisto(
        'OPPILAITOSTYYPPI',
        true
    );
    const [isKaikkiValittu, setIsKaikkiValittu] = useState(KAIKKIVALITTU);
    const [isModaliAuki, setIsModaaliAuki] = useState(false);

    if (oppilaitosTyypitLoading || oppilaitosTyypitError) {
        return <Spin />;
    }

    const oppilaitosTyypitOptions = oppilaitosTyypit.map((k: Koodi) => ({
        value: k.uri,
        label: k.nimi[language] || k.nimi['fi'] || k.nimi['sv'] || k.nimi['en'] || '',
    }));
    return (
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <a href="/organisaatio/yhteystietotyypit">
                        <Icon icon={homeIcon} />
                    </a>
                </div>
                <div>
                    <a href="/organisaatio/yhteystietotyypit">{i18n.translate('TYYPIT_KAIKKI_YHTEYSTIETOTYYPIT')}</a>
                </div>
            </div>
            <div className={styles.PaaKehys}>
                <div className={styles.ValiContainer}>
                    <div className={styles.ValiOtsikko}>
                        <h3>{i18n.translate('TYYPIT_YHTEYSTIETOTYYPPI')}</h3>
                        <h1>{i18n.translate('TYYPIT_UUDEN_YHTEYSTIETOTYYPIN_LISAAMINEN')}</h1>
                    </div>
                </div>
                <div className={styles.PaaOsio}>
                    <div className={styles.OtsikkoRivi}>
                        <div className={styles.Otsikko}>
                            <h3>{i18n.translate('TYYPIT_YHTEYSTIETOTYYPPI_TIEDOT_OTSIKKO')}</h3>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('TYYPIT_YHTEYSTIETOTYYPPI_NIMI')}</label>
                            <div className={styles.Rivi}>
                                <label>{i18n.translate('LABEL_SUOMEKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input />
                                </div>
                            </div>
                            <div className={styles.Rivi}>
                                <label>{i18n.translate('LABEL_RUOTSIKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input />
                                </div>
                            </div>
                            <div className={styles.Rivi}>
                                <label>{i18n.translate('LABEL_ENGLANNIKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input />
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('TYYPIT_TARVITTAVAT_NIMITIEDOT')}</label>
                            <div className={styles.Rivi}>
                                <label className={styles.FixedWidthLabel}>
                                    {i18n.translate('TYYPIT_NIMITIEDOT_NIMI')}
                                </label>
                                <Checkbox>{i18n.translate('LABEL_KAYTOSSA')}</Checkbox>
                                <Checkbox>{i18n.translate('LABEL_PAKOLLINEN')}</Checkbox>
                            </div>
                            <div className={styles.Rivi}>
                                <label className={styles.FixedWidthLabel}>
                                    {i18n.translate('TYYPIT_NIMITIEDOT_NIMIKE')}
                                </label>
                                <Checkbox>{i18n.translate('LABEL_KAYTOSSA')}</Checkbox>
                                <Checkbox>{i18n.translate('LABEL_PAKOLLINEN')}</Checkbox>
                            </div>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('TYYPIT_TARVITTAVAT_OSOITTEET')}</label>
                            <div className={styles.Rivi}>
                                <label className={styles.FixedWidthLabel}>
                                    {i18n.translate('TYYPIT_OSOITTEET_KAYNTIOSOITE')}
                                </label>
                                <Checkbox>{i18n.translate('LABEL_KAYTOSSA')}</Checkbox>
                                <Checkbox>{i18n.translate('LABEL_PAKOLLINEN')}</Checkbox>
                            </div>
                            <div className={styles.Rivi}>
                                <label className={styles.FixedWidthLabel}>
                                    {i18n.translate('TYYPIT_OSOITTEET_POSTIOSOITE')}
                                </label>
                                <Checkbox>{i18n.translate('LABEL_KAYTOSSA')}</Checkbox>
                                <Checkbox>{i18n.translate('LABEL_PAKOLLINEN')}</Checkbox>
                            </div>
                            <div className={styles.Rivi}>
                                <label className={styles.FixedWidthLabel}>
                                    {i18n.translate('TYYPIT_OSOITTEET_ULKOMAAN_OSOITE')}
                                </label>
                                <Checkbox>{i18n.translate('LABEL_KAYTOSSA')}</Checkbox>
                                <Checkbox>{i18n.translate('LABEL_PAKOLLINEN')}</Checkbox>
                            </div>
                            <div className={styles.Rivi}>
                                <label className={styles.FixedWidthLabel} />
                                <Button variant="outlined" onClick={() => setIsModaaliAuki(true)}>
                                    + {i18n.translate('TYYPIT_OSOITTEET_LISAA_UUSI')}
                                </Button>
                            </div>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('TYYPIT_TARVITTAVAT_SAHKOISET_YHTEYSTIEDOT')}</label>
                            <div className={styles.Rivi}>
                                <label className={styles.FixedWidthLabel}>
                                    {i18n.translate('TYYPIT_SAHKOISET_SAHKOPOSTIOSOITE')}
                                </label>
                                <Checkbox>{i18n.translate('LABEL_KAYTOSSA')}</Checkbox>
                                <Checkbox>{i18n.translate('LABEL_PAKOLLINEN')}</Checkbox>
                            </div>
                            <div className={styles.Rivi}>
                                <label className={styles.FixedWidthLabel}>
                                    {i18n.translate('TYYPIT_SAHKOISET_WWW_OSOITE')}
                                </label>
                                <Checkbox>{i18n.translate('LABEL_KAYTOSSA')}</Checkbox>
                                <Checkbox>{i18n.translate('LABEL_PAKOLLINEN')}</Checkbox>
                            </div>
                            <div className={styles.Rivi}>
                                <label className={styles.FixedWidthLabel} />
                                <Button variant="outlined">+ {i18n.translate('TYYPIT_SAHKOISET_LISAA_UUSI')}</Button>
                            </div>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <div>{i18n.translate('YHTEYSTIETOTYYPPIA_KAYTTAVAT')}</div>
                            <div className={styles.OrganisaatioMonivalinta}>
                                <label>{i18n.translate('LABEL_ORGANISAATIOT')}</label>
                                <Select isMulti value={[]} options={oppilaitosTyypitOptions} />
                            </div>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <div>{i18n.translate('TYYPIT_OPPILAITOSTYYPIT')}</div>
                            <div className={styles.MonivalintaKentta}>
                                <RadioGroup
                                    value={isKaikkiValittu}
                                    options={[
                                        { value: '1', label: i18n.translate('TYYPIT_KAIKKI') },
                                        { value: '0', label: i18n.translate('TYYPIT_RAJATUT_OPPILAITOSTYYPIT') },
                                    ]}
                                    onChange={(e) => setIsKaikkiValittu(e.target.value)}
                                />

                                <Select isMulti value={[]} options={oppilaitosTyypitOptions} />
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
                        <span>01.01.2020 16:39 Ingo Schimpff</span>
                    </div>
                </div>
                <div>
                    <Button variant="outlined" className={styles.Versionappula}>
                        {i18n.translate('BUTTON_SULJE')}
                    </Button>
                    <Button className={styles.Versionappula}>{i18n.translate('BUTTON_TALLENNA')}</Button>
                </div>
            </div>
            {isModaliAuki && (
                <PohjaModaali
                    header={<UOTHeader />}
                    body={<UOTBody />}
                    footer={<UOTFooter />}
                    suljeCallback={() => setIsModaaliAuki(false)}
                />
            )}
        </PohjaSivu>
    );
}
