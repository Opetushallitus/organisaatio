import React from 'react';
import PohjaModaali from '../PohjaModaali/PohjaModaali';
import YTJHeader from './YTJHeader';
import YTJBody from './YTJBody';
import YTJFooter from './YTJFooter';
import { UseFormSetValue } from 'react-hook-form/dist/types/form';
import { Perustiedot, Yhteystiedot } from '../../../types/types';

type ModaaliProps = {
    ytunnus: string;
    suljeModaali: () => void;
    setters: { setPerustiedotValue: UseFormSetValue<Perustiedot>; setYhteystiedotValue: UseFormSetValue<Yhteystiedot> };
};
export default function YTJModaali({ ytunnus, suljeModaali, setters }: ModaaliProps) {
    return (
        <PohjaModaali
            header={<YTJHeader />}
            body={<YTJBody setters={setters} ytunnus={ytunnus} suljeModaali={suljeModaali} />}
            footer={<YTJFooter peruutaCallback={suljeModaali} />}
            suljeCallback={suljeModaali}
        />
    );
}
