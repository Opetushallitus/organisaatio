import React from 'react';
import styles from './SelectDropdown.module.css';
import Select, { components, createFilter, MenuListProps, MultiValue, OptionProps } from 'react-select';
import { FixedSizeList as List } from 'react-window';
import { CheckedIcon, UncheckedIcon } from './Checkbox';

export type DropdownProps = {
    label: string;
    options: DropdownOption[];
    onChange: (selection: string[]) => void;
    disabled?: boolean;
    selections: string[];
    classNamePrefix?: string;
};

export type DropdownOption = {
    label: string;
    value: string;
};

export function SelectDropdown({ onChange, label, options, disabled, selections, classNamePrefix }: DropdownProps) {
    const selection = options.filter((v) => selections.includes(v.value));
    function selectOnChange(selection: MultiValue<DropdownOption>) {
        if (Array.isArray(selection)) {
            onChange(selection.map((_) => _.value));
        } else {
            throw new Error('Selection is not an array');
        }
    }
    function removeSelection(value: string) {
        onChange(selection.filter((_) => _.value !== value).map((_) => _.value));
    }

    return (
        <div className={styles.SelectDropdown} role="listbox" aria-multiselectable={true}>
            <Select<DropdownOption, true>
                aria-label={label}
                className={styles.Select}
                escapeClearsValue={false}
                hideSelectedOptions={false}
                captureMenuScroll={false}
                components={{ MenuList: CustomMenuList, Option: CustomOption }}
                placeholder={label}
                isMulti
                isDisabled={disabled}
                isClearable={false}
                options={options}
                styles={{
                    option: (_baseStyles, state) => ({
                        margin: '0 8px',
                        borderRadius: '3px',
                        backgroundColor: state.isFocused
                            ? 'var(--color-primary-blue-lighten-4)'
                            : 'var(--color-neutral-white)',
                    }),
                    menuPortal: (base) => ({ ...base, zIndex: 9999 }),
                }}
                closeMenuOnSelect={false}
                value={selection}
                onChange={selectOnChange}
                backspaceRemovesValue={false}
                controlShouldRenderValue={false}
                classNamePrefix={classNamePrefix}
                filterOption={createFilter({ ignoreAccents: false })}
            />
            <SelectionList>
                {selection.map((v) => (
                    <SelectionItem key={v.value} value={v.value} label={v.label} onRemove={removeSelection} />
                ))}
            </SelectionList>
        </div>
    );
}

export function SelectionList({ children }: { children: React.ReactNode }) {
    return <div className={styles.SelectionList}>{children}</div>;
}

type SelectionItemProps = {
    value: string;
    label: string;
    onRemove: (value: string) => void;
};
export function SelectionItem({ value, label, onRemove }: SelectionItemProps) {
    return (
        <div className={styles.Selection}>
            <span>{label}</span>
            <IconRemoveSelection
                tabIndex={0}
                role="button"
                onKeyDown={() => onRemove(value)}
                onClick={() => onRemove(value)}
            />
        </div>
    );
}

const CustomMenuList = (props: MenuListProps<DropdownOption, true>) => {
    const { children, maxHeight } = props;
    const childrenOptions = React.Children.toArray(children);
    return (
        <List itemSize={36} height={maxHeight} width="100%" itemCount={childrenOptions.length}>
            {({ index, style }) => (
                <div style={style} key={index}>
                    {childrenOptions[index]}
                </div>
            )}
        </List>
    );
};

export function CustomOption(props: OptionProps<DropdownOption, true>) {
    const { isSelected, children } = props;
    return (
        <components.Option {...props}>
            <div className={styles.Option} aria-label={props.data.label} aria-selected={isSelected}>
                <div className={styles.OptionCheckbox}>{isSelected ? <CheckedIcon /> : <UncheckedIcon />}</div>
                <div className={styles.OptionText} title={children?.toString()}>
                    {children}
                </div>
            </div>
        </components.Option>
    );
}

export function IconRemoveSelection(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg {...props} width="13" height="14" viewBox="0 0 13 14" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
                d="M2.0211 12.0915L1.23779 11.3082L5.54598 7L1.23779 2.69182L2.0211 1.90851L6.32929 6.2167L10.6375 1.90851L11.4208 2.69182L7.1126 7L11.4208 11.3082L10.6375 12.0915L6.32929 7.78331L2.0211 12.0915Z"
                fill="white"
            />
        </svg>
    );
}
