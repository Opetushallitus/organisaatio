import React, { useContext } from 'react';
import { LocalizableText } from './types/types';
import { toLocalizedText } from './LocalizableTextUtils';
import { LanguageContext } from './contexts';
import classNames from 'classnames';

type ItemType = {
    value: string;
    label: LocalizableText;
};

type Props = {
    id?: string;
    selectable: ItemType[];
    selected?: string;
    disabled?: boolean;
    hasError?: boolean;
    onChange: (key: string) => void;
};

export default function Select(props: Props) {
    const { language } = useContext(LanguageContext);
    const classes = classNames({
        'oph-input': true,
        'oph-select': true,
        'oph-input-has-error': props.hasError,
    });
    return (
        <div className="oph-select-container">
            <select
                id={props.id}
                className={classes}
                defaultValue={props.selected}
                disabled={props.disabled}
                onChange={(event) => props.onChange(event.currentTarget.value)}
            >
                {props.selectable.map((item) => (
                    <option value={item.value} key={item.value}>
                        {toLocalizedText(item.label, language)}
                    </option>
                ))}
            </select>
        </div>
    );
}
