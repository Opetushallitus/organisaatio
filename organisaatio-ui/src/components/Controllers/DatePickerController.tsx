import { Control, Controller, FieldValues, Path } from 'react-hook-form';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import * as React from 'react';
import { DayPickerProps } from 'react-day-picker/types/Props';

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
    dayPickerProps?: DayPickerProps;
}) {
    return (
        <Controller
            control={form}
            name={name}
            render={({ field: { ref, value, ...controllerRest } }) => (
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
