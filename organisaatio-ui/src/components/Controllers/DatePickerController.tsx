import { Control, Controller, Path } from 'react-hook-form';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import moment from 'moment';
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
            render={({ field: { ref, value, ...controllerRest } }) => {
                const formattedDate = value ? moment(new Date(value)).format('D.M.yyyy') : '';
                return (
                    <DatePickerInput
                        dayPickerProps={dayPickerProps}
                        value={formattedDate}
                        error={!!validationErrors[name]}
                        {...controllerRest}
                    />
                );
            }}
        />
    );
}
