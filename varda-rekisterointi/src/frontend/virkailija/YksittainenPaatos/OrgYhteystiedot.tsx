import React, { useContext } from 'react';
import FormFieldContainer from '../../FormFieldContainer';
import { Yhteystiedot } from '../../types/types';
import { LanguageContext } from '../../contexts';

type Props = {
    yhteystiedot: Yhteystiedot;
};

export default function OrgYhteystiedot({ yhteystiedot }: Props) {
    const { i18n } = useContext(LanguageContext);
    return (
        <>
            <FormFieldContainer label={i18n.translate('PUHELINNUMERO')}>
                <span>{yhteystiedot.puhelinnumero}</span>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('ORGANISAATION_SAHKOPOSTI')}>
                <span>{yhteystiedot.sahkoposti}</span>
            </FormFieldContainer>
        </>
    );
}
