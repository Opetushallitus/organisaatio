import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from "@opetushallitus/virkailija-ui-components/Input";
import Checkbox from "@opetushallitus/virkailija-ui-components/Checkbox";
import RadioGroup from "@opetushallitus/virkailija-ui-components/RadioGroup";
import {useState} from "react";
import {Koodi, Osoite, Yhteystiedot} from "../../../../../types/types";
import useAxios from "axios-hooks";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";
import {SyntheticEvent} from "react";

type yhteystietoProps = {
    yhteystiedot: Yhteystiedot[]
    handleOnChange: ({ name, value }: { name: string; value: any; }) => void
    postinumerot: Koodi[]
}
const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '/organisaatio';

export default function YhteystietoLomake(props: yhteystietoProps) {
    const [kieleksi, setKieleksi ] = useState('kieli_fi#1');
    const [postiSamakuinKaynti, setPostiSamakuinKaynti] = useState({ kieleksi, onSama: false });

    const { yhteystiedot, handleOnChange, postinumerot } = props;

    const currentVisibleYhteystiedot = { posti: { osoite: '', postinumeroUri: ''}, kaynti: { osoite: '', postinumeroUri: ''}, puhelin: { numero: ''}, www: { www: ''}, email: { email: ''}};
    yhteystiedot.forEach((yT: any) => {
        if (yT.kieli === kieleksi) {
            if (yT.osoiteTyyppi && yT.osoiteTyyppi=== 'posti') {
                currentVisibleYhteystiedot.posti = yT;
            } else if (yT.osoiteTyyppi && yT.osoiteTyyppi=== 'kaynti') {
                currentVisibleYhteystiedot.kaynti = yT;
            } else if (yT.tyyppi && yT.tyyppi=== 'puhelin') {
                currentVisibleYhteystiedot.puhelin = yT;
            } else if (yT.www) {
                currentVisibleYhteystiedot.www = yT;
            }  else if (yT.email) {
                currentVisibleYhteystiedot.email = yT;
            }
        }
    });
    const handlePostiOsSamaKuinKaynti = (event: SyntheticEvent) => {
        const element = event.target as HTMLInputElement;
        const postiYt = Object.assign({}, yhteystiedot.find((yt: Yhteystiedot) => yt.kieli === kieleksi && yt.osoiteTyyppi === 'posti'));
        const kayntiYt = yhteystiedot.find((yt: Yhteystiedot) => yt.kieli === kieleksi && yt.osoiteTyyppi === 'kaynti');
        if (postiYt && kayntiYt && element.checked) {
            kayntiYt.osoite = postiYt.osoite;
            kayntiYt.postinumeroUri = postiYt.postinumeroUri;
            handleOnChange({ name: 'yhteystiedot', value: yhteystiedot});
        }
        setPostiSamakuinKaynti({kieleksi: kieleksi, onSama: !postiSamakuinKaynti.onSama})
    }
    const handleYhteystietoOnChange = (event: SyntheticEvent) => {
        const element = event.target as HTMLInputElement;
        const name = element.name;
        const oikeankieliset = yhteystiedot.filter((yt: Yhteystiedot) => yt.kieli === kieleksi);
        if( oikeankieliset.length > 0) {
            if (name === 'www' || name === 'email' || name === 'numero') {
                const oikea = oikeankieliset.find(yt => yt.hasOwnProperty(name));
                if(oikea) oikea[name] = element.value;
                else yhteystiedot.push({ kieli: kieleksi, [name]: element.value, })
            } else {
                const jaettu = name.split('.');
                const pohja = jaettu[0] as keyof Yhteystiedot;
                const avain = jaettu[1] as keyof Osoite;
                const oikea = oikeankieliset.find(yt => yt.osoiteTyyppi && yt.osoiteTyyppi === pohja);
                if(oikea) {
                    oikea[avain] = element.value;
                    if (oikea.osoiteTyyppi === 'posti' && kieleksi === postiSamakuinKaynti.kieleksi && postiSamakuinKaynti.onSama) {
                        const kayntiYt = yhteystiedot.find((yt: Yhteystiedot) => yt.kieli === kieleksi && yt.osoiteTyyppi === 'kaynti');
                        if (kayntiYt) {
                            kayntiYt.osoite = oikea.osoite;
                            kayntiYt.postinumeroUri = oikea.postinumeroUri;
                        }
                    }
                }
            }
        }
        handleOnChange({ name: 'yhteystiedot', value: yhteystiedot});
    };
    if (!postinumerot) {
       return <Spin />;
    }
    return(
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
                        onChange={e => setKieleksi(e.target.value)}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <Checkbox
                    checked={kieleksi === postiSamakuinKaynti.kieleksi && postiSamakuinKaynti.onSama}
                    onChange={handlePostiOsSamaKuinKaynti}
                >Postiosoite on sama kuin käyntiosoite</Checkbox>
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
                        name="numero"
                        onChange={handleYhteystietoOnChange}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Sähköpostiosoite</label>
                    <Input
                        value={currentVisibleYhteystiedot.email && currentVisibleYhteystiedot.email.email}
                        name="email"
                        onChange={handleYhteystietoOnChange}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Www-osoite</label>
                    <Input
                        value={currentVisibleYhteystiedot.www && currentVisibleYhteystiedot.www.www}
                        name="www"
                        onChange={handleYhteystietoOnChange}
                    />
                </div>
            </div>
        </div>
    );
}