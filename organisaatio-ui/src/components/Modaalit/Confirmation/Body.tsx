import React, { useContext } from 'react';
import styles from './Confirmation.module.css';
import { LanguageContext } from '../../../contexts/LanguageContext';

export default function Body({
    messageKey,
    replacements,
}: {
    messageKey: string;
    replacements: { key: string; value: string }[];
}) {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.BodyKentta}>
                <label>{i18n.enrichMessage(messageKey, replacements)}</label>
            </div>
        </div>
    );
}
