import React from 'react';
import PohjaModaali from '../PohjaModaali/PohjaModaali';
import YTJBody from './YTJBody';
import { UseFormSetValue } from 'react-hook-form/dist/types/form';
import { Perustiedot, Yhteystiedot } from '../../../types/types';
import Footer from '../Footer/Footer';
import Header from '../Header/Header';

type ModaaliProps = {
    ytunnus: string;
    suljeModaali: () => void;
    setters: { setPerustiedotValue: UseFormSetValue<Perustiedot>; setYhteystiedotValue: UseFormSetValue<Yhteystiedot> };
};
export default function YTJModaali({ ytunnus, suljeModaali, setters }: ModaaliProps) {
    return (
        <PohjaModaali
            header={<Header label={'VALITSE_ORGANISAATIO'} />}
            body={<YTJBody setters={setters} ytunnus={ytunnus} suljeModaali={suljeModaali} />}
            footer={<Footer peruutaCallback={suljeModaali} />}
            suljeCallback={suljeModaali}
        />
    );
}
