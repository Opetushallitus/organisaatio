import React from 'react';
import ReactDatePicker from 'react-datepicker';
import { Control, Controller, FieldError, FieldValues, Path } from 'react-hook-form';
import format from 'date-fns/format';
import parse from 'date-fns/parse';

import { FormError } from './FormError';
import { useLanguageContext } from './LanguageContext';

type DatePickerProps<T extends FieldValues> = {
    name: Path<T>;
    control: Control<T>;
    error?: FieldError;
};

const dateFormat = 'd.M.yyyy';

const parseDate = (value: string) => {
    const date = value ? parse(value, dateFormat, new Date()) : null;
    if (date?.toString() === 'Invalid Date') {
        return null;
    }
    return date;
};

export const DatePicker = <T extends FieldValues>({ name, control, error }: DatePickerProps<T>) => {
    const { language } = useLanguageContext();
    return (
        <div>
            <Controller
                name={name}
                control={control}
                render={({ field }) => (
                    <ReactDatePicker
                        id={name}
                        locale={language}
                        aria-invalid={!!error}
                        aria-errormessage={`#error-${name}`}
                        onChange={(e) => field.onChange(e && format(e, dateFormat))}
                        selected={parseDate(field.value as string)}
                        dateFormat={dateFormat}
                        className={error ? 'invalid_date' : ''}
                    />
                )}
            />
            <FormError id={`error-${name}`} error={error?.message} inputId={name} />
        </div>
    );
};
