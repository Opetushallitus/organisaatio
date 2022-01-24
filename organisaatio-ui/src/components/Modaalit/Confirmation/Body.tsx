import React from 'react';
import styles from './Confirmation.module.css';
import { languageAtom } from '../../../contexts/LanguageContext';
import { useAtom } from 'jotai';

export default function Body({
    messageKey,
    replacements,
}: {
    messageKey: string;
    replacements: { key: string; value: string }[];
}) {
    const [i18n] = useAtom(languageAtom);
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.BodyKentta}>
                <label>{i18n.enrichMessage(messageKey, replacements)}</label>
            </div>
        </div>
    );
}
