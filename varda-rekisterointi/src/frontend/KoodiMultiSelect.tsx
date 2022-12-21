import React, { useContext } from 'react';
import { Koodi } from './types/types';
import { toLocalizedText } from './LocalizableTextUtils';
import { LanguageContext } from './contexts';
import classNames from 'classnames';
import Select from 'react-select';
import { OptionTypeBase } from 'react-select/src/types';

type Props = {
    labelledBy?: string;
    selectable: Koodi[];
    selected?: string[];
    disabled?: boolean;
    required?: boolean;
    hasError?: boolean;
    onChange: (uris: string[]) => void;
};

export default function KoodiMultiSelect(props: Props) {
    const { language } = useContext(LanguageContext);
    const classes = classNames({
        'oph-input-has-error': props.hasError,
    });
    const options = props.selectable
        .map((koodi) => ({ value: koodi.uri, label: toLocalizedText(koodi.nimi, language, koodi.arvo) }))
        .sort((option1, option2) => option1.label.localeCompare(option2.label, language));
    const defaultValue: OptionTypeBase[] = props.selected
        ? options.filter((option) => (props.selected ? props.selected.some((select) => select === option.value) : []))
        : [];
    return (
        <div className="oph-input-container">
            <Select
                aria-labelledby={props.labelledBy}
                className={classes}
                placeholder=""
                defaultValue={defaultValue}
                isDisabled={props.disabled}
                options={options}
                isMulti
                isClearable={false}
                hideSelectedOptions={true}
                onChange={(selectionOptions) =>
                    props.onChange(
                        selectionOptions
                            ? selectionOptions.map((selectedOption: OptionTypeBase) => selectedOption.value)
                            : []
                    )
                }
            />
        </div>
    );
}
