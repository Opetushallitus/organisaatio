import React, { useContext } from 'react';
import FormFieldContainer from '../../FormFieldContainer';
import { Organisaatio} from '../../types';
import { LanguageContext } from '../../contexts';

type Props = {
    organisaatio: Organisaatio,
}

export default function OrganisaationTiedot({ organisaatio }: Props) {
    const { i18n } = useContext(LanguageContext);
    return (
        <>
            <FormFieldContainer label={i18n.translate('ORGANISAATION_NIMI')}>
                <span>{organisaatio.ytjNimi.nimi}</span>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('YTUNNUS')}>
                <span>{organisaatio.ytunnus}s</span>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('YRITYSMUOTO')}>
                <span>{organisaatio.yritysmuoto}</span>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('KOTIPAIKKA')}>
                <span>{organisaatio.kotipaikkaUri}</span>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('TOIMINNAN_ALKAMISAIKA')}>
                <span>{organisaatio.alkuPvm}</span>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('OPETUSKIELI')}>
                <span>{organisaatio.kieletUris}</span>
            </FormFieldContainer>
        </>
    );
}
