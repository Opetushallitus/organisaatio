import React from 'react';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import { Nimi } from '../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { UseFormRegister } from 'react-hook-form/dist/types/form';
import { BodyKehys, BodyKentta } from '../ModalFields/ModalFields';

type TNProps = {
    validationErrors: FieldErrors<Nimi>;
    register: UseFormRegister<Nimi>;
};

export default function TNBody(props: TNProps) {
    const { validationErrors, register } = props;
    return (
        <BodyKehys>
            <BodyKentta>
                <BodyKentta label={'LABEL_SUOMEKSI'}>
                    <Input error={!!validationErrors['fi']} id={'organisaation_nimiFi'} {...register('fi')} />
                </BodyKentta>
                <BodyKentta label={'LABEL_RUOTSIKSI'}>
                    <Input error={!!validationErrors['sv']} id={'organisaation_nimiSv'} {...register('sv')} />
                </BodyKentta>
                <BodyKentta label={'LABEL_ENGLANNIKSI'}>
                    <Input error={!!validationErrors['en']} id={'organisaation_nimiEn'} {...register('en')} />
                </BodyKentta>
            </BodyKentta>
        </BodyKehys>
    );
}
