import React from 'react';
import { FieldError, FieldValues, Path, UseFormRegister } from 'react-hook-form';
import { FormError } from './FormError';

import styles from './Input.module.css';

type InputProps<T extends FieldValues> = {
    name: Path<T>;
    register: UseFormRegister<T>;
    required?: boolean;
    error?: FieldError;
};

export const Input = <T extends FieldValues>({ name, required, register, error }: InputProps<T>) => {
    const isRequired = required ?? true;
    return (
        <div>
            <input
                id={name}
                className={`${styles.input} ${error ? styles.error : ''}`}
                type="text"
                autoComplete="off"
                aria-invalid={!!error}
                aria-errormessage={`#error-${name}`}
                aria-required={isRequired}
                {...register(name, { required: isRequired })}
            />
            <FormError id={`error-${name}`} error={error?.message} inputId={name} />
        </div>
    );
};
