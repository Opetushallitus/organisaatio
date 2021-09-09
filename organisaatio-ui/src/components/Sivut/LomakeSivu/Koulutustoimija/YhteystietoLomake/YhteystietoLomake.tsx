import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import { useState, SyntheticEvent, useContext } from 'react';
import type { Organisaatio, Osoite, Yhteystiedot, YhteystiedotOsoite } from '../../../../../types/types';
import { LanguageContext } from '../../../../../contexts/contexts';
import { KoodiUri, Nimi } from '../../../../../types/types';

export type Props = {
    yhteystiedot: Yhteystiedot[];
    handleOnChange: ({
        name,
        value,
    }: {
        name: keyof Organisaatio;
        value: { nimi: Nimi; alkuPvm: string }[] | Nimi | KoodiUri[] | Date | KoodiUri | Yhteystiedot[];
    }) => void;
};

type SupportedOsoiteType = 'kaynti' | 'posti';
type SupportedYhteystietoType = 'www' | 'email' | 'numero';

const DEFAULT_LANGUAGE_CODE = 'kieli_fi#1';
const NAME_WWW = 'www';
const NAME_EMAIL = 'email';
const NAME_PHONE = 'numero';

// this should be defined in some common place relating to localization
const SUPPORTED_LANGUAGES = [
    { value: DEFAULT_LANGUAGE_CODE, label: 'Suomeksi' },
    { value: 'kieli_sv#1', label: 'Ruotsiksi' },
    { value: 'kieli_en#1', label: 'Englanniksi' },
];

const initializeOsoite = (kieli: string, osoiteTyyppi: SupportedOsoiteType): YhteystiedotOsoite => ({
    kieli,
    osoiteTyyppi,
    postinumeroUri: '',
    postitoimipaikka: '',
    osoite: '',
});

const isOsoite = (yhteystieto: Yhteystiedot): yhteystieto is YhteystiedotOsoite =>
    yhteystieto.hasOwnProperty('osoiteTyyppi');

const isSupportedYhteystietoType = (name: string): name is SupportedYhteystietoType =>
    [NAME_EMAIL, NAME_PHONE, NAME_WWW].includes(name);

export const getOsoite = (
    yhteystiedot: Yhteystiedot[],
    kieli: string,
    osoiteTyyppi: SupportedOsoiteType
): YhteystiedotOsoite => {
    const found = yhteystiedot.find(
        (yhteystieto: Yhteystiedot) =>
            isOsoite(yhteystieto) && yhteystieto.kieli === kieli && yhteystieto.osoiteTyyppi === osoiteTyyppi
    );
    if (found) {
        return found as YhteystiedotOsoite;
    }
    yhteystiedot.push(initializeOsoite(kieli, osoiteTyyppi));
    return getOsoite(yhteystiedot, kieli, osoiteTyyppi);
};

export const getYhteystieto = (
    yhteystiedot: Yhteystiedot[],
    kieli: string,
    osoiteTyyppi: SupportedYhteystietoType
): Yhteystiedot => {
    const found = yhteystiedot.find(
        (yhteystieto: Yhteystiedot) => yhteystieto.kieli === kieli && yhteystieto.hasOwnProperty(osoiteTyyppi)
    );
    if (found) {
        return found as Yhteystiedot;
    }
    yhteystiedot.push({ kieli, [osoiteTyyppi]: '' } as Yhteystiedot);
    return getYhteystieto(yhteystiedot, kieli, osoiteTyyppi);
};

const YhteystietoLomake = ({ yhteystiedot, handleOnChange }: Props): React.ReactElement => {
    const { i18n } = useContext(LanguageContext);
    const [kieleksi, setKieleksi] = useState<string>(DEFAULT_LANGUAGE_CODE);
    const [postiSamakuinKaynti, setPostiSamakuinKaynti] = useState({ kieleksi: DEFAULT_LANGUAGE_CODE, onSama: false });

    const handlePostiOsSamaKuinKaynti = (event: SyntheticEvent) => {
        const element = event.target as HTMLInputElement;
        setPostiSamakuinKaynti({ kieleksi: kieleksi, onSama: element.checked });
        copyAddress(element.checked);
    };

    const copyAddress = (copy: boolean) => {
        if (copy) {
            const postiYt = getOsoite(yhteystiedot, kieleksi, 'posti');
            const kayntiYt = getOsoite(yhteystiedot, kieleksi, 'kaynti');
            kayntiYt.osoite = postiYt.osoite;
            kayntiYt.postinumeroUri = postiYt.postinumeroUri;
        }
        handleOnChange({ name: 'yhteystiedot', value: yhteystiedot });
    };

    const handleYhteystietoOnChange = (event: SyntheticEvent) => {
        const element = event.target as HTMLInputElement;
        yhteystietoOnChange(element.name, element.value);
    };

    const yhteystietoOnChange = (name: string, value: string) => {
        isSupportedYhteystietoType(name) ? updateField(name, value) : updateAddress(name, value);
    };

    const updateField = (name: SupportedYhteystietoType, value: string) => {
        getYhteystieto(yhteystiedot, kieleksi, name)[name] = value;
        handleOnChange({ name: 'yhteystiedot', value: yhteystiedot });
    };

    const updateAddress = (name: string, value: string) => {
        const [osoiteTyyppi, attribute] = [...name.split('.')] as [SupportedOsoiteType, keyof Osoite];
        getOsoite(yhteystiedot, kieleksi, osoiteTyyppi)[attribute] = value;
        copyAddress(postiSamakuinKaynti.onSama);
    };

    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <RadioGroup
                        value={kieleksi}
                        options={SUPPORTED_LANGUAGES}
                        onChange={(e) => setKieleksi(e.target.value)}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <Checkbox
                    id="checkbox"
                    checked={kieleksi === postiSamakuinKaynti.kieleksi && postiSamakuinKaynti.onSama}
                    onChange={handlePostiOsSamaKuinKaynti}
                >
                    {i18n.translate('YHTEYSTIEDOT_POSTIOSOITE_ON_SAMA_KUIN_KAYNTIOSOITE')}
                </Checkbox>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHTEYSTIEDOT_POSTIOSOITE')}</label>
                    <Input
                        name="posti.osoite"
                        onChange={handleYhteystietoOnChange}
                        value={getOsoite(yhteystiedot, kieleksi, 'posti').osoite}
                    />
                </div>
                <div className={styles.KenttaLyhyt}>
                    <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO')}</label>
                    <Input
                        name="posti.postinumeroUri"
                        onChange={handleYhteystietoOnChange}
                        value={getOsoite(yhteystiedot, kieleksi, 'posti').postinumeroUri}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHTEYSTIEDOT_KAYNTIOSOITE')}</label>
                    <Input
                        disabled={kieleksi === postiSamakuinKaynti.kieleksi && postiSamakuinKaynti.onSama}
                        name="kaynti.osoite"
                        onChange={handleYhteystietoOnChange}
                        value={getOsoite(yhteystiedot, kieleksi, 'kaynti').osoite}
                    />
                </div>
                <div className={styles.KenttaLyhyt}>
                    <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO')}</label>
                    <Input
                        disabled={kieleksi === postiSamakuinKaynti.kieleksi && postiSamakuinKaynti.onSama}
                        onChange={handleYhteystietoOnChange}
                        value={getOsoite(yhteystiedot, kieleksi, 'kaynti').postinumeroUri}
                        name="kaynti.postinumeroUri"
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHTEYSTIEDOT_PUHELINNUMERO')}</label>
                    <Input
                        value={getYhteystieto(yhteystiedot, kieleksi, NAME_PHONE)[NAME_PHONE]}
                        name={NAME_PHONE}
                        onChange={handleYhteystietoOnChange}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHTEYSTIEDOT_SAHKOPOSTIOSOITE')}</label>
                    <Input
                        value={getYhteystieto(yhteystiedot, kieleksi, NAME_EMAIL)[NAME_EMAIL]}
                        name={NAME_EMAIL}
                        onChange={handleYhteystietoOnChange}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHTEYSTIEDOT_WWW_OSOITE')}</label>
                    <Input
                        value={getYhteystieto(yhteystiedot, kieleksi, NAME_WWW)[NAME_WWW]}
                        name={NAME_WWW}
                        onChange={handleYhteystietoOnChange}
                    />
                </div>
            </div>
        </div>
    );
};

export default YhteystietoLomake;
