import React, {useContext} from "react";
import {LanguageContext} from "../../../contexts/contexts";
import styles from './ToimipisteenNimenmuutos.module.css';
import Input from "@opetushallitus/virkailija-ui-components/Input";

export default function TNBody() {
    const { i18n} = useContext(LanguageContext);
    return (
        <div className={styles.BodyKehys}>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('NIMENMUUTOS_TULEE_VOIMAAN')}</label>
                    <Input value={''}/>
                </div>
                <div className={styles.BodyKentta}>
                    <div className={styles.BodyKentta}>
                        <label>{i18n.translate('SUOMEKSI')}</label>
                        <Input value={''}/>
                    </div>
                    <div className={styles.BodyKentta}>
                        <label>{i18n.translate('RUOTSIKSI')}</label>
                        <Input value={''}/>
                    </div>
                    <div className={styles.BodyKentta}>
                        <label>{i18n.translate('ENGLANNIKSI')}</label>
                        <Input value={''}/>
                    </div>
                </div>
        </div>
    );
}