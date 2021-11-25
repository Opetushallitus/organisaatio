import { Control, Controller, Path } from 'react-hook-form';
import * as React from 'react';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { ValueType } from 'react-select';
import { Koodisto } from '../../types/types';

export default function MultiSelectController<T>({
    name,
    form,
    validationErrors,
    koodisto,
}: {
    form: Control<T>;
    validationErrors: { [x: string]: any };
    name: Path<T>;
    koodisto: Koodisto;
}) {
    return (
        <Controller
            control={form}
            name={name}
            render={({ field: { ref, value, ...rest } }) => {
                return (
                    <Select
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
