import React from 'react';
import { BodyKentta, BodyRivi } from '../ModalFields/ModalFields';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { NimenmuutosLomake } from '../../../types/types';
import { Control, UseFormRegister } from 'react-hook-form/dist/types/form';
import DatePickerController from '../../Controllers/DatePickerController';

type UusiNimiProps = {
    validationErrors: FieldErrors<NimenmuutosLomake>;
    register: UseFormRegister<NimenmuutosLomake>;
    edit: boolean;
    disabled?: boolean;
    formControl: Control<NimenmuutosLomake>;
};

export default function NimenMuutosFields(props: UusiNimiProps) {
    const { validationErrors, register, edit, formControl } = props;
    return (
        <BodyRivi>
            <BodyKentta>
                <BodyKentta isRequired label={'LABEL_SUOMEKSI'}>
                    <Input
                        error={!!validationErrors['nimi']?.fi}
                        id={'organisaation_nimiFi'}
                        {...register('nimi.fi')}
                    />
                </BodyKentta>
                <BodyKentta isRequired label={'LABEL_RUOTSIKSI'}>
                    <Input
                        error={!!validationErrors['nimi']?.sv}
                        id={'organisaation_nimiSv'}
                        {...register('nimi.sv')}
                    />
                </BodyKentta>
                <BodyKentta isRequired label={'LABEL_ENGLANNIKSI'}>
                    <Input
                        error={!!validationErrors['nimi']?.en}
                        id={'organisaation_nimiEn'}
                        {...register('nimi.en')}
                    />
                </BodyKentta>
            </BodyKentta>
            {!edit && (
                <BodyRivi>
                    <BodyKentta isRequired label={'LABEL_NIMI_ALKUPVM'}>
                        <DatePickerController<NimenmuutosLomake>
                            name={'alkuPvm'}
                            form={formControl}
                            validationErrors={validationErrors}
                        />
                    </BodyKentta>
                </BodyRivi>
            )}
        </BodyRivi>
    );
}
