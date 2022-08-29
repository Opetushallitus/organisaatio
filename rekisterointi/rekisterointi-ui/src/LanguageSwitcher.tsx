import React, { useContext } from 'react';

import { LanguageContext } from './contexts';

import styles from './LanguageSwitcher.module.css';

export function LanguageSwitcher() {
    const { language, setLanguage } = useContext(LanguageContext);

    return (
        <div className={styles.switcherContainer}>
            <div className={styles.languageSwitcher}>
                <button
                    className={`${styles.languageButton} ${styles.leftButton}`}
                    onClick={() => setLanguage('fi')}
                    disabled={language === 'fi'}
                >
                    Suomeksi
                </button>
                <button
                    className={`${styles.languageButton} ${styles.rightButton}`}
                    onClick={() => setLanguage('sv')}
                    disabled={language === 'sv'}
                >
                    På svenska
                </button>
            </div>
        </div>
    );
}
