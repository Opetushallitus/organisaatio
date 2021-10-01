import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import styles from './Confirmation.module.css';

export default function Body({ messageKey }: { messageKey: string }) {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.BodyKentta}>
                <label>{i18n.translate(messageKey)}</label>
            </div>
        </div>
    );
}
