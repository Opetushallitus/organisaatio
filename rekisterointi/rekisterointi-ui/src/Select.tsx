import React from 'react';
import ReactSelect from 'react-select';
import { Control, Controller, FieldError, FieldValues, Path } from 'react-hook-form';

import { FormError } from './FormError';
import { reactSelectStyles, DropdownIndicator } from './select-styles';

type Option = {
    value: string;
    label: string;
};

type SelectProps<T extends FieldValues> = {
    name: Path<T>;
    control: Control<T, any>;
    options: Option[];
    error?: FieldError;
};

export const Select = <T extends FieldValues>({ name, control, options, error }: SelectProps<T>) => {
    return (
        <div>
            <Controller
                name={name}
                control={control}
                render={({ field }) => (
                    <ReactSelect
                        {...field}
                        id={name}
                        placeholder={null}
                        styles={reactSelectStyles(!!error)}
                        components={{ DropdownIndicator }}
                        options={options}
                    />
                )}
            />
            <FormError error={error?.message} />
        </div>
    );
};
