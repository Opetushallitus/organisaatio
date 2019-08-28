import React, { useContext } from 'react';
import { LocalizableText } from './types';
import { toLocalizedText } from './LocalizableTextUtils';
import { LanguageContext } from './contexts';

type Props = {
    value: LocalizableText,
    disabled?: boolean,
    onChange: (value: LocalizableText) => void,
}

export default function LocalizableTextEdit(props: Props) {
    const language = useContext(LanguageContext);
    const localizedText = toLocalizedText(props.value, language);
    return <input className="oph-input"
                  type="text"
                  value={localizedText}
                  disabled={props.disabled}
                  onChange={event => props.onChange({ ...props.value, [language]: event.currentTarget.value })} />
}
