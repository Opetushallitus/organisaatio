import { Control, Controller, Path } from 'react-hook-form';
import * as React from 'react';
import Input from '@opetushallitus/virkailija-ui-components/Input';

export default function InputController<T>({
    name,
    form,
    validationErrors,
    disabled,
}: {
    form: Control<T>;
    validationErrors: { [x: string]: unknown };
    name: Path<T>;
    disabled: boolean;
}) {
    return (
        <Controller
            control={form}
            name={name}
            render={({ field: { ref, ...rest } }) => {
                return <Input disabled={disabled} id={name} {...rest} error={!!validationErrors[name]} />;
            }}
        />
    );
}
