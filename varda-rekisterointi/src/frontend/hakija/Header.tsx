import React, { useContext } from 'react';
import { asiointikielet, toLocalizedText } from '../LocalizableTextUtils';
import { LanguageContext } from '../contexts';
import { Language } from '../types/types';
import styles from './Header.module.css';
import Axios from 'axios';

export default function Header() {
    const { language, setLanguage, i18n } = useContext(LanguageContext);
    async function onChange(language: Language) {
        try {
            await Axios.put(`/varda-rekisterointi/api/lokalisointi/kieli?locale=${language}`);
        } catch (error) {
            console.log(error);
        } finally {
            setLanguage(language);
        }
    }
    return (
        <header className={styles.header}>
            <div className={styles.title}>{i18n.translate('OTSIKKO')}</div>
            <div className={styles.language}>
                <label htmlFor="kielivalikko">{i18n.translate('SISALLON_KIELI')}</label>:
                <select
                    id="kielivalikko"
                    className={styles.select}
                    defaultValue={language}
                    onChange={(event) => onChange(event.currentTarget.value as Language)}
                >
                    {asiointikielet.map((asiointikieli) => (
                        <option value={asiointikieli.value} key={asiointikieli.value}>
                            {toLocalizedText(asiointikieli.label, language)}
                        </option>
                    ))}
                </select>
            </div>
        </header>
    );
}
