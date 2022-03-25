import React from 'react';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { LocalDate } from '../../../types/types';
import { Control } from 'react-hook-form/dist/types/form';
import DatePickerController from '../../Controllers/DatePickerController';
import { BodyKehys, BodyKentta } from '../ModalFields/ModalFields';

type TLProps = {
    validationErrors: FieldErrors<{ date: LocalDate }>;
    control: Control<{ date: LocalDate }>;
};
export default function TLBody({ validationErrors, control }: TLProps) {
    return (
        <BodyKehys>
            <BodyKentta label={'TOIMIPISTEEN_LAKKAUTUS_PVM'}>
                <DatePickerController<{ date: LocalDate }>
                    form={control}
                    validationErrors={validationErrors}
                    name={'date'}
                />
            </BodyKentta>
        </BodyKehys>
    );
}
