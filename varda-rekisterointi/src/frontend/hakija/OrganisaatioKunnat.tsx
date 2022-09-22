import React, { useContext } from 'react';
import { LanguageContext } from '../contexts';
import FormFieldContainer from '../FormFieldContainer';
import KoodiMultiSelect from '../KoodiMultiSelect';
import { Koodi } from '../types/types';

type Props = {
    id?: string;
    readOnly?: boolean;
    kaikkiKunnat: Koodi[];
    kunnat: string[];
    setKunnat: (kunnat: string[]) => void;
    errors: Record<string, string>;
};

export default function OrganisaatioKunnat({ readOnly, kaikkiKunnat, kunnat, setKunnat, errors }: Props) {
    const { i18n, language } = useContext(LanguageContext);

    return (
        <>
            <FormFieldContainer
                readOnly={readOnly}
                errorText={errors.kunnat}
                label={i18n.translate('ORGANISAATION_KUNNAT')}
                labelBy="organisaation-kunnat"
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                {!readOnly ? (
                    <KoodiMultiSelect
                        labelledBy="organisaation-kunnat"
                        selectable={kaikkiKunnat}
                        selected={kunnat}
                        disabled={readOnly}
                        onChange={setKunnat}
                    />
                ) : (
                    <div tabIndex={0} className="oph-input-container">
                        {kunnat
                            .map((k) => {
                                const kunta = kaikkiKunnat.find((kk) => kk.uri === k);
                                return kunta ? kunta.nimi[language] : k;
                            })
                            .join(', ')}
                    </div>
                )}
            </FormFieldContainer>
        </>
    );
}
