import { Organisaatio, ResolvedRakenne, YhdistaOrganisaatioon } from '../../../types/types';
import PohjaModaali from '../PohjaModaali/PohjaModaali';
import TYHeader from './TYHeader';
import TYBody from './TYBody';
import TYFooter from './TYFooter';
import * as React from 'react';
import { Confirmation } from '../Confirmation/Confirmation';
import { useState } from 'react';

export function YhdistaOrganisaatio(props: {
    yhdistaOrganisaatio: YhdistaOrganisaatioon;
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
                    header={<TYHeader titleKey={'TOIMIPISTEEN_YHDISTYS_TITLE'} />}
                    body={
                        <TYBody
                            organisaatio={props.organisaatio}
                            yhdistaOrganisaatio={props.yhdistaOrganisaatio}
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
                    header={'TOIMIPISTEEN_YHDISTYS_TITLE'}
                    message={'TOIMIPISTEEN_YHDISTYS_VAHVISTUS'}
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
