import * as React from 'react';
import { useContext, useState } from 'react';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import CheckboxGroup from '@opetushallitus/virkailija-ui-components/CheckboxGroup';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import {
    KoodistoSelectOption,
    OrganisaatioNimiJaOid,
    Perustiedot,
    ResolvedRakenne,
    UiOrganisaatioBase,
} from '../../../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormGetValues, UseFormRegister, UseFormSetValue } from 'react-hook-form/dist/types/form';
import { Controller } from 'react-hook-form';
import ToimipisteenNimenmuutosModaali from '../../../../Modaalit/ToimipisteenNimenmuutos/ToimipisteenNimenmuutosModaali';
import DatePickerController from '../../../../Controllers/DatePickerController';
import DynamicFields from '../DynamicFields/DynamicFields';
import {
    AvainKevyestiBoldattu,
    Kentta,
    LabelLink,
    LomakeButton,
    ReadOnly,
    ReadOnlyDate,
    ReadOnlyNimi,
    Rivi,
    Ruudukko,
    UloinKehys,
} from '../../LomakeFields/LomakeFields';
import ToimipisteenLakkautus from '../../../../Modaalit/ToimipisteenLakkautus/ToimipisteenLakkautus';
import { KoodistoContext } from '../../../../../contexts/KoodistoContext';
import { CasMeContext } from '../../../../../contexts/CasMeContext';

type PerustietoLomakeProps = {
    resolvedTyypit: KoodistoSelectOption[];
    rakenne: ResolvedRakenne | undefined;
    openYtjModal: () => void;
    validationErrors: FieldErrors<Perustiedot>;
    formRegister: UseFormRegister<Perustiedot>;
    formControl: Control<Perustiedot>;
    setPerustiedotValue: UseFormSetValue<Perustiedot>;
    getPerustiedotValues: UseFormGetValues<Perustiedot>;
    organisaatioBase: UiOrganisaatioBase;
    handleNimiTallennus: () => void;
    organisaatioNimiPolku: OrganisaatioNimiJaOid[];
    readOnly: boolean;
};

export default function PerustietoLomake(props: PerustietoLomakeProps) {
    const { me: casMe } = useContext(CasMeContext);
    const {
        organisaatioBase,
        getPerustiedotValues,
        openYtjModal,
        validationErrors,
        formRegister,
        formControl,
        setPerustiedotValue,
        handleNimiTallennus,
        rakenne,
        resolvedTyypit,
        organisaatioNimiPolku,
        readOnly,
    } = props;
    const [nimenmuutosModaaliAuki, setNimenmuutosModaaliAuki] = useState<boolean>(false);
    const [lakkautusModaaliAuki, setLakkautusModaaliAuki] = useState<boolean>(false);

    const koodistot = useContext(KoodistoContext);
    const kunnatOptions = koodistot.kuntaKoodisto.selectOptions();

    const handleNimenMuutosModaaliClose = (nimiIsUpdated: boolean) => {
        nimiIsUpdated && handleNimiTallennus();
        setNimenmuutosModaaliAuki(false);
    };
    const { organisaatioTyypit, lakkautusPvm } = getPerustiedotValues();
    const { currentNimi } = organisaatioBase;
    return (
        <UloinKehys>
            <Rivi>
                <Ruudukko>
                    <AvainKevyestiBoldattu label={'LABEL_OID'} />
                    <ReadOnly value={organisaatioBase?.oid} />
                    {organisaatioBase?.yritysmuoto && [
                        <AvainKevyestiBoldattu key={'yritysmuoto_title'} label={'PERUSTIETO_YRITYSMUOTO'} />,
                        <AvainKevyestiBoldattu key={'yritysmuoto_arvo'} label={organisaatioBase.yritysmuoto} />,
                    ]}
                    <AvainKevyestiBoldattu label={'PERUSTIETO_ORGANISAATION_NIMI'} />
                    <ReadOnlyNimi value={currentNimi?.nimi} />
                </Ruudukko>
                <div>
                    {casMe.canHaveButton('PERUSTIETO_MUOKKAA_ORGANISAATION_NIMEA', organisaatioNimiPolku) && (
                        <LomakeButton
                            disabled={!currentNimi}
                            label={'PERUSTIETO_MUOKKAA_ORGANISAATION_NIMEA'}
                            onClick={() => setNimenmuutosModaaliAuki(true)}
                        />
                    )}
                </div>
            </Rivi>
            {organisaatioTyypit && (
                <Rivi>
                    <Kentta label={'PERUSTIETO_ORGANISAATIOTYYPPI'}>
                        <Controller
                            control={formControl}
                            name={'organisaatioTyypit'}
                            render={({ field: { ref, ...rest } }) => (
                                <CheckboxGroup
                                    disabled={readOnly}
                                    options={resolvedTyypit}
                                    error={!!validationErrors['organisaatioTyypit']}
                                    {...rest}
                                />
                            )}
                        />
                    </Kentta>
                </Rivi>
            )}

            {rakenne?.showYtj && (
                <>
                    <Rivi>
                        <Kentta isRequired label={'PERUSTIETO_Y_TUNNUS'}>
                            <Input
                                disabled={readOnly}
                                readOnly={true}
                                error={!!validationErrors['ytunnus']}
                                id={'ytunnus'}
                                {...formRegister('ytunnus')}
                            />
                        </Kentta>
                        {casMe.canHaveButton('PERUSTIETO_PAIVITA_YTJ_TIEDOT', organisaatioNimiPolku) && (
                            <LomakeButton label={'PERUSTIETO_PAIVITA_YTJ_TIEDOT'} onClick={openYtjModal} />
                        )}
                    </Rivi>
                    <Rivi>
                        <LabelLink
                            value={'PERUSTIETO_YTUNNUS_MUUTOKSET'}
                            to={'https://www.ytj.fi/index/ilmoittaminen/muutosilmoitus.html'}
                        />
                    </Rivi>
                </>
            )}
            {rakenne?.dynamicFields && (
                <DynamicFields
                    readOnly={readOnly}
                    dynamicFields={rakenne.dynamicFields}
                    getPerustiedotValues={getPerustiedotValues}
                    formControl={formControl}
                    validationErrors={validationErrors}
                    koodistot={koodistot}
                />
            )}
            <Rivi>
                <Kentta isRequired label={'PERUSTIETO_PERUSTAMISPAIVA'}>
                    <DatePickerController<Perustiedot>
                        disabled={readOnly}
                        name={'alkuPvm'}
                        form={formControl}
                        validationErrors={validationErrors}
                    />
                </Kentta>
                {lakkautusPvm && (
                    <Kentta label={'PERUSTIETO_LAKKAUTUSPAIVA'}>
                        <ReadOnlyDate value={lakkautusPvm} />
                    </Kentta>
                )}
                {casMe.canHaveButton('PERUSTIETO_MERKITSE_ORGANISAATIO_LAKKAUTETUKSI', organisaatioNimiPolku) && (
                    <LomakeButton
                        label={'PERUSTIETO_MERKITSE_ORGANISAATIO_LAKKAUTETUKSI'}
                        onClick={() => setLakkautusModaaliAuki(true)}
                    />
                )}
            </Rivi>
            <Rivi>
                <Kentta isRequired label={'PERUSTIETO_PAASIJAINTIKUNTA'}>
                    <Controller
                        control={formControl}
                        name={'kotipaikka'}
                        render={({ field }) => (
                            <Select
                                isDisabled={readOnly}
                                id={'PERUSTIETO_PAASIJAINTIKUNTA_SELECT'}
                                {...field}
                                ref={undefined}
                                error={!!validationErrors['kotipaikka']}
                                options={kunnatOptions}
                            />
                        )}
                    />
                </Kentta>
                <Kentta label={'PERUSTIETO_MUUT_KUNNAT'}>
                    <Controller
                        control={formControl}
                        name={'muutKotipaikat'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                isDisabled={readOnly}
                                id={'PERUSTIETO_MUUT_KUNNAT_SELECT'}
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
                <Kentta isRequired label={'PERUSTIETO_MAA'}>
                    <Controller
                        control={formControl}
                        name={'maa'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                isDisabled={readOnly}
                                id={'PERUSTIETO_MAA_SELECT'}
                                {...rest}
                                error={!!validationErrors['maa']}
                                options={koodistot.maatJaValtiotKoodisto.selectOptions()}
                            />
                        )}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta isRequired label={'PERUSTIETO_OPETUSKIELI'}>
                    <Controller
                        control={formControl}
                        name={'kielet'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                isDisabled={readOnly}
                                isMulti
                                id={'PERUSTIETO_OPETUSKIELI_SELECT'}
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
                    closeNimenmuutosModaali={handleNimenMuutosModaaliClose}
                    currentNimi={currentNimi}
                    oid={organisaatioBase.oid}
                    nimet={organisaatioBase.nimet}
                />
            )}
            {lakkautusModaaliAuki && (
                <ToimipisteenLakkautus
                    closeModaali={() => setLakkautusModaaliAuki(false)}
                    date={lakkautusPvm}
                    handleTallennus={(date) => {
                        setPerustiedotValue('lakkautusPvm', date);
                    }}
                />
            )}
        </UloinKehys>
    );
}
