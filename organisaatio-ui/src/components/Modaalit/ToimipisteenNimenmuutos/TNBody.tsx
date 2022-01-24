import React from 'react';
import Radio from '@opetushallitus/virkailija-ui-components/Radio';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import { NimenmuutosLomake } from '../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormGetValues, UseFormRegister } from 'react-hook-form/dist/types/form';
import { BodyKentta, BodyRivi } from '../ModalFields/ModalFields';
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
    isLoading: boolean;
};

export default function TNBody(props: TNProps) {
    const { validationErrors, register, formControl, getValues, isLoading } = props;
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
        <>
            <BodyRivi>
                <BodyKentta>
                    <Controller
                        control={formControl}
                        name={'muutostyyppi'}
                        render={({ field: { ref, value = 'CREATE', ...rest } }) => (
                            <RadioGroup {...rest} value={value}>
                                <Radio name={MUUTOSTYYPPI_CREATE} value={MUUTOSTYYPPI_CREATE}>
                                    {i18n.translate('NIMENMUUTOS_RADIO_LUO_UUSI_NIMI_JAA_HISTORIAAN')}
                                </Radio>
                                <Radio name={MUUTOSTYYPPI_EDIT} value={MUUTOSTYYPPI_EDIT}>
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
            <BodyRivi>
                <NimenMuutosFields
                    edit={muutostyyppi === MUUTOSTYYPPI_EDIT}
                    validationErrors={validationErrors}
                    formControl={formControl}
                    register={register}
                />
            </BodyRivi>
        </>
    );
}
