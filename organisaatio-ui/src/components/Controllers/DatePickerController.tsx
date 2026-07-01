import { Control, Controller, FieldValues, Path } from 'react-hook-form';
import DatePickerInput, { DatePickerInputProps } from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import * as React from 'react';

export default function DatePickerController<T extends FieldValues>({
    name,
    form,
    validationErrors,
    dayPickerProps,
    disabled,
}: {
    form: Control<T>;
    validationErrors: { [x: string]: unknown };
    name: Path<T>;
    disabled?: boolean;
    dayPickerProps?: DatePickerInputProps['dayPickerProps'];
}) {
    return (
        <Controller
            control={form}
            name={name}
            render={({ field: { ref: _, value, ...controllerRest } }) => (
                <DatePickerInput
                    value={value}
                    error={!!validationErrors[name]}
                    dayPickerProps={dayPickerProps}
                    inputProps={{ disabled }}
                    {...controllerRest}
                />
            )}
        />
    );
}
