import React, { useContext } from 'react';
import { Koodi } from './types';
import { toLocalizedText } from './LocalizableTextUtils';
import { LanguageContext } from './contexts';

type Props = {
    id?: string,
    readOnly?: boolean,
    selectable: Koodi[],
    selected?: string,
    disabled?: boolean,
    onChange: (uri: string) => void,
}

export default function KoodiSelectRadio(props: Props) {
    const { language } = useContext(LanguageContext);
    if (props.readOnly) {
        const value = props.selectable.find(koodi => koodi.uri === props.selected)
        return <div>{value ? toLocalizedText(value.nimi, language, value.arvo) : ''}</div>;
    }
    return (
        <div role="radiogroup"
             id={props.id}>
            {props.selectable.map(koodi => {
                return (
                    <label className="oph-checkable" htmlFor={koodi.uri} key={koodi.uri}>
                        <input id={koodi.uri}
                               className="oph-checkable-input"
                               type="radio"
                               value={koodi.uri}
                               checked={koodi.uri === props.selected}
                               disabled={props.disabled}
                               onChange={event => props.onChange(event.currentTarget.value)} />
                        <span className="oph-checkable-text">{toLocalizedText(koodi.nimi, language, koodi.arvo)}</span>
                    </label>
                )
            })}
        </div>
    )
}
