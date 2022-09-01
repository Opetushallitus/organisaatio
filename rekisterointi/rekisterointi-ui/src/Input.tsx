import React from 'react';
import { FieldError, Path, UseFormRegister } from 'react-hook-form';
import { FormError } from './FormError';

import styles from './Input.module.css';

type InputProps<T> = {
    name: Path<T>;
    register: UseFormRegister<T>;
    required?: boolean;
    error?: FieldError;
};

export const Input = <T,>({ name, required, register, error }: InputProps<T>) => {
    return (
        <div>
            <input
                className={`${styles.input} ${error ? styles.error : ''}`}
                type="text"
                {...register(name, { required })}
            />
            <FormError error={error?.message} />
        </div>
    );
};
