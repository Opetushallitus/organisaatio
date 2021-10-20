import PohjaModaali from '../PohjaModaali/PohjaModaali';
import TNHeader from './TNHeader';
import TNBody from './TNBody';
import TNFooter from './TNFooter';
import * as React from 'react';
import { useForm } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import { Nimi } from '../../../types/types';
import ToimipisteenNimenmuutosModaaliSchema from '../../../ValidationSchemas/ToimipisteenNimenmuutosModaaliSchema';

type ModaaliProps = {
    nimi: Nimi;
    closeNimenmuutosModaali: () => void;
    handleNimiTallennus: (Nimi) => void;
};
export default function ToimipisteenNimenmuutosModaali(props: ModaaliProps) {
    const {
        reset,
        getValues,
        register,
        formState: { errors: validationErrors },
        handleSubmit,
    } = useForm<Nimi>({ defaultValues: props.nimi, resolver: joiResolver(ToimipisteenNimenmuutosModaaliSchema) });

    const handleTallenna = () => {
        const nimi = getValues();
        props.handleNimiTallennus(nimi);
        return props.closeNimenmuutosModaali();
    };
    const handlePeruuta = () => {
        reset();
        props.closeNimenmuutosModaali();
    };
    return (
        <PohjaModaali
            header={<TNHeader />}
            body={<TNBody validationErrors={validationErrors} register={register} />}
            footer={<TNFooter tallennaCallback={handleSubmit(handleTallenna)} peruutaCallback={handlePeruuta} />}
            suljeCallback={handlePeruuta}
        />
    );
}
