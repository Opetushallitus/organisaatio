import React, { useContext } from 'react';

import { LanguageContext } from './contexts';
import { Language } from './types';

import styles from './LanguageSwitcher.module.css';
import axios from 'axios';

export function LanguageSwitcher() {
    const { language, setLanguage } = useContext(LanguageContext);
    const onChange = async (lang: Language) => {
        await axios.put(`/api/lokalisointi/kieli?locale=${lang}`);
        setLanguage(lang);
    };

    return (
        <div className={styles.switcherContainer}>
            <div className={styles.languageSwitcher}>
                <button
                    className={`${styles.languageButton} ${styles.leftButton}`}
                    onClick={() => onChange('fi')}
                    disabled={language === 'fi'}
                >
                    Suomeksi
                </button>
                <button
                    className={`${styles.languageButton} ${styles.rightButton}`}
                    onClick={() => onChange('sv')}
                    disabled={language === 'sv'}
                >
                    På svenska
                </button>
            </div>
        </div>
    );
}
