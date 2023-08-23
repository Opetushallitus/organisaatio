import PohjaModaali from '../PohjaModaali/PohjaModaali';
import TNBody from './TNBody';
import * as React from 'react';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import { LocalDate, NimenmuutosLomake, UiOrganisaationNimetNimi } from '../../../types/types';
import ToimipisteenNimenmuutosModaaliSchema from '../../../ValidationSchemas/ToimipisteenNimenmuutosModaaliSchema';
import Header from '../Header/Header';
import Footer from '../Confirmation/Footer';
import { createOrganisaatioNimi, updateOrganisaatioNimi } from '../../../api/organisaatio';
import { MUUTOSTYYPPI_CREATE, MUUTOSTYYPPI_EDIT } from './constants';
import { getUiDateStr } from '../../../tools/mappers';

type ModaaliProps = {
    nimet: UiOrganisaationNimetNimi[];
    oid: string;
    currentNimi?: UiOrganisaationNimetNimi;
    closeNimenmuutosModaali: (nimiIsUpdated: boolean) => void;
};

const todayUiDateStr = getUiDateStr();

const findNimiByAlkuPvm = (
    nimet: UiOrganisaationNimetNimi[],
    alkuPvm: LocalDate
): UiOrganisaationNimetNimi | undefined => nimet.find((nimi) => nimi.alkuPvm === getUiDateStr(alkuPvm));

export default function ToimipisteenNimenmuutosModaali(props: ModaaliProps) {
    const { currentNimi, oid, nimet } = props;
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const matchingNimi = findNimiByAlkuPvm(nimet, todayUiDateStr);
    const {
        reset,
        getValues,
        register,
        formState: { errors: validationErrors },
        handleSubmit,
        control: formControl,
        watch,
        setValue,
    } = useForm<NimenmuutosLomake>({
        defaultValues: {
            nimi: matchingNimi?.nimi || { fi: '', sv: '', en: '' },
            alkuPvm: todayUiDateStr,
            muutostyyppi: MUUTOSTYYPPI_CREATE,
            oid,
            foundAmatch: !!matchingNimi,
        },
        resolver: joiResolver(ToimipisteenNimenmuutosModaaliSchema),
    });
    useEffect(() => {
        const subscription = watch((value, { name }) => {
            if (name === 'muutostyyppi') {
                const { muutostyyppi } = value;
                const foundNimiForToday = findNimiByAlkuPvm(nimet, todayUiDateStr);
                switch (muutostyyppi) {
                    case MUUTOSTYYPPI_CREATE:
                        reset({
                            nimi: foundNimiForToday?.nimi || { fi: '', sv: '', en: '' },
                            alkuPvm: todayUiDateStr,
                            oid,
                            muutostyyppi,
                            foundAmatch: !!foundNimiForToday,
                        });
                        return;
                    case MUUTOSTYYPPI_EDIT:
                        reset({
                            nimi: currentNimi?.nimi,
                            alkuPvm: getUiDateStr(currentNimi?.alkuPvm),
                            muutostyyppi,
                            oid,
                            foundAmatch: false,
                        });
                        return;
                }
            } else if (name === 'alkuPvm') {
                const nimiMatch = findNimiByAlkuPvm(nimet, value.alkuPvm);
                if (nimiMatch) {
                    reset({
                        nimi: nimiMatch.nimi,
                        alkuPvm: nimiMatch.alkuPvm,
                        muutostyyppi: MUUTOSTYYPPI_CREATE,
                        oid,
                        foundAmatch: true,
                    });
                } else {
                    reset({
                        nimi: { fi: '', sv: '', en: '' },
                        alkuPvm: value.alkuPvm,
                        muutostyyppi: MUUTOSTYYPPI_CREATE,
                        oid,
                        foundAmatch: false,
                    });
                }
            }
        });
        return () => {
            return subscription.unsubscribe();
        };
    }, [watch, currentNimi, oid, reset, nimet]);

    const handleTallenna = async () => {
        setIsLoading(true);
        try {
            const { muutostyyppi, nimi, alkuPvm: newAlkuPvm, oid: oidValue, foundAmatch } = getValues();
            const newNimi = { nimi, alkuPvm: newAlkuPvm };
            if ((muutostyyppi === MUUTOSTYYPPI_EDIT || foundAmatch) && currentNimi) {
                const { nimi: matchNimi, alkuPvm } = foundAmatch
                    ? (findNimiByAlkuPvm(nimet, newAlkuPvm) as UiOrganisaationNimetNimi)
                    : currentNimi;
                const oldNimi = { nimi: matchNimi, alkuPvm, version: currentNimi.version };
                await updateOrganisaatioNimi(oidValue, oldNimi, { ...newNimi, version: currentNimi.version });
            } else if (muutostyyppi === MUUTOSTYYPPI_CREATE) {
                await createOrganisaatioNimi(oidValue, { ...newNimi, version: 0 });
            }
        } finally {
            setIsLoading(false);
            props.closeNimenmuutosModaali(true);
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
                    setValue={setValue}
                />
            }
            footer={<Footer tallennaCallback={handleSubmit(handleTallenna)} peruutaCallback={handlePeruuta} />}
            suljeCallback={handlePeruuta}
        />
    );
}
