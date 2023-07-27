import * as React from 'react';
import { Kentta, Rivi, UloinKehys } from '../../LomakeFields/LomakeFields';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import { UseFormRegister } from 'react-hook-form/dist/types/form';
import { YhteystietoArvot } from '../../../../../types/types';

type Props = {
    tyyppiOid: string;
    yhteystietoArvoRegister: UseFormRegister<YhteystietoArvot>;
    readOnly: boolean;
};
export default function KoskiPostiLomake({ yhteystietoArvoRegister, readOnly }: Props) {
    return (
        <UloinKehys>
            <Rivi>
                <Rivi>
                    <Kentta label={'YHTEYSTIEDOT_SAHKOPOSTIOSOITE_fi'}>
                        <Input
                            disabled={readOnly}
                            id={`koskiposti.fi`}
                            {...yhteystietoArvoRegister(`koskiposti.fi` as const)}
                        />
                    </Kentta>
                </Rivi>
                <Rivi>
                    <Kentta label={'YHTEYSTIEDOT_SAHKOPOSTIOSOITE_sv'}>
                        <Input
                            disabled={readOnly}
                            id={`koskiposti.sv`}
                            {...yhteystietoArvoRegister(`koskiposti.sv` as const)}
                        />
                    </Kentta>
                </Rivi>
                <Rivi>
                    <Kentta label={'YHTEYSTIEDOT_SAHKOPOSTIOSOITE_en'}>
                        <Input
                            disabled={readOnly}
                            id={`koskiposti.en`}
                            {...yhteystietoArvoRegister(`koskiposti.en` as const)}
                        />
                    </Kentta>
                </Rivi>
            </Rivi>
        </UloinKehys>
    );
}
