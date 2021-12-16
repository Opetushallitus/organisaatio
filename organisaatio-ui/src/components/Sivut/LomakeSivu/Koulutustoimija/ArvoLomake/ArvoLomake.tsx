import * as React from 'react';
import { Kentta, Rivi, UloinKehys } from '../../LomakeFields/LomakeFields';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import { UseFormRegister } from 'react-hook-form/dist/types/form';
import { YhteystietoArvot } from '../../../../../types/types';

type Props = {
    tyyppiOid: string;
    yhteystietoArvoRegister: UseFormRegister<YhteystietoArvot>;
};
export default function ArvoLomake({ yhteystietoArvoRegister }: Props) {
    return (
        <UloinKehys>
            <Rivi>
                <Rivi>
                    <Kentta label={'YHTEYSTIEDOT_SAHKOPOSTIOSOITE_fi'}>
                        <Input id={'koskiposti.fi'} {...yhteystietoArvoRegister('koskiposti.fi')} />
                    </Kentta>
                </Rivi>
                <Rivi>
                    <Kentta label={'YHTEYSTIEDOT_SAHKOPOSTIOSOITE_sv'}>
                        <Input id={'koskiposti.sv'} {...yhteystietoArvoRegister('koskiposti.sv')} />
                    </Kentta>
                </Rivi>
                <Rivi>
                    <Kentta label={'YHTEYSTIEDOT_SAHKOPOSTIOSOITE_en'}>
                        <Input id={'koskiposti.en'} {...yhteystietoArvoRegister('koskiposti.en')} />
                    </Kentta>
                </Rivi>
            </Rivi>
        </UloinKehys>
    );
}
