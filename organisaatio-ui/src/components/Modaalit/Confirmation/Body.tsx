import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import styles from './Confirmation.module.css';

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
