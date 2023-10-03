import PohjaModaali from '../PohjaModaali/PohjaModaali';
import * as React from 'react';
import { useForm } from 'react-hook-form';
import { LocalDate } from '../../../types/types';
import Header from '../Header/Header';
import TLBody from './TLBody';
import Footer from '../Footer/Footer';

type ModaaliProps = {
    date?: LocalDate;
    closeModaali: () => void;
    handleTallennus: (date: LocalDate) => void;
};
export default function ToimipisteenLakkautus(props: ModaaliProps) {
    const {
        reset,
        getValues,
        control,
        formState: { errors: validationErrors },
        handleSubmit,
    } = useForm<{ date: LocalDate }>({ defaultValues: { date: props.date || '' } });

    const handleTallenna = () => {
        const { date } = getValues();
        props.handleTallennus(date);
        return props.closeModaali();
    };
    const handlePeruuta = () => {
        reset();
        props.closeModaali();
    };
    return (
        <PohjaModaali
            header={<Header label={'TOIMIPISTEEN_LAKKAUTUS_TITLE'} />}
            body={<TLBody validationErrors={validationErrors} control={control} />}
            footer={<Footer tallennaCallback={handleSubmit(handleTallenna)} peruutaCallback={handlePeruuta} />}
            suljeCallback={handlePeruuta}
        />
    );
}
