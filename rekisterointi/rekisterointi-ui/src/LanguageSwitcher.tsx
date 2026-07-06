import React from 'react';

import { useLanguageContext } from './LanguageContext';
import { Language } from './types';

import styles from './LanguageSwitcher.module.css';
import { useSetLanguageMutation } from './rekisterointiApi';

export function LanguageSwitcher() {
    const { language } = useLanguageContext();
    const [setLanguageMutation, { isLoading }] = useSetLanguageMutation();

    const onChange = async (lang: Language) => {
        await setLanguageMutation(lang).unwrap();
    };

    return (
        <div className={styles.languageSwitcher}>
            <button
                className={`${styles.languageButton} ${styles.leftButton}`}
                onClick={() => onChange('fi')}
                disabled={language === 'fi' || isLoading}
            >
                Suomeksi
            </button>
            <button
                className={`${styles.languageButton} ${styles.rightButton}`}
                onClick={() => onChange('sv')}
                disabled={language === 'sv' || isLoading}
            >
                På svenska
            </button>
        </div>
    );
}
