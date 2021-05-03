import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import { useState } from 'react';
import type { Organisaatio, Koodi, Osoite, Yhteystiedot, YhteystiedotOsoite } from '../../../../../types/types';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { SyntheticEvent } from 'react';

export type Props = {
    yhteystiedot: Yhteystiedot[];
    handleOnChange: ({ name, value }: { name: keyof Organisaatio; value: any }) => void;
    postinumerot: Koodi[];
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

const getOsoite = (
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

const getYhteystieto = (
    yhteystiedot: Yhteystiedot[],
    kieli: string,
    tyyppi: SupportedYhteystietoType
): Yhteystiedot | Record<string, string> => ({
    ...yhteystiedot.find((yhteystieto: Yhteystiedot) => yhteystieto.kieli === kieli && !!yhteystieto[tyyppi]),
});

const YhteystietoLomake = ({ yhteystiedot, handleOnChange, postinumerot }: Props): React.ReactElement => {
    const [kieleksi, setKieleksi] = useState<string>(DEFAULT_LANGUAGE_CODE);
    const [postiSamakuinKaynti, setPostiSamakuinKaynti] = useState({ kieleksi: DEFAULT_LANGUAGE_CODE, onSama: false });

    const handlePostiOsSamaKuinKaynti = (event: SyntheticEvent) => {
        if ((event.target as HTMLInputElement).checked) {
            const postiYt = getOsoite(yhteystiedot, kieleksi, 'posti');
            const kayntiYt = getOsoite(yhteystiedot, kieleksi, 'kaynti');
            kayntiYt.osoite = postiYt.osoite;
            kayntiYt.postinumeroUri = postiYt.postinumeroUri;
            handleOnChange({ name: 'yhteystiedot', value: yhteystiedot });
        }
        setPostiSamakuinKaynti({ kieleksi: kieleksi, onSama: !postiSamakuinKaynti.onSama });
    };

    const handleYhteystietoOnChange = (event: SyntheticEvent) => {
        const element = event.target as HTMLInputElement;
        const name = element.name;
        const oikeankieliset = yhteystiedot.filter((yt: Yhteystiedot) => yt.kieli === kieleksi);
        if (oikeankieliset.length > 0) {
            if (name === NAME_WWW || name === NAME_EMAIL || name === NAME_PHONE) {
                const oikea = oikeankieliset.find((yt) => yt.hasOwnProperty(name));
                if (oikea) {
                    oikea[name] = element.value;
                } else yhteystiedot.push({ kieli: kieleksi, [name]: element.value } as Yhteystiedot);
            } else {
                const [osoiteTyyppi, attribute] = [...name.split('.')] as [SupportedOsoiteType, keyof Osoite];
                const osoite = getOsoite(yhteystiedot, kieleksi, osoiteTyyppi);

                osoite[attribute] = element.value;
                if (
                    osoite.osoiteTyyppi === 'posti' &&
                    kieleksi === postiSamakuinKaynti.kieleksi &&
                    postiSamakuinKaynti.onSama
                ) {
                    const kayntiYt = getOsoite(yhteystiedot, kieleksi, 'kaynti');
                    if (!!Object.keys(kayntiYt).length) {
                        kayntiYt.osoite = osoite.osoite;
                        kayntiYt.postinumeroUri = osoite.postinumeroUri;
                    }
                }
            }
        }
        handleOnChange({ name: 'yhteystiedot', value: yhteystiedot });
    };
    if (!postinumerot) {
        return <Spin />;
    }
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
                    checked={kieleksi === postiSamakuinKaynti.kieleksi && postiSamakuinKaynti.onSama}
                    onChange={handlePostiOsSamaKuinKaynti}
                >
                    Postiosoite on sama kuin käyntiosoite
                </Checkbox>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Postiosoite</label>
                    <Input
                        name="posti.osoite"
                        onChange={handleYhteystietoOnChange}
                        value={getOsoite(yhteystiedot, kieleksi, 'posti').osoite}
                    />
                </div>
                <div className={styles.KenttaLyhyt}>
                    <label>Postinumero</label>
                    <Input
                        name="posti.postinumeroUri"
                        onChange={handleYhteystietoOnChange}
                        value={getOsoite(yhteystiedot, kieleksi, 'posti').postinumeroUri}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Käyntiosoite</label>
                    <Input
                        disabled={kieleksi === postiSamakuinKaynti.kieleksi && postiSamakuinKaynti.onSama}
                        name="kaynti.osoite"
                        onChange={handleYhteystietoOnChange}
                        value={getOsoite(yhteystiedot, kieleksi, 'kaynti').osoite}
                    />
                </div>
                <div className={styles.KenttaLyhyt}>
                    <label>Postinumero</label>
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
                    <label>Puhelinnumero</label>
                    <Input
                        value={getYhteystieto(yhteystiedot, kieleksi, NAME_PHONE)[NAME_PHONE] || ''}
                        name={NAME_PHONE}
                        onChange={handleYhteystietoOnChange}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Sähköpostiosoite</label>
                    <Input
                        value={getYhteystieto(yhteystiedot, kieleksi, NAME_EMAIL)[NAME_EMAIL] || ''}
                        name={NAME_EMAIL}
                        onChange={handleYhteystietoOnChange}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Www-osoite</label>
                    <Input
                        value={getYhteystieto(yhteystiedot, kieleksi, NAME_WWW)[NAME_WWW] || ''}
                        name={NAME_WWW}
                        onChange={handleYhteystietoOnChange}
                    />
                </div>
            </div>
        </div>
    );
};

export default YhteystietoLomake;
