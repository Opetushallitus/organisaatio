import React, { useContext } from 'react';
import { Koodi } from './types';
import { toLocalizedText } from './LocalizableTextUtils';
import { LanguageContext } from './contexts';

type Props = {
    id?: string,
    selectable: Koodi[],
    selected?: string,
    disabled?: boolean,
    required?: boolean,
    onChange: (uri: string) => void,
}

export default function KoodiSelect(props: Props) {
    const language = useContext(LanguageContext);
    return (
        <div className="oph-select-container">
            <select id={props.id}
                    className="oph-input oph-select"
                    defaultValue={props.selected}
                    disabled={props.disabled}
                    onChange={event => props.onChange(event.currentTarget.value)}>
                {props.required && props.selected ? null : <option value=""></option>}
                {props.selectable.map(koodi => <option value={koodi.uri} key={koodi.uri}>{toLocalizedText(koodi.nimi, language, koodi.arvo)}</option>)}
            </select>
        </div>
    )
}
