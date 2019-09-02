import React, { useContext } from 'react';
import Select from '../Select';
import { asiointikielet } from '../LocalizableTextUtils';
import { LanguageContext } from '../contexts';
import { Language } from '../types';

export default function Header() {
    const { language, setLanguage, i18n } = useContext(LanguageContext);
    return (
        <header>
            <div>{i18n.translate('OTSIKKO')}</div>
            <div>{i18n.translate('SISALLON_KIELI')}:
                <Select selectable={asiointikielet}
                        selected={language}
                        onChange={language => setLanguage(language as Language)}
                ></Select>
            </div>
        </header>
    )
}
