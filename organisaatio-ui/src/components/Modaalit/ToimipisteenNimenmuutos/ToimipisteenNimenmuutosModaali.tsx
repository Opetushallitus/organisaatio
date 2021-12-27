import PohjaModaali from '../PohjaModaali/PohjaModaali';
import TNBody from './TNBody';
import * as React from 'react';
import { useForm } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import { NimenmuutosLomake, UiOrganisaationNimetNimi } from '../../../types/types';
import ToimipisteenNimenmuutosModaaliSchema from '../../../ValidationSchemas/ToimipisteenNimenmuutosModaaliSchema';
import Header from '../Header/Header';
import Footer from '../Confirmation/Footer';
import { useState } from 'react';
import { createOrganisaatioNimi, updateOrganisaatioNimi } from '../../../api/organisaatio';
import { MUUTOSTYYPPI_CREATE, MUUTOSTYYPPI_EDIT } from './constants';
import { getUiDateStr } from '../../../tools/mappers';

type ModaaliProps = {
    oid: string;
    currentNimi?: UiOrganisaationNimetNimi & { disabled: boolean | undefined };
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
            alkuPvm: getUiDateStr(),
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
                            alkuPvm: getUiDateStr(),
                            oid,
                            muutostyyppi,
                        });
                        return;
                    case MUUTOSTYYPPI_EDIT:
                        reset({
                            nimi: currentNimi?.nimi,
                            alkuPvm: getUiDateStr(currentNimi?.alkuPvm),
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
    }, [watch, currentNimi, oid, reset]);

    watch('muutostyyppi', MUUTOSTYYPPI_CREATE);

    const handleTallenna = async () => {
        setIsLoading(true);
        try {
            const { muutostyyppi, nimi, alkuPvm: newAlkuPvm, oid } = getValues();
            const newNimi = { nimi, alkuPvm: newAlkuPvm };
            if (muutostyyppi === MUUTOSTYYPPI_CREATE) {
                await createOrganisaatioNimi(oid, newNimi);
            } else if (muutostyyppi === MUUTOSTYYPPI_EDIT && currentNimi) {
                const { nimi, alkuPvm } = currentNimi;
                const oldNimi = { nimi, alkuPvm };
                await updateOrganisaatioNimi(oid, oldNimi, newNimi);
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
