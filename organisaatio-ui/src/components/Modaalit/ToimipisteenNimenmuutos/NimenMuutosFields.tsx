import React from 'react';
import { BodyKentta, BodyRivi } from '../ModalFields/ModalFields';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { NimenmuutosLomake, Nimi } from '../../../types/types';
import { Control, UseFormGetValues, UseFormRegister, UseFormSetValue } from 'react-hook-form/dist/types/form';
import DatePickerController from '../../Controllers/DatePickerController';
import { NimiKentta } from '../../Sivut/LomakeSivu/LomakeFields/LomakeFields';
import { Path } from 'react-hook-form';

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
    const copyToNames = (field: Path<Nimi>): void => {
        const muutosTiedot = getValues();
        setValue('nimi.sv', muutosTiedot.nimi?.[field]);
        setValue('nimi.en', muutosTiedot.nimi?.[field]);
    };
    return (
        <BodyRivi>
            <BodyKentta>
                <NimiKentta
                    label={'LABEL_SUOMEKSI'}
                    id={'organisaation_nimiFi'}
                    field={'fi'}
                    formRegisterReturn={register('nimi.fi')}
                    copyToNames={copyToNames}
                />
                <NimiKentta
                    label={'LABEL_RUOTSIKSI'}
                    id={'organisaation_nimiSv'}
                    field={'sv'}
                    formRegisterReturn={register('nimi.sv')}
                />
                <NimiKentta
                    label={'LABEL_ENGLANNIKSI'}
                    id={'organisaation_nimiEn'}
                    field={'en'}
                    formRegisterReturn={register('nimi.en')}
                />
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
