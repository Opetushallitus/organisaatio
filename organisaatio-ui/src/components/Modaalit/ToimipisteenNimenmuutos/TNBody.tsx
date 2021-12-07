import React, { useContext } from 'react';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Radio from '@opetushallitus/virkailija-ui-components/Radio';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import { Nimi } from '../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormRegister } from 'react-hook-form/dist/types/form';
import { BodyKehys, BodyKentta, BodyRivi } from '../ModalFields/ModalFields';
import { LanguageContext } from '../../../contexts/contexts';
import { Controller } from 'react-hook-form';

type TNProps = {
    validationErrors: FieldErrors<Nimi>;
    register: UseFormRegister<Nimi>;
    formControl: Control<Nimi>;
};

export default function TNBody(props: TNProps) {
    const { validationErrors, register, formControl } = props;
    console.log('ve', validationErrors);
    const { i18n } = useContext(LanguageContext);
    return (
        <BodyKehys>
            <BodyRivi>
                <BodyKentta>
                    <Controller
                        control={formControl}
                        defaultValue="CREATE"
                        name={'muutostyyppi'}
                        render={({ field: { ref, value, ...rest } }) => (
                            <RadioGroup {...rest} value={value || 'CREATE'}>
                                <Radio value="CREATE">
                                    {i18n.translate('NIMENMUUTOS_RADIO_LUO_UUSI_NIMI_JAA_HISTORIAAN')}
                                </Radio>
                                <Radio value="EDIT">
                                    {i18n.translate('NIMENMUUTOS_RADIO_LUO_UUSI_NIMI_EI_HISTORIAAN')}
                                </Radio>
                                <Radio value="CANCEL">
                                    {i18n.translate('NIMENMUUTOS_PERUUTA_AJASTETTU_NIMENMUUTOS')}
                                </Radio>
                            </RadioGroup>
                        )}
                    />
                </BodyKentta>
            </BodyRivi>
            <BodyRivi>
                <BodyKentta>
                    <BodyKentta isRequired label={'LABEL_SUOMEKSI'}>
                        <Input error={!!validationErrors['fi']} id={'organisaation_nimiFi'} {...register('fi')} />
                    </BodyKentta>
                    <BodyKentta isRequired label={'LABEL_RUOTSIKSI'}>
                        <Input error={!!validationErrors['sv']} id={'organisaation_nimiSv'} {...register('sv')} />
                    </BodyKentta>
                    <BodyKentta isRequired label={'LABEL_ENGLANNIKSI'}>
                        <Input error={!!validationErrors['en']} id={'organisaation_nimiEn'} {...register('en')} />
                    </BodyKentta>
                </BodyKentta>
            </BodyRivi>
        </BodyKehys>
    );
}
