import { ResolvedRakenne, UiOrganisaatioBase, YhdistaOrganisaatioon } from '../../../types/types';
import PohjaModaali from '../PohjaModaali/PohjaModaali';
import TYHeader from './TYHeader';
import TYBody from './TYBody';
import TYFooter from './TYFooter';
import * as React from 'react';
import { Confirmation } from '../Confirmation/Confirmation';
import { useContext, useState } from 'react';
import { LanguageContext } from '../../../contexts/contexts';

export function YhdistaOrganisaatio(props: {
    yhdistaOrganisaatio: YhdistaOrganisaatioon;
    organisaatioBase: UiOrganisaatioBase;
    handleChange: (
        value: ((prevState: YhdistaOrganisaatioon) => YhdistaOrganisaatioon) | YhdistaOrganisaatioon
    ) => void;
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
                    header={<TYHeader titleKey={'TOIMIPISTEEN_YHDISTYS_TITLE'} />}
                    body={
                        <TYBody
                            organisaatioBase={props.organisaatioBase}
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
                    message={'TOIMIPISTEEN_YHDISTYS_VAHVISTUS_{from}_TO_{to}'}
                    replacements={[
                        {
                            key: 'from',
                            value: `${i18n.translateNimi(props.organisaatioBase.currentNimi)} (${
                                props.organisaatioBase.oid
                            })`,
                        },
                        {
                            key: 'to',
                            value: `${i18n.translateNimi(props.yhdistaOrganisaatio.newParent?.nimi)} (${
                                props.yhdistaOrganisaatio.newParent?.oid || ''
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
