import React, { useContext } from 'react';
import { Koodi, Language } from './types/types';
import { toLocalizedText } from './LocalizableTextUtils';
import { LanguageContext } from './contexts';
import classNames from 'classnames';

type Props = {
    tabIndex?: number;
    id?: string;
    selectable: Koodi[];
    selected?: string;
    disabled?: boolean;
    required?: boolean;
    hasError?: boolean;
    valueFn?: (koodi: Koodi) => string;
    optionLabelFn?: (koodi: Koodi, language: Language) => string;
    sortFn?: (item1: { value: string; label: string }, item2: { value: string; label: string }) => number;
    onChange: (uri: string) => void;
};

export default function KoodiSelect(props: Props) {
    const { language } = useContext(LanguageContext);
    const classes = classNames({
        'oph-input': true,
        'oph-select': true,
        'oph-input-has-error': props.hasError,
    });
    const valueFn = props.valueFn ? props.valueFn : (koodi: Koodi) => koodi.uri;
    const optionLabelFn = props.optionLabelFn
        ? props.optionLabelFn
        : (koodi: Koodi, language: Language) => toLocalizedText(koodi.nimi, language, koodi.arvo);
    const sortFn = props.sortFn
        ? props.sortFn
        : (item1: { value: string; label: string }, item2: { value: string; label: string }) =>
              item1.label.localeCompare(item2.label, language);
    return (
        <div className="oph-select-container">
            <select
                id={props.id}
                tabIndex={props.tabIndex || 0}
                className={classes}
                defaultValue={props.selected}
                disabled={props.disabled}
                onChange={(event) => props.onChange(event.currentTarget.value)}
            >
                {props.required && props.selected ? null : <option value=""></option>}
                {props.selectable
                    .map((koodi) => ({ value: valueFn(koodi), label: optionLabelFn(koodi, language) }))
                    .sort(sortFn)
                    .map((item) => (
                        <option value={item.value} key={item.value}>
                            {item.label}
                        </option>
                    ))}
            </select>
        </div>
    );
}
