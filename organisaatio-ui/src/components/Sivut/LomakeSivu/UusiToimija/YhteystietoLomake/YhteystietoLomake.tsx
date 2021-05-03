import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import { SyntheticEvent, useState } from 'react';
import { Koodi, Osoite, Yhteystiedot, YhteystiedotOsoite } from '../../../../../types/types';
import useAxios from 'axios-hooks';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

type Props = {
    yhteystiedot: Partial<Yhteystiedot>[];
    handleOnChange: ({ name, value }: { name: string; value: any }) => void;
};

type SupportedOsoiteType = 'kaynti' | 'posti';

const DEFAULT_LANGUAGE_CODE = 'kieli_fi#1';
const NAME_WWW = 'www';
const NAME_EMAIL = 'email';
const NAME_PHONE = 'numero';

const isOsoite = (yhteystieto: Partial<Yhteystiedot>): yhteystieto is YhteystiedotOsoite =>
    yhteystieto.hasOwnProperty('osoiteTyyppi');

const getOsoite = (
    yhteystiedot: Partial<Yhteystiedot>[],
    kieli: string,
    osoiteTyyppi: SupportedOsoiteType
): YhteystiedotOsoite | Record<string, string> => ({
    ...yhteystiedot.find(
        (yt: Partial<Yhteystiedot>) => isOsoite(yt) && yt.kieli === kieli && yt.osoiteTyyppi === osoiteTyyppi
    ),
});

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '/organisaatio';

export default function YhteystietoLomake(props: Props) {
    const [kieleksi, setKieleksi] = useState<string>(DEFAULT_LANGUAGE_CODE);
    const [postiSamakuinKaynti, setPostiSamakuinKaynti] = useState({ kieleksi: DEFAULT_LANGUAGE_CODE, onSama: false });
    const [{ data: postinumerot, loading: postinumerotLoading, error: postinumerotError }] = useAxios<Koodi[]>(
        `${urlPrefix}/koodisto/POSTI/koodi?onlyValid=true`
    );

    const { yhteystiedot, handleOnChange } = props;

    const currentVisibleYhteystiedot = {
        posti: { osoite: '', postinumeroUri: '' },
        kaynti: { osoite: '', postinumeroUri: '' },
        puhelin: { numero: '' },
        www: { www: '' },
        email: { email: '' },
    };
    yhteystiedot.forEach((yT: any) => {
        if (yT.kieli === kieleksi) {
            if (yT.osoiteTyyppi && yT.osoiteTyyppi === 'posti') {
                currentVisibleYhteystiedot.posti = yT;
            } else if (yT.osoiteTyyppi && yT.osoiteTyyppi === 'kaynti') {
                currentVisibleYhteystiedot.kaynti = yT;
            } else if (yT.tyyppi && yT.tyyppi === 'puhelin') {
                currentVisibleYhteystiedot.puhelin = yT;
            } else if (yT.www) {
                currentVisibleYhteystiedot.www = yT;
            } else if (yT.email) {
                currentVisibleYhteystiedot.email = yT;
            }
        }
    });
    /*

    const handleOsoiteMuutos = (kentta: OsoiteKentta, muutos: Partial<Osoite>): void => {
        const yhteystiedot = organisaatio.yhteystiedot;
        yhteystiedot[kentta] = { ...yhteystiedot[kentta], ...muutos };
        if (kentta === 'postiosoite' && kayntiosoiteSamaKuinPostiosoite) {
            yhteystiedot.kayntiosoite = { ...yhteystiedot.postiosoite };
        }
        setOrganisaatio({ yhteystiedot });
    };

     */

    const handlePostiOsSamaKuinKaynti = (event: SyntheticEvent) => {
        const element = event.target as HTMLInputElement;
        const postiYt = getOsoite(yhteystiedot, kieleksi, 'posti');
        const kayntiYt = getOsoite(yhteystiedot, kieleksi, 'kaynti');
        if (element.checked) {
            kayntiYt.osoite = postiYt.osoite;
            kayntiYt.postinumeroUri = postiYt.postinumeroUri;
            handleOnChange({ name: 'yhteystiedot', value: yhteystiedot });
        }
        setPostiSamakuinKaynti({ kieleksi: kieleksi, onSama: !postiSamakuinKaynti.onSama });
    };
    const handleYhteystietoOnChange = (event: SyntheticEvent) => {
        const element = event.target as HTMLInputElement;
        const name = element.name;
        const oikeankieliset = yhteystiedot.filter((yt: Partial<Yhteystiedot>) => yt.kieli === kieleksi);
        if (oikeankieliset.length > 0) {
            if (name === NAME_WWW || name === NAME_EMAIL || name === NAME_PHONE) {
                const oikea = oikeankieliset.find((yt) => yt.hasOwnProperty(name));
                if (oikea) {
                    oikea[name] = element.value;
                } else yhteystiedot.push({ kieli: kieleksi, [name]: element.value });
            } else {
                const [osoiteTyyppi, attribute] = [...name.split('.')] as [SupportedOsoiteType, keyof Osoite];

                const osoitteet = oikeankieliset.filter((yt: Partial<Yhteystiedot>) =>
                    isOsoite(yt)
                ) as YhteystiedotOsoite[];
                const osoite = osoitteet.find((yt) => yt.osoiteTyyppi === osoiteTyyppi);

                if (osoite) {
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
        }
        handleOnChange({ name: 'yhteystiedot', value: yhteystiedot });
    };
    if (postinumerotLoading || postinumerotError) {
        return <Spin />;
    }
    console.log('todo postinumerot', postinumerot, currentVisibleYhteystiedot, yhteystiedot);
    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <RadioGroup
                        value={kieleksi}
                        options={[
                            { value: 'kieli_fi#1', label: 'Suomeksi' },
                            { value: 'kieli_sv#1', label: 'Ruotsiksi' },
                            { value: 'kieli_en#1', label: 'Englanniksi' },
                        ]}
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
                        value={currentVisibleYhteystiedot.posti && currentVisibleYhteystiedot.posti.osoite}
                    />
                </div>
                <div className={styles.KenttaLyhyt}>
                    <label>Postinumero</label>
                    <Input
                        name="posti.postinumeroUri"
                        onChange={handleYhteystietoOnChange}
                        value={currentVisibleYhteystiedot.posti && currentVisibleYhteystiedot.posti.postinumeroUri}
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
                        value={currentVisibleYhteystiedot.kaynti && currentVisibleYhteystiedot.kaynti.osoite}
                    />
                </div>
                <div className={styles.KenttaLyhyt}>
                    <label>Postinumero</label>
                    <Input
                        disabled={kieleksi === postiSamakuinKaynti.kieleksi && postiSamakuinKaynti.onSama}
                        onChange={handleYhteystietoOnChange}
                        value={currentVisibleYhteystiedot.kaynti && currentVisibleYhteystiedot.kaynti.postinumeroUri}
                        name="kaynti.postinumeroUri"
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Puhelinnumero</label>
                    <Input
                        value={currentVisibleYhteystiedot.puhelin && currentVisibleYhteystiedot.puhelin.numero}
                        name={NAME_PHONE}
                        onChange={handleYhteystietoOnChange}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Sähköpostiosoite</label>
                    <Input
                        value={currentVisibleYhteystiedot.email && currentVisibleYhteystiedot.email.email}
                        name={NAME_EMAIL}
                        onChange={handleYhteystietoOnChange}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Www-osoite</label>
                    <Input
                        value={currentVisibleYhteystiedot.www && currentVisibleYhteystiedot.www.www}
                        name={NAME_WWW}
                        onChange={handleYhteystietoOnChange}
                    />
                </div>
            </div>
        </div>
    );
}
