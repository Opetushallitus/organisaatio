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
    const message = i18n.translate(messageKey);
    const enrichedMessage = replacements.reduce((previous, current) => {
        return previous.replace(`{${current.key}}`, current.value);
    }, message);
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.BodyKentta}>
                <label>{enrichedMessage}</label>
            </div>
        </div>
    );
}
