import * as React from 'react';
import styles from './YhdistysJaSiirto.module.css';
import { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import Input from '@opetushallitus/virkailija-ui-components/Input';

export default function YhdistysJaSiirto() {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.SiirtoLomake}>
            <div className={styles.Rivi}>
                <h3>{i18n.translate('YHDISTYS_ORGANISAATION_YHDISTAMINEN_OTSIKKO')}</h3>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHDISTYS_YHDISTETTAVA_KOULUTUSTOIMIJA')}</label>
                    <Input value={''} />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHDISTYS_NIMENMUUTOS_TULEE_VOIMAAN')}</label>
                    <Input value={''} />
                </div>
            </div>
        </div>
    );
}