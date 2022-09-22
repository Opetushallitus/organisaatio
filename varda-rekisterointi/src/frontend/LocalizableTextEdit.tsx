import React, { useContext } from 'react';
import { LocalizableText } from './types/types';
import { toLocalizedText } from './LocalizableTextUtils';
import { LanguageContext } from './contexts';
import classNames from 'classnames/bind';

type Props = {
    value: LocalizableText;
    disabled?: boolean;
    hasError?: boolean;
    onChange: (value: LocalizableText) => void;
};

export default function LocalizableTextEdit(props: Props) {
    const { language } = useContext(LanguageContext);
    const localizedText = toLocalizedText(props.value, language);
    const classes = classNames({
        'oph-input': true,
        'oph-input-has-error': props.hasError,
    });
    return (
        <input
            className={classes}
            type="text"
            value={localizedText}
            disabled={props.disabled}
            onChange={(event) => props.onChange({ ...props.value, [language]: event.currentTarget.value })}
        />
    );
}
