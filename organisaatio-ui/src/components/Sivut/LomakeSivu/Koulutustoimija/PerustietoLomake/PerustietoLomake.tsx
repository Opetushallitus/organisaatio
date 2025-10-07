import * as React from 'react';
import { useReducer, useState } from 'react';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import CheckboxGroup from '@opetushallitus/virkailija-ui-components/CheckboxGroup';
import Select from 'react-select';
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
import { useAtom } from 'jotai';
import { casMeAtom } from '../../../../../api/kayttooikeus';
import { koodistotAtom } from '../../../../../api/koodisto';
import { getUiDateStr } from '../../../../../tools/mappers';
import IconWrapper from '../../../../IconWapper/IconWrapper';

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
    const [casMe] = useAtom(casMeAtom);
    const [koodistot] = useAtom(koodistotAtom);
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
    const [rerenderKey, forceRerender] = useReducer((x) => x + 1, 0);

    const kunnatOptions = koodistot.kuntaKoodisto.selectOptions();

    const handleNimenMuutosModaaliClose = (nimiIsUpdated: boolean) => {
        nimiIsUpdated && handleNimiTallennus();
        setNimenmuutosModaaliAuki(false);
    };
    const { organisaatioTyypit, lakkautusPvm } = getPerustiedotValues();
    const { currentNimi } = organisaatioBase;
    const canEditLakkautuspvm = casMe.canHaveButton(
        'PERUSTIETO_MERKITSE_ORGANISAATIO_LAKKAUTETUKSI',
        organisaatioBase.oid,
        organisaatioNimiPolku
    );
    return (
        <UloinKehys>
            <Rivi>
                <Ruudukko>
                    <AvainKevyestiBoldattu label={'LABEL_OID'} />
                    <ReadOnly value={organisaatioBase.oid} />
                    {organisaatioBase.yritysmuoto && [
                        <AvainKevyestiBoldattu key={'yritysmuoto_title'} label={'PERUSTIETO_YRITYSMUOTO'} />,
                        <AvainKevyestiBoldattu
                            key={'yritysmuoto_arvo'}
                            label={organisaatioBase.yritysmuoto}
                            translate={false}
                        />,
                    ]}
                    <AvainKevyestiBoldattu label={'PERUSTIETO_ORGANISAATION_NIMI'} />
                    <ReadOnlyNimi value={currentNimi?.nimi} />
                </Ruudukko>
                <div>
                    {casMe.canHaveButton(
                        'PERUSTIETO_MUOKKAA_ORGANISAATION_NIMEA',
                        organisaatioBase.oid,
                        organisaatioNimiPolku
                    ) && (
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
                    <Kentta label={'PERUSTIETO_ORGANISAATIOTYYPPI'} error={validationErrors['organisaatioTyypit']}>
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
                        {casMe.canHaveButton(
                            'PERUSTIETO_PAIVITA_YTJ_TIEDOT',
                            organisaatioBase.oid,
                            organisaatioNimiPolku
                        ) && <LomakeButton label={'PERUSTIETO_PAIVITA_YTJ_TIEDOT'} onClick={openYtjModal} />}
                    </Rivi>
                    <Rivi>
                        <LabelLink value={'PERUSTIETO_YTUNNUS_MUUTOKSET'} to={'https://www.ytj.fi'} />
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
            </Rivi>
            <Rivi>
                {lakkautusPvm && (
                    <div style={{ display: 'flex' }} key={`rerenderkey${rerenderKey}`}>
                        <Kentta label={'PERUSTIETO_LAKKAUTUSPAIVA'}>
                            <ReadOnlyDate value={lakkautusPvm} />
                        </Kentta>
                        {canEditLakkautuspvm && (
                            <>
                                <LomakeButton
                                    label={'PERUSTIETO_MERKITSE_ORGANISAATIO_LAKKAUTETUKSI'}
                                    onClick={() => setLakkautusModaaliAuki(true)}
                                />
                                <LomakeButton
                                    label={'PERUSTIETO_POISTA_LAKKAUTUS'}
                                    onClick={() => {
                                        setPerustiedotValue('lakkautusPvm', undefined);
                                        forceRerender();
                                    }}
                                    icon={() => <IconWrapper inline={true} icon="ci:trash-full" height={'1.2rem'} />}
                                />
                            </>
                        )}
                    </div>
                )}
                {canEditLakkautuspvm && !lakkautusPvm && (
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
                                options={kunnatOptions}
                                styles={{ menuPortal: (base) => ({ ...base, zIndex: 9999 }) }}
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
                                isMulti
                                options={kunnatOptions}
                                styles={{ menuPortal: (base) => ({ ...base, zIndex: 9999 }) }}
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
                                options={koodistot.maatJaValtiotKoodisto.selectOptions()}
                                styles={{ menuPortal: (base) => ({ ...base, zIndex: 9999 }) }}
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
                                options={koodistot.oppilaitoksenOpetuskieletKoodisto.selectOptions()}
                                styles={{ menuPortal: (base) => ({ ...base, zIndex: 9999 }) }}
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
                    handleTallennus={(date) =>
                        setPerustiedotValue('lakkautusPvm', date ? getUiDateStr(date) : undefined)
                    }
                />
            )}
        </UloinKehys>
    );
}
