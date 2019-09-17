import React, { useContext } from 'react';
import { asiointikielet, toLocalizedText } from '../LocalizableTextUtils';
import { LanguageContext } from '../contexts';
import { Language } from '../types';
import styles from './Header.module.css';

export default function Header() {
    const { language, setLanguage, i18n } = useContext(LanguageContext);
    return (
        <header className={styles.header}>
            <div>{i18n.translate('OTSIKKO')}</div>
            <div>{i18n.translate('SISALLON_KIELI')}:
                <select className={styles.select}
                        defaultValue={language}
                        onChange={event => setLanguage(event.currentTarget.value as Language)}>
                    {asiointikielet.map(asiointikieli => <option value={asiointikieli.value} key={asiointikieli.value}>{toLocalizedText(asiointikieli.label, language)}</option>)}
                </select>
            </div>
        </header>
    )
}
