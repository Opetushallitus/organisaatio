import { Control, Controller, FieldValues, Path } from 'react-hook-form';
import * as React from 'react';
import Select from 'react-select';
import { Koodisto, KoodistoSelectOption } from '../../types/types';

export default function SelectController<T extends FieldValues>({
    name,
    form,
    koodisto,
    disabled,
}: {
    form: Control<T>;
    validationErrors: { [x: string]: unknown };
    name: Path<T>;
    koodisto: Koodisto;
    disabled: boolean;
}) {
    return (
        <Controller
            control={form}
            name={name}
            render={({ field: { ref, value, ...rest } }) => {
                return (
                    <Select<KoodistoSelectOption>
                        isDisabled={disabled}
                        value={value}
                        id={name}
                        {...rest}
                        options={koodisto?.selectOptions()}
                    />
                );
            }}
        />
    );
}
