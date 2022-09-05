import React from 'react';
import ReactDatePicker from 'react-datepicker';
import { Control, Controller, FieldError, Path } from 'react-hook-form';

import { FormError } from './FormError';
import { useLanguageContext } from './LanguageContext';

type DatePickerProps<T> = {
    name: Path<T>;
    control: Control<T>;
    error?: FieldError;
};

export const DatePicker = <T,>({ name, control, error }: DatePickerProps<T>) => {
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
                        onChange={(e) => field.onChange(e)}
                        selected={field.value as Date}
                        dateFormat="dd.MM.yyyy"
                        className={error ? 'invalid_date' : ''}
                    />
                )}
            />
            <FormError error={error?.message} />
        </div>
    );
};
