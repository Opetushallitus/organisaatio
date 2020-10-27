import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from "@opetushallitus/virkailija-ui-components/Input";
import Checkbox from "@opetushallitus/virkailija-ui-components/Checkbox";
import RadioGroup from "@opetushallitus/virkailija-ui-components/RadioGroup";
import {useState} from "react";
import {Koodi} from "../../../../../types/types";
import useAxios from "axios-hooks";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";

type yhteystietoProps = {
    yhteystiedot?: any
}
const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '/organisaatio';

export default function YhteystietoLomake(props: yhteystietoProps) {
    const [kieleksi, setKieleksi ] = useState('kieli_fi#1');
    const [{data: postinumerot, loading: postinumerotLoading, error: postinumerotError}]
        = useAxios<Koodi[]>(`${urlPrefix}/koodisto/POSTI/koodi?onlyValid=true`);

    const { yhteystiedot } = props;

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
    /*
    const handleKayntiosoiteSamaKuinPostiosoite = (value: boolean): void => {
        setKayntiosoiteSamaKuinPostiosoite(value);
        if (value) {
            const yhteystiedot = organisaatio.yhteystiedot;
            yhteystiedot.kayntiosoite = { ...yhteystiedot.postiosoite };
            setOrganisaatio({ yhteystiedot: yhteystiedot });
        }
    };

    const handleOsoiteMuutos = (kentta: OsoiteKentta, muutos: Partial<Osoite>): void => {
        const yhteystiedot = organisaatio.yhteystiedot;
        yhteystiedot[kentta] = { ...yhteystiedot[kentta], ...muutos };
        if (kentta === 'postiosoite' && kayntiosoiteSamaKuinPostiosoite) {
            yhteystiedot.kayntiosoite = { ...yhteystiedot.postiosoite };
        }
        setOrganisaatio({ yhteystiedot });
    };

     */
    if (postinumerotLoading || postinumerotError) {
       return <Spin />;
    }
    console.log('todo postinumerot', postinumerot);
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
                <div className={styles.Kentta}>
                    <label>Postiosoite</label>
                    <Input value={currentVisibleYhteystiedot.posti && currentVisibleYhteystiedot.posti.osoite} />
                </div>
                <div className={styles.KenttaLyhyt}>
                    <label>Postinumero</label>
                    <Input value={currentVisibleYhteystiedot.posti && currentVisibleYhteystiedot.posti.postinumeroUri} />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Käyntiosoite</label>
                    <Input value={currentVisibleYhteystiedot.kaynti && currentVisibleYhteystiedot.kaynti.osoite} />
                </div>
                <div className={styles.KenttaLyhyt}>
                    <label>Postinumero</label>
                    <Input value={currentVisibleYhteystiedot.posti && currentVisibleYhteystiedot.kaynti.postinumeroUri} />
                </div>
            </div>
            <div className={styles.Rivi}>
                <Checkbox>Postiosoite on sama kuin käyntiosoite</Checkbox>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Puhelinnumero</label>
                    <Input value={currentVisibleYhteystiedot.puhelin && currentVisibleYhteystiedot.puhelin.numero} />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Sähköpostiosoite</label>
                    <Input value={currentVisibleYhteystiedot.email && currentVisibleYhteystiedot.email.email} />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Www-osoite</label>
                    <Input value={currentVisibleYhteystiedot.www && currentVisibleYhteystiedot.www.www} />
                </div>
            </div>
        </div>
    );
}