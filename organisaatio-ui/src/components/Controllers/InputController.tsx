import { Control, Controller, Path } from 'react-hook-form';
import * as React from 'react';
import Input from '@opetushallitus/virkailija-ui-components/Input';

export default function InputController<T>({
    name,
    form,
    validationErrors,
}: {
    form: Control<T>;
    validationErrors: { [x: string]: unknown };
    name: Path<T>;
}) {
    return (
        <Controller
            control={form}
            name={name}
            render={({ field: { ref, ...rest } }) => {
                return <Input id={name} {...rest} error={!!validationErrors[name]} />;
            }}
        />
    );
}
