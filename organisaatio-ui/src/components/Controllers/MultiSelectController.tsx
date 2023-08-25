import { Control, Controller, FieldValues, Path } from 'react-hook-form';
import * as React from 'react';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { ValueType } from 'react-select';
import { Koodisto } from '../../types/types';

export default function MultiSelectController<T extends FieldValues>({
    name,
    form,
    validationErrors,
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
                    <Select
                        isDisabled={disabled}
                        isMulti
                        value={value as ValueType<{ label: string; value: string }>}
                        id={name}
                        {...rest}
                        error={!!validationErrors[name]}
                        options={koodisto?.selectOptions()}
                    />
                );
            }}
        />
    );
}
