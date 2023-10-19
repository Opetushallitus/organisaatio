import PohjaModaali from '../PohjaModaali/PohjaModaali';
import React, { useState } from 'react';
import { LocalDate } from '../../../types/types';
import Header from '../Header/Header';
import TLBody from './TLBody';
import Footer from '../Footer/Footer';

type ModaaliProps = {
    date?: LocalDate;
    closeModaali: () => void;
    handleTallennus: (date?: Date) => void;
};

export default function ToimipisteenLakkautus(props: ModaaliProps) {
    const [date, setDate] = useState<Date>();
    const handleTallenna = () => {
        props.handleTallennus(date);
        props.closeModaali();
    };

    return (
        <PohjaModaali
            header={<Header label={'TOIMIPISTEEN_LAKKAUTUS_TITLE'} />}
            body={<TLBody date={props.date} setDate={setDate} />}
            footer={<Footer tallennaCallback={handleTallenna} peruutaCallback={props.closeModaali} />}
            suljeCallback={props.closeModaali}
        />
    );
}
