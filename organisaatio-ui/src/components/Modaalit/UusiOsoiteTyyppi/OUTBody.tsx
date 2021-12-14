import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/LanguageContext';
import styles from './UusiOsoiteTyyppi.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';

export default function OUTBody() {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.Rivi}>
                <label>{i18n.translate('LABEL_SUOMEKSI')}</label>
                <div className={styles.PitkaInput}>
                    <Input value={''} />
                </div>
            </div>
            <div className={styles.Rivi}>
                <label>{i18n.translate('LABEL_RUOTSIKSI')}</label>
                <div className={styles.PitkaInput}>
                    <Input value={''} />
                </div>{' '}
            </div>
            <div className={styles.Rivi}>
                <label>{i18n.translate('LABEL_ENGLANNIKSI')}</label>
                <div className={styles.PitkaInput}>
                    <Input value={''} />
                </div>{' '}
            </div>
        </div>
    );
}
