import React, { useContext } from 'react';
import { LanguageContext } from '../contexts';
import { ReactComponent as Image } from './hakemus odottaa hyväksyntää.svg';
import styles from './RekisterointiValmis.module.css';
import Fieldset from '../Fieldset';

export default function RekisterointiValmis() {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.section}>
            <div className={styles.child}>
                <form>
                    <Fieldset title={i18n.translate('REKISTEROINNIN_KASITTELY')}
                              description={i18n.translate('REKISTEROINNIN_KASITTELY_KUVAUS')}>
                    </Fieldset>
                    <p>{i18n.translate('REKISTEROINNIN_KASITTELY_OHJE')}</p>
                    <p className={styles.center}><Image /></p>
                </form>
            </div>
        </div>
    );
}
