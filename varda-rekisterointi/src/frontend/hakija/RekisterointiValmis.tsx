import React, { useContext } from 'react';
import { LanguageContext } from '../contexts';
import { ReactComponent as Image } from './hakemus odottaa hyväksyntää.svg';
import styles from './RekisterointiValmis.module.css';
import Fieldset from '../Fieldset';

export default function RekisterointiValmis() {
    const { i18n } = useContext(LanguageContext);
    return (
        <form>
            <Fieldset title={i18n.translate('REKISTEROINNIN_KASITTELY')}>
            </Fieldset>
            <div>{i18n.translate('REKISTEROINNIN_KASITTELY_KUVAUS')}</div>
            <div>{i18n.translate('REKISTEROINNIN_KASITTELY_OHJE')}</div>
            <div className={styles.center}><Image /></div>
        </form>
    );
}
