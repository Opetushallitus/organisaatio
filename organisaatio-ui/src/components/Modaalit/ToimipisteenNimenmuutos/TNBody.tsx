import React from 'react';
import Radio from '@opetushallitus/virkailija-ui-components/Radio';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import { NimenmuutosLomake } from '../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormGetValues, UseFormRegister, UseFormSetValue } from 'react-hook-form/dist/types/form';
import { BodyKehys, BodyKentta, BodyRivi } from '../ModalFields/ModalFields';
import { Controller } from 'react-hook-form';
import NimenMuutosFields from './NimenMuutosFields';
import { MUUTOSTYYPPI_CREATE, MUUTOSTYYPPI_EDIT } from './constants';
import Loading from '../../Loading/Loading';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../api/lokalisaatio';

type TNProps = {
    validationErrors: FieldErrors<NimenmuutosLomake>;
    register: UseFormRegister<NimenmuutosLomake>;
    formControl: Control<NimenmuutosLomake>;
    getValues: UseFormGetValues<NimenmuutosLomake>;
    setValue: UseFormSetValue<NimenmuutosLomake>;
    isLoading: boolean;
};

export default function TNBody(props: TNProps) {
    const { validationErrors, register, formControl, getValues, setValue, isLoading } = props;
    const [i18n] = useAtom(languageAtom);
    if (isLoading) {
        return (
            <BodyRivi>
                <BodyKentta>
                    <Loading />
                </BodyKentta>
            </BodyRivi>
        );
    }
    const { muutostyyppi, foundAmatch } = getValues();
    return (
        <BodyKehys>
            <BodyRivi>
                <BodyKentta>
                    <Controller
                        control={formControl}
                        name={'muutostyyppi'}
                        render={({ field: { ref, value = 'CREATE', ...rest } }) => (
                            <RadioGroup {...rest} value={value}>
                                <Radio
                                    name={MUUTOSTYYPPI_CREATE}
                                    value={MUUTOSTYYPPI_CREATE}
                                    data-testid="CREATE_NAME_CHANGE"
                                >
                                    {i18n.translate('NIMENMUUTOS_RADIO_LUO_UUSI_NIMI_JAA_HISTORIAAN')}
                                </Radio>
                                <Radio
                                    name={MUUTOSTYYPPI_EDIT}
                                    value={MUUTOSTYYPPI_EDIT}
                                    data-testid="EDIT_NAME_CHANGE"
                                >
                                    {i18n.translate('NIMENMUUTOS_RADIO_LUO_UUSI_NIMI_EI_HISTORIAAN')}
                                </Radio>
                            </RadioGroup>
                        )}
                    />
                </BodyKentta>
            </BodyRivi>
            {foundAmatch && (
                <BodyRivi>
                    <BodyKentta>
                        <span style={{ color: '#e44e4e' }}>
                            {i18n.translate('NIMENMUUTOS_MUOKKAUS_FOUND_NAME_FOR_DATE')}
                        </span>
                    </BodyKentta>
                </BodyRivi>
            )}

            <NimenMuutosFields
                edit={muutostyyppi === MUUTOSTYYPPI_EDIT}
                validationErrors={validationErrors}
                formControl={formControl}
                register={register}
                getValues={getValues}
                setValue={setValue}
            />
        </BodyKehys>
    );
}
