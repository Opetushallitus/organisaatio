import React, { useContext } from 'react';
import { Koodi } from './types';
import { toLocalizedText } from './LocalizableTextUtils';
import { LanguageContext } from './contexts';

type Props = {
    selectable: Koodi[],
    selected?: string,
    disabled?: boolean,
    onChange: (uri: string) => void,
}

export default function KoodiSelectRadio(props: Props) {
    const { language } = useContext(LanguageContext);
    return (
        <div>
            {props.selectable.map(koodi => {
                return (
                    <label className="oph-checkable" key={koodi.uri}>
                        <input className="oph-checkable-input"
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
