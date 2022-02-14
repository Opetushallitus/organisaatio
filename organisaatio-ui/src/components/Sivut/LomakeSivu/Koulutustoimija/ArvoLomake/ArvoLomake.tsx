import * as React from 'react';
import { Kentta, Rivi, UloinKehys } from '../../LomakeFields/LomakeFields';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import { UseFormRegister } from 'react-hook-form/dist/types/form';
import { YhteystietoArvot } from '../../../../../types/types';

type Props = {
    tyyppiOid: string;
    yhteystietoArvoRegister: UseFormRegister<YhteystietoArvot>;
    readOnly: boolean;
    field: 'koskiposti';
};
export default function ArvoLomake({ yhteystietoArvoRegister, readOnly, field }: Props) {
    return (
        <UloinKehys>
            <Rivi>
                <Rivi>
                    <Kentta label={'YHTEYSTIEDOT_SAHKOPOSTIOSOITE_fi'}>
                        <Input
                            disabled={readOnly}
                            id={'koskiposti.fi'}
                            {...yhteystietoArvoRegister(`${field}.fi` as const)}
                        />
                    </Kentta>
                </Rivi>
                <Rivi>
                    <Kentta label={'YHTEYSTIEDOT_SAHKOPOSTIOSOITE_sv'}>
                        <Input
                            disabled={readOnly}
                            id={'koskiposti.sv'}
                            {...yhteystietoArvoRegister(`${field}.sv` as const)}
                        />
                    </Kentta>
                </Rivi>
                <Rivi>
                    <Kentta label={'YHTEYSTIEDOT_SAHKOPOSTIOSOITE_en'}>
                        <Input
                            disabled={readOnly}
                            id={'koskiposti.en'}
                            {...yhteystietoArvoRegister(`${field}.en` as const)}
                        />
                    </Kentta>
                </Rivi>
            </Rivi>
        </UloinKehys>
    );
}
