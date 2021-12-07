import PohjaModaali from '../PohjaModaali/PohjaModaali';
import TNBody from './TNBody';
import * as React from 'react';
import { useForm } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import { Nimi } from '../../../types/types';
import ToimipisteenNimenmuutosModaaliSchema from '../../../ValidationSchemas/ToimipisteenNimenmuutosModaaliSchema';
import Header from '../Header/Header';
import Footer from '../Confirmation/Footer';

type ModaaliProps = {
    nimi: Nimi;
    closeNimenmuutosModaali: () => void;
    handleNimiTallennus: (Nimenmuutostyyppi, Nimi) => void;
};
export default function ToimipisteenNimenmuutosModaali(props: ModaaliProps) {
    const {
        reset,
        getValues,
        register,
        formState: { errors: validationErrors },
        handleSubmit,
        control: formControl,
    } = useForm<Nimi>({ defaultValues: props.nimi, resolver: joiResolver(ToimipisteenNimenmuutosModaaliSchema) });

    const handleTallenna = () => {
        const { muutostyyppi, ...rest } = getValues();
        props.handleNimiTallennus({ ...rest }, muutostyyppi);
        return props.closeNimenmuutosModaali();
    };
    const handlePeruuta = () => {
        reset();
        props.closeNimenmuutosModaali();
    };
    return (
        <PohjaModaali
            header={<Header label={'TOIMIPISTEEN_NIMENMUUTOS_TITLE'} />}
            body={<TNBody validationErrors={validationErrors} register={register} formControl={formControl} />}
            footer={<Footer tallennaCallback={handleSubmit(handleTallenna)} peruutaCallback={handlePeruuta} />}
            suljeCallback={handlePeruuta}
        />
    );
}
