import React, { useContext } from 'react';
import { LanguageContext } from '../contexts';
import FormFieldContainer from '../FormFieldContainer';
import InputMultiple from '../InputMultiple';

type Props = {
    readOnly?: boolean;
    sahkopostit: string[];
    setSahkopostit: (sahkopostit: string[]) => void;
    errors: Record<string, string>;
};

export default function OrganisaatioSahkopostit({ readOnly, sahkopostit, setSahkopostit, errors }: Props) {
    const { i18n } = useContext(LanguageContext);

    return (
        <>
            <FormFieldContainer
                readOnly={readOnly}
                errorText={errors.sahkopostit}
                label={i18n.translate('ORGANISAATION_SAHKOPOSTIT')}
                labelBy="organisaation-sahkopostit"
                helpText={i18n.translate('ORGANISAATION_SAHKOPOSTIT_OHJE')}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <InputMultiple
                    id="organisaation-sahkopostit"
                    values={sahkopostit}
                    disabled={readOnly}
                    hasError={!!errors.sahkopostit}
                    onChange={(sahkopostit) => setSahkopostit(sahkopostit)}
                />
            </FormFieldContainer>
        </>
    );
}
