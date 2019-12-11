import React, { useContext } from 'react';
import FormFieldContainer from '../../FormFieldContainer';
import { Kayttaja } from '../../types';
import { LanguageContext } from '../../contexts';

type Props = {
    kayttaja: Kayttaja,
}

export default function PaakayttajanTiedot({ kayttaja }: Props) {
    const { i18n } = useContext(LanguageContext);
    return (
        <>
            <FormFieldContainer label={i18n.translate('SAHKOPOSTI')}>
                <span>{kayttaja.sahkoposti}</span>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('ASIOINTIKIELI')}>
                <span>{kayttaja.asiointikieli}</span>
            </FormFieldContainer>
        </>
    );
}
