import PohjaModaali from '../PohjaModaali/PohjaModaali';
import TNBody from './TNBody';
import * as React from 'react';
import { useForm } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import { LocalDate, NimenmuutosLomake, OrganisaationNimetNimi } from '../../../types/types';
import ToimipisteenNimenmuutosModaaliSchema from '../../../ValidationSchemas/ToimipisteenNimenmuutosModaaliSchema';
import Header from '../Header/Header';
import Footer from '../Confirmation/Footer';
import { useState } from 'react';
import { createOrganisaatioNimi, updateOrganisaatioNimi } from '../../../api/organisaatio';
import { MUUTOSTYYPPI_CREATE, MUUTOSTYYPPI_EDIT } from './constants';

type ModaaliProps = {
    oid: string;
    currentNimi?: OrganisaationNimetNimi & { disabled: boolean | undefined };
    closeNimenmuutosModaali: (nimiIsUpdated: boolean) => void;
};

export default function ToimipisteenNimenmuutosModaali(props: ModaaliProps) {
    const { currentNimi, oid } = props;
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const {
        reset,
        getValues,
        register,
        formState: { errors: validationErrors },
        handleSubmit,
        control: formControl,
        watch,
    } = useForm<NimenmuutosLomake>({
        defaultValues: {
            nimi: { fi: '', sv: '', en: '' },
            alkuPvm: new Date().toISOString().split('T')[0] as LocalDate,
            muutostyyppi: MUUTOSTYYPPI_CREATE,
            oid,
            editDisabled: currentNimi?.disabled,
        },
        resolver: joiResolver(ToimipisteenNimenmuutosModaaliSchema),
    });

    React.useEffect(() => {
        const subscription = watch((value, { name }) => {
            if (name === 'muutostyyppi') {
                const { muutostyyppi, editDisabled } = value;
                switch (muutostyyppi) {
                    case MUUTOSTYYPPI_CREATE:
                        reset({
                            nimi: { fi: '', sv: '', en: '' },
                            alkuPvm: new Date().toISOString().split('T')[0] as LocalDate,
                            oid,
                            muutostyyppi,
                        });
                        return;
                    case MUUTOSTYYPPI_EDIT:
                        reset({
                            nimi: currentNimi?.nimi,
                            alkuPvm: currentNimi?.alkuPvm,
                            muutostyyppi,
                            oid,
                            editDisabled,
                        });
                        return;
                }
            }
        });
        return () => {
            return subscription.unsubscribe();
        };
    }, [watch]);

    watch('muutostyyppi', MUUTOSTYYPPI_CREATE);

    const handleTallenna = async () => {
        setIsLoading(true);
        try {
            const { muutostyyppi, nimi, alkuPvm, oid } = getValues();
            const newNimi = { nimi, alkuPvm };
            if (muutostyyppi === MUUTOSTYYPPI_CREATE) {
                await createOrganisaatioNimi(oid, newNimi);
            } else if (muutostyyppi === MUUTOSTYYPPI_EDIT && currentNimi) {
                await updateOrganisaatioNimi(oid, currentNimi, newNimi);
            }
            props.closeNimenmuutosModaali(true);
        } finally {
            setIsLoading(false);
        }
    };
    const handlePeruuta = () => {
        reset();
        props.closeNimenmuutosModaali(false);
    };

    return (
        <PohjaModaali
            header={<Header label={'TOIMIPISTEEN_NIMENMUUTOS_TITLE'} />}
            body={
                <TNBody
                    isLoading={isLoading}
                    getValues={getValues}
                    validationErrors={validationErrors}
                    register={register}
                    formControl={formControl}
                />
            }
            footer={<Footer tallennaCallback={handleSubmit(handleTallenna)} peruutaCallback={handlePeruuta} />}
            suljeCallback={handlePeruuta}
        />
    );
}
