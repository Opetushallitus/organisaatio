import PohjaModaali from '../PohjaModaali/PohjaModaali';
import TNHeader from './TNHeader';
import TNBody from './TNBody';
import TNFooter from './TNFooter';
import * as React from 'react';
import { useForm } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import Joi from 'joi';
import { Nimi } from '../../../types/types';
import { Dispatch, SetStateAction } from 'react';

type ModaaliProps = {
    nimi: Nimi;
    setNimenmuutosModaaliAuki: Dispatch<SetStateAction<boolean>>;
    handleNimiTallennus: (Nimi) => void;
};

export const nimiModaaliSchema = Joi.object({
    nimiEn: Joi.string(),
    nimiFi: Joi.string(),
    nimiSv: Joi.string(),
})
    .when(Joi.object({ nimiFi: Joi.string().required() }).unknown(), {
        then: Joi.object({ nimiSv: Joi.string().allow(''), nimiEn: Joi.string().allow('') }),
    })
    .when(Joi.object({ nimiSv: Joi.string().required() }).unknown(), {
        then: Joi.object({ nimiFi: Joi.string().allow(''), nimiEn: Joi.string().allow('') }),
    })
    .when(Joi.object({ nimiEn: Joi.string().required() }).unknown(), {
        then: Joi.object({ nimiFi: Joi.string().allow(''), nimiSv: Joi.string().allow('') }),
    })
    .or('nimiFi', 'nimiSv', 'nimiEn');

export default function ToimipisteenNimenmuutosModaali(props: ModaaliProps) {
    const {
        reset,
        getValues,
        register,
        formState: { errors: validationErrors },
        handleSubmit,
    } = useForm({ resolver: joiResolver(nimiModaaliSchema) });

    const handleTallenna = () => {
        const { nimiFi: fi, nimiSv: sv, nimiEn: en } = getValues();
        props.handleNimiTallennus({ fi, sv, en });
        return props.setNimenmuutosModaaliAuki(false);
    };
    const handlePeruuta = () => {
        reset();
        props.setNimenmuutosModaaliAuki(false);
    };

    return (
        <PohjaModaali
            header={<TNHeader />}
            body={
                <TNBody
                    validationErrors={validationErrors}
                    register={register}
                    nimi={props.nimi}
                    //handleChange={setNimi}
                />
            }
            footer={<TNFooter tallennaCallback={handleSubmit(handleTallenna)} peruutaCallback={handlePeruuta} />}
            suljeCallback={handlePeruuta}
        />
    );
}
