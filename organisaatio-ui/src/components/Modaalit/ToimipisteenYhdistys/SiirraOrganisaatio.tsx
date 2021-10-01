import PohjaModaali from '../PohjaModaali/PohjaModaali';
import TYHeader from './TYHeader';
import TYFooter from './TYFooter';
import * as React from 'react';
import { useState } from 'react';
import TSBody from './TSBody';
import { Confirmation } from '../Confirmation/Confirmation';
import { Organisaatio, ResolvedRakenne, SiirraOrganisaatioon, YhdistaOrganisaatioon } from '../../../types/types';

export function SiirraOrganisaatio(props: {
    siirraOrganisaatio: SiirraOrganisaatioon;
    organisaatio: Organisaatio;

    handleChange: (
        value: ((prevState: YhdistaOrganisaatioon) => YhdistaOrganisaatioon) | YhdistaOrganisaatioon
    ) => void;
    organisaatioRakenne: ResolvedRakenne;
    tallennaCallback: () => void;
    peruutaCallback: () => void;
    suljeCallback: () => void;
}) {
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
                    message={'TOIMIPISTEEN_SIIRTO_VAHVISTUS'}
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
