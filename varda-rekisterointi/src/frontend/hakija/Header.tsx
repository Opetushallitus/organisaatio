import React, { useContext } from 'react';
import { asiointikielet, toLocalizedText } from '../LocalizableTextUtils';
import { LanguageContext } from '../contexts';
import { Language } from '../types';
import styles from './Header.module.css';

type Props = {
    hideLanguage?: boolean,
}

export default function Header(props: Props) {
    const { language, setLanguage, i18n } = useContext(LanguageContext);
    return (
        <header className={styles.header}>
            <div className={styles.title}>{i18n.translate('OTSIKKO')}</div>
            { !props.hideLanguage ?
            <div className={styles.language}>{i18n.translate('SISALLON_KIELI')}:
                <select className={styles.select}
                        defaultValue={language}
                        onChange={event => setLanguage(event.currentTarget.value as Language)}>
                    {asiointikielet.map(asiointikieli => <option value={asiointikieli.value} key={asiointikieli.value}>{toLocalizedText(asiointikieli.label, language)}</option>)}
                </select>
            </div>
            : null }
        </header>
    )
}
