import { Control, Controller, Path } from 'react-hook-form';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import moment from 'moment';
import * as React from 'react';

export default function DatePickerController<T>(props: {
    form: Control<T>;
    validationErrors: { [x: string]: any };
    name: Path<T>;
}) {
    return (
        <Controller
            control={props.form}
            name={props.name}
            render={({ field: { ref, value, ...rest } }) => {
                const formattedDate = value ? moment(new Date(value)).format('D.M.yyyy') : '';
                return <DatePickerInput value={formattedDate} error={!!props.validationErrors[props.name]} {...rest} />;
            }}
        />
    );
}
