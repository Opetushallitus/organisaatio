import PohjaModaali from '../PohjaModaali/PohjaModaali';
import TYHeader from './TYHeader';
import TYFooter from './TYFooter';
import * as React from 'react';
import { useContext, useState } from 'react';
import TSBody from './TSBody';
import { Confirmation } from '../Confirmation/Confirmation';
import { ResolvedRakenne, SiirraOrganisaatioon, UiOrganisaatioBase } from '../../../types/types';
import { LanguageContext } from '../../../contexts/contexts';

export function SiirraOrganisaatio(props: {
    siirraOrganisaatio: SiirraOrganisaatioon;
    organisaatioBase: UiOrganisaatioBase;
    handleChange: (value: ((prevState: SiirraOrganisaatioon) => SiirraOrganisaatioon) | SiirraOrganisaatioon) => void;
    organisaatioRakenne: ResolvedRakenne;
    tallennaCallback: () => void;
    peruutaCallback: () => void;
    suljeCallback: () => void;
}) {
    const { i18n } = useContext(LanguageContext);
    const [confirmationModaaliAuki, setConfirmationModaaliAuki] = useState<boolean>(false);
    return (
        <>
            {!confirmationModaaliAuki && (
                <PohjaModaali
                    header={<TYHeader titleKey={'TOIMIPISTEEN_SIIRTO_TITLE'} />}
                    body={
                        <TSBody
                            organisaatioBase={props.organisaatioBase}
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
                            value: `${i18n.translateNimi(props.organisaatioBase.currentNimi)} (${
                                props.organisaatioBase.oid
                            })`,
                        },
                        {
                            key: 'to',
                            value: `${i18n.translateNimi(props.siirraOrganisaatio.newParent?.nimi)} (${
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