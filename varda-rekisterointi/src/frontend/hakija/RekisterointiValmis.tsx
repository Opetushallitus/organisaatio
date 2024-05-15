import React, { useContext } from 'react';
import { LanguageContext } from '../contexts';
import Image from './hakemus_odottaa_hyvaksyntaa.svg';
import styles from './RekisterointiValmis.module.css';
import Fieldset from '../Fieldset';

export default function RekisterointiValmis() {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.section}>
            <div className={styles.child}>
                <form>
                    <Fieldset
                        title={i18n.translate('REKISTEROINNIN_KASITTELY')}
                        description={i18n.translate('REKISTEROINNIN_KASITTELY_KUVAUS')}
                    ></Fieldset>
                    <p>{i18n.translate('REKISTEROINNIN_KASITTELY_OHJE')}</p>
                    <div className={styles.center}>
                        <Image />
                    </div>
                </form>
            </div>
        </div>
    );
}
