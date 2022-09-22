import React, { useContext } from 'react';
import { Koodi } from './types/types';
import { toLocalizedText } from './LocalizableTextUtils';
import { LanguageContext } from './contexts';

type Props = {
    id?: string;
    readOnly?: boolean;
    selectable: Koodi[];
    selected?: string;
    onChange: (uri: string) => void;
    autoFocus?: boolean;
};

const KoodiSelectRadio = (props: Props) => {
    const { language } = useContext(LanguageContext);
    if (props.readOnly) {
        const value = props.selectable.find((koodi) => koodi.uri === props.selected);
        return <div tabIndex={0}>{value ? toLocalizedText(value.nimi, language, value.arvo) : ''}</div>;
    }
    return (
        <div role="radiogroup" id={props.id}>
            {props.selectable.map((koodi) => {
                return (
                    <label className="oph-checkable" htmlFor={koodi.uri} key={koodi.uri}>
                        <input
                            autoFocus={props.autoFocus}
                            id={koodi.uri}
                            className="oph-checkable-input"
                            type="radio"
                            value={koodi.uri}
                            checked={koodi.uri === props.selected}
                            readOnly={props.readOnly}
                            onChange={(event) => props.onChange(event.currentTarget.value)}
                        />
                        <span className="oph-checkable-text">{toLocalizedText(koodi.nimi, language, koodi.arvo)}</span>
                    </label>
                );
            })}
        </div>
    );
};

KoodiSelectRadio.defaultProps = {
    autoFocus: false,
};

export default KoodiSelectRadio;
