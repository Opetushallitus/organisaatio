import React from 'react';
import { BodyKentta, BodyRivi } from '../ModalFields/ModalFields';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { NimenmuutosLomake } from '../../../types/types';
import { Control, UseFormGetValues, UseFormRegister, UseFormSetValue } from 'react-hook-form/dist/types/form';
import DatePickerController from '../../Controllers/DatePickerController';
import { NimiGroup } from '../../Sivut/LomakeSivu/LomakeFields/LomakeFields';

type UusiNimiProps = {
    validationErrors: FieldErrors<NimenmuutosLomake>;
    register: UseFormRegister<NimenmuutosLomake>;
    edit: boolean;
    disabled?: boolean;
    formControl: Control<NimenmuutosLomake>;
    getValues: UseFormGetValues<NimenmuutosLomake>;
    setValue: UseFormSetValue<NimenmuutosLomake>;
};

export default function NimenMuutosFields({
    validationErrors,
    register,
    edit,
    formControl,
    getValues,
    setValue,
}: UusiNimiProps) {
    return (
        <BodyRivi>
            <BodyKentta>
                <NimiGroup
                    error={validationErrors.nimi}
                    register={register}
                    getValues={getValues}
                    setValue={setValue}
                />
            </BodyKentta>
            {!edit && (
                <BodyKentta isRequired label={'LABEL_NIMI_ALKUPVM'}>
                    <DatePickerController<NimenmuutosLomake>
                        name={'alkuPvm'}
                        form={formControl}
                        validationErrors={validationErrors}
                    />
                </BodyKentta>
            )}
        </BodyRivi>
    );
}
