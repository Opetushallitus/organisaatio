import * as React from 'react';
import { useContext, useState } from 'react';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import CheckboxGroup from '@opetushallitus/virkailija-ui-components/CheckboxGroup';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { KoodistoContext } from '../../../../../contexts/contexts';
import PohjaModaali from '../../../../Modaalit/PohjaModaali/PohjaModaali';
import TLHeader from '../../../../Modaalit/ToimipisteenLakkautus/TLHeader';
import TLBody from '../../../../Modaalit/ToimipisteenLakkautus/TLBody';
import TLFooter from '../../../../Modaalit/ToimipisteenLakkautus/TLFooter';
import {
    KoodistoSelectOption,
    Nimi,
    Perustiedot,
    ResolvedRakenne,
    UiOrganisaatioBase,
} from '../../../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormRegister } from 'react-hook-form/dist/types/form';
import { Controller, useWatch } from 'react-hook-form';
import ToimipisteenNimenmuutosModaali from '../../../../Modaalit/ToimipisteenNimenmuutos/ToimipisteenNimenmuutosModaali';
import DatePickerController from '../../../../Controllers/DatePickerController';
import DynamicFields from '../DynamicFields/DynamicFields';
import {
    AvainKevyestiBoldattu,
    Kentta,
    LomakeButton,
    ReadOnly,
    ReadOnlyNimi,
    Rivi,
    Ruudukko,
    UloinKehys,
} from '../../LomakeFields/LomakeFields';

type PerustietoLomakeProps = {
    resolvedTyypit: KoodistoSelectOption[];
    rakenne: ResolvedRakenne | undefined;
    language: string;
    openYtjModal: () => void;
    validationErrors: FieldErrors<Perustiedot>;
    formRegister: UseFormRegister<Perustiedot>;
    formControl: Control<Perustiedot>;
    handleNimiUpdate: (nimi: Nimi) => void;
    getPerustiedotValues: () => Perustiedot;
    organisaatioBase: UiOrganisaatioBase;
};

const OrganisaationNimi = ({ defaultNimi, control }) => {
    const nimi = useWatch({ control, name: 'nimi', defaultValue: defaultNimi });
    return <ReadOnlyNimi value={nimi} />;
};

export default function PerustietoLomake(props: PerustietoLomakeProps) {
    const {
        organisaatioBase,
        getPerustiedotValues,
        openYtjModal,
        validationErrors,
        formRegister,
        formControl,
        handleNimiUpdate,
        rakenne,
        resolvedTyypit,
    } = props;
    const [nimenmuutosModaaliAuki, setNimenmuutosModaaliAuki] = useState<boolean>(false);
    const [lakkautusModaaliAuki, setLakkautusModaaliAuki] = useState<boolean>(false);

    const koodistot = useContext(KoodistoContext);
    const kunnatOptions = koodistot.kuntaKoodisto.selectOptions();

    formRegister('nimi');
    const { nimi, organisaatioTyypit } = getPerustiedotValues();
    return (
        <UloinKehys>
            <Rivi>
                <Ruudukko>
                    <AvainKevyestiBoldattu label="LABEL_OID" />
                    <ReadOnly value={organisaatioBase?.oid} />
                    {organisaatioBase?.yritysmuoto && [
                        <AvainKevyestiBoldattu key={'yritysmuoto_title'} label="PERUSTIETO_YRITYSMUOTO" />,
                        <AvainKevyestiBoldattu key={'yritysmuoto_arvo'} label={organisaatioBase.yritysmuoto} />,
                    ]}
                    <AvainKevyestiBoldattu label="PERUSTIETO_ORGANISAATION_NIMI" />
                    <OrganisaationNimi control={formControl} defaultNimi={nimi} />
                </Ruudukko>
                <div>
                    <LomakeButton
                        label="PERUSTIETO_MUOKKAA_ORGANISAATION_NIMEA"
                        onClick={() => setNimenmuutosModaaliAuki(true)}
                    />
                </div>
            </Rivi>

            {organisaatioTyypit && (
                <Rivi>
                    <Kentta label="PERUSTIETO_ORGANISAATIOTYYPPI">
                        <Controller
                            control={formControl}
                            name={'organisaatioTyypit'}
                            render={({ field: { ref, ...rest } }) => (
                                <CheckboxGroup {...rest} options={resolvedTyypit} />
                            )}
                        />
                    </Kentta>
                </Rivi>
            )}

            {rakenne?.showYtj && (
                <Rivi>
                    <Kentta isRequired label="PERUSTIETO_Y_TUNNUS'">
                        <Input error={!!validationErrors['ytunnus']} id={'ytunnus'} {...formRegister('ytunnus')} />
                    </Kentta>
                    <LomakeButton label="PERUSTIETO_PAIVITA_YTJ_TIEDOT" onClick={openYtjModal} />
                </Rivi>
            )}
            {rakenne?.dynamicFields && (
                <DynamicFields
                    dynamicFields={rakenne.dynamicFields}
                    getPerustiedotValues={getPerustiedotValues}
                    formControl={formControl}
                    validationErrors={validationErrors}
                    koodistot={koodistot}
                />
            )}
            <Rivi>
                <Kentta isRequired label="PERUSTIETO_PERUSTAMISPAIVA">
                    <DatePickerController<Perustiedot>
                        name={'alkuPvm'}
                        form={formControl}
                        validationErrors={validationErrors}
                    />
                </Kentta>
                <LomakeButton
                    label="PERUSTIETO_MERKITSE_ORGANISAATIO_LAKKAUTETUKSI"
                    onClick={() => setLakkautusModaaliAuki(true)}
                />
            </Rivi>
            <Rivi>
                <Kentta isRequired label="PERUSTIETO_PAASIJAINTIKUNTA">
                    <Controller
                        control={formControl}
                        name={'kotipaikka'}
                        render={({ field }) => (
                            <Select
                                id="PERUSTIETO_PAASIJAINTIKUNTA_SELECT"
                                {...field}
                                ref={undefined}
                                error={!!validationErrors['kotipaikka']}
                                options={kunnatOptions}
                            />
                        )}
                    />
                </Kentta>
                <Kentta label="PERUSTIETO_MUUT_KUNNAT">
                    <Controller
                        control={formControl}
                        name={'muutKotipaikat'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                id="PERUSTIETO_MUUT_KUNNAT_SELECT"
                                {...rest}
                                error={!!validationErrors['muutKotipaikat']}
                                isMulti
                                options={kunnatOptions}
                            />
                        )}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta isRequired label="PERUSTIETO_MAA">
                    <Controller
                        control={formControl}
                        name={'maa'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                id="PERUSTIETO_MAA_SELECT"
                                {...rest}
                                error={!!validationErrors['maa']}
                                options={koodistot.maatJaValtiotKoodisto.selectOptions()}
                            />
                        )}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta isRequired label="PERUSTIETO_OPETUSKIELI">
                    <Controller
                        control={formControl}
                        name={'kielet'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                isMulti
                                id="PERUSTIETO_OPETUSKIELI_SELECT"
                                {...rest}
                                error={!!validationErrors['kielet']}
                                options={koodistot.oppilaitoksenOpetuskieletKoodisto.selectOptions()}
                            />
                        )}
                    />
                </Kentta>
            </Rivi>
            {nimenmuutosModaaliAuki && (
                <ToimipisteenNimenmuutosModaali
                    closeNimenmuutosModaali={() => setNimenmuutosModaaliAuki(false)}
                    handleNimiTallennus={handleNimiUpdate}
                    nimi={nimi}
                />
            )}
            {lakkautusModaaliAuki && (
                <PohjaModaali
                    header={<TLHeader />}
                    body={<TLBody />}
                    footer={
                        <TLFooter
                            peruutaCallback={() => {
                                setLakkautusModaaliAuki(false);
                            }}
                        />
                    }
                    suljeCallback={() => setLakkautusModaaliAuki(false)}
                />
            )}
        </UloinKehys>
    );
}
