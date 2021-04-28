import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import styles from './UusiOsoiteTyyppi.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';

export default function OUTBody() {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.Rivi}>
                <label>{i18n.translate('SUOMEKSI')}</label>
                <div className={styles.PitkaInput}>
                    <Input value={''} />
                </div>
            </div>
            <div className={styles.Rivi}>
                <label>{i18n.translate('RUOTSIKSI')}</label>
                <div className={styles.PitkaInput}>
                    <Input value={''} />
                </div>{' '}
            </div>
            <div className={styles.Rivi}>
                <label>{i18n.translate('ENGLANNIKSI')}</label>
                <div className={styles.PitkaInput}>
                    <Input value={''} />
                </div>{' '}
            </div>
        </div>
    );
}
