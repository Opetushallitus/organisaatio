import { Control, Controller, Path } from 'react-hook-form';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import * as React from 'react';
import { DayPickerProps } from 'react-day-picker/types/Props';

export default function DatePickerController<T>({
    name,
    form,
    validationErrors,
    dayPickerProps,
}: {
    form: Control<T>;
    validationErrors: { [x: string]: unknown };
    name: Path<T>;
    dayPickerProps?: DayPickerProps;
}) {
    return (
        <Controller
            control={form}
            name={name}
            render={({ field: { ref, value, ...controllerRest } }) => (
                <DatePickerInput
                    value={value}
                    dayPickerProps={dayPickerProps}
                    error={!!validationErrors[name]}
                    {...controllerRest}
                />
            )}
        />
    );
}
