import React, { useContext } from 'react';
import { LocalizableText } from './types';
import { toLocalizedText } from './LocalizableTextUtils';
import { LanguageContext } from './contexts';

type ItemType = {
    value: string,
    label: LocalizableText,
}

type Props = {
    id?: string,
    selectable: ItemType[],
    selected?: string,
    disabled?: boolean,
    onChange: (key: string) => void,
}

export default function Select(props: Props) {
    const language = useContext(LanguageContext);
    return (
        <div className="oph-select-container">
            <select id={props.id}
                    className="oph-input oph-select"
                    defaultValue={props.selected}
                    disabled={props.disabled}
                    onChange={event => props.onChange(event.currentTarget.value)}>
                {props.selectable.map(item => <option value={item.value} key={item.value}>{toLocalizedText(item.label, language)}</option>)}
            </select>
        </div>
    )
}
