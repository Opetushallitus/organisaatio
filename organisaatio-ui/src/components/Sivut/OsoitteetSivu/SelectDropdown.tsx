import React from 'react';
import styles from './SelectDropdown.module.css';
import Select, { components, OptionProps, ValueType } from 'react-select';
import { CheckedIcon, UncheckedIcon } from './Checkbox';

export type DropdownProps = {
    label: string;
    options: DropdownOption[];
    onChange: (selection: string[]) => void;
    disabled?: boolean;
    initialSelection: string[];
};
export type DropdownOption = {
    label: string;
    value: string;
};
export function SelectDropdown({ onChange, label, options, disabled, initialSelection }: DropdownProps) {
    const initialValue = options.filter((v) => initialSelection.includes(v.value));
    const [selection, setSelection] = React.useState<DropdownOption[]>(initialValue);
    function updateSelection(newSelection: DropdownOption[]) {
        setSelection(newSelection);
        onChange(newSelection.map((_) => _.value));
    }
    function selectOnChange(selection: ValueType<DropdownOption>) {
        if (Array.isArray(selection)) {
            updateSelection(selection);
        } else {
            throw new Error('Selection is not an array');
        }
    }
    function removeSelection(value: string) {
        updateSelection(selection.filter((_) => _.value !== value));
    }

    return (
        <div className={styles.SelectDropdown}>
            <Select<DropdownOption>
                className={styles.Select}
                escapeClearsValue={false}
                hideSelectedOptions={false}
                components={{
                    ClearIndicator: () => null, // Disable clear button
                    Option: CustomOption,
                }}
                placeholder={label}
                isMulti={true}
                isDisabled={disabled}
                isClearable={true}
                options={options}
                styles={{ option: () => ({}) }}
                closeMenuOnSelect={false}
                value={selection}
                onChange={selectOnChange}
                backspaceRemovesValue={false}
                controlShouldRenderValue={false}
            />
            <div className={styles.SelectionList}>
                {selection.map((v) => (
                    <div key={v.value} className={styles.Selection}>
                        <span>{v.label}</span>
                        <IconRemoveSelection onClick={() => removeSelection(v.value)} />
                    </div>
                ))}
            </div>
        </div>
    );
}

function CustomOption(props: OptionProps<DropdownOption>) {
    const { isSelected, children } = props;
    return (
        <components.Option {...props} className={styles.Option}>
            {isSelected ? <CheckedIcon /> : <UncheckedIcon />}
            {children}
        </components.Option>
    );
}

function IconRemoveSelection(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg {...props} width="13" height="14" viewBox="0 0 13 14" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
                d="M2.0211 12.0915L1.23779 11.3082L5.54598 7L1.23779 2.69182L2.0211 1.90851L6.32929 6.2167L10.6375 1.90851L11.4208 2.69182L7.1126 7L11.4208 11.3082L10.6375 12.0915L6.32929 7.78331L2.0211 12.0915Z"
                fill="white"
            />
        </svg>
    );
}
