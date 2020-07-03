import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Button from "@opetushallitus/virkailija-ui-components/Button";
import Input from "@opetushallitus/virkailija-ui-components/Input";
import Checkbox from "@opetushallitus/virkailija-ui-components/Checkbox";
import RadioGroup from "@opetushallitus/virkailija-ui-components/RadioGroup";

type yhteystietoProps = {
    yhteystiedot?: any
}

export default function YhteystietoLomake(props: yhteystietoProps) {
    console.log(props.yhteystiedot)
    return(
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <RadioGroup
                        value={'1'}
                        options={[
                            { value: '1', label: 'Suomeksi' },
                            { value: '2', label: 'Ruotsiksi' },
                            { value: '3', label: 'Englanniksi' },
                        ]}
                        onChange={() => {}}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Postiosoite</label>
                    <Input />
                </div>
                <div className={styles.KenttaLyhyt}>
                    <label>Postinumero</label>
                    <Input />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Käyntiosoite</label>
                    <Input />
                </div>
                <div className={styles.KenttaLyhyt}>
                    <label>Postinumero</label>
                    <Input />
                </div>
            </div>
            <div className={styles.Rivi}>
                <Checkbox>Postiosoite on sama kuin käyntiosoite</Checkbox>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Puhelinnumero</label>
                    <Input />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Sähköpostiosoite</label>
                    <Input />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Www-osoite</label>
                    <Input />
                </div>
            </div>
        </div>
    );
}