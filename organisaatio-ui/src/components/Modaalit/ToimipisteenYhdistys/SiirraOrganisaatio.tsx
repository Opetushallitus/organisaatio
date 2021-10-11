import PohjaModaali from '../PohjaModaali/PohjaModaali';
import TYHeader from './TYHeader';
import TYFooter from './TYFooter';
import * as React from 'react';
import { useContext, useState } from 'react';
import TSBody from './TSBody';
import { Confirmation } from '../Confirmation/Confirmation';
import { Organisaatio, ResolvedRakenne, SiirraOrganisaatioon } from '../../../types/types';
import { organisaatioNimiByLanguage } from '../../../tools/organisaatio';
import { LanguageContext } from '../../../contexts/contexts';

export function SiirraOrganisaatio(props: {
    siirraOrganisaatio: SiirraOrganisaatioon;
    organisaatio: Organisaatio;

    handleChange: (value: ((prevState: SiirraOrganisaatioon) => SiirraOrganisaatioon) | SiirraOrganisaatioon) => void;
    organisaatioRakenne: ResolvedRakenne;
    tallennaCallback: () => void;
    peruutaCallback: () => void;
    suljeCallback: () => void;
}) {
    const { language } = useContext(LanguageContext);
    const [confirmationModaaliAuki, setConfirmationModaaliAuki] = useState<boolean>(false);
    return (
        <>
            {!confirmationModaaliAuki && (
                <PohjaModaali
                    header={<TYHeader titleKey={'TOIMIPISTEEN_SIIRTO_TITLE'} />}
                    body={
                        <TSBody
                            organisaatio={props.organisaatio}
                            siirraOrganisaatio={props.siirraOrganisaatio}
                            handleChange={props.handleChange}
                            organisaatioRakenne={props.organisaatioRakenne}
                        />
                    }
                    footer={
                        <TYFooter
                            tallennaCallback={() => setConfirmationModaaliAuki(true)}
                            peruutaCallback={props.peruutaCallback}
                        />
                    }
                    suljeCallback={props.suljeCallback}
                />
            )}
            {confirmationModaaliAuki && (
                <Confirmation
                    header={'TOIMIPISTEEN_SIIRTO_TITLE'}
                    message={'TOIMIPISTEEN_SIIRTO_VAHVISTUS_{from}_TO_{to}'}
                    replacements={[
                        {
                            key: 'from',
                            value: `${organisaatioNimiByLanguage(props.organisaatio, language)} (${
                                props.organisaatio.oid
                            })`,
                        },
                        {
                            key: 'to',
                            value: `${organisaatioNimiByLanguage(props.siirraOrganisaatio.newParent, language)} (${
                                props.siirraOrganisaatio.newParent?.oid || ''
                            })`,
                        },
                    ]}
                    tallennaCallback={() => {
                        setConfirmationModaaliAuki(false);
                        props.tallennaCallback();
                    }}
                    peruutaCallback={() => setConfirmationModaaliAuki(false)}
                    suljeCallback={() => setConfirmationModaaliAuki(false)}
                />
            )}
        </>
    );
}
