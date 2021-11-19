import { Control, Controller } from 'react-hook-form';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import moment from 'moment';
import * as React from 'react';
import { Perustiedot } from '../../types/types';

export default function DatePickerController(props: {
    form: Control<Perustiedot> | undefined;
    validationErrors: { [x: string]: any };
    name: 'alkuPvm';
}) {
    return (
        <Controller
            control={props.form}
            name={props.name}
            render={({ field: { ref, value, ...rest } }) => {
                return (
                    <DatePickerInput
                        value={moment(new Date(value)).format('D.M.yyyy')}
                        error={!!props.validationErrors[props.name]}
                        {...rest}
                    />
                );
            }}
        />
    );
}
