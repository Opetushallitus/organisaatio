import * as React from 'react';
import { useState } from 'react';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import CheckboxGroup from '@opetushallitus/virkailija-ui-components/CheckboxGroup';
import Select from 'react-select';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import {
    Control,
    UseFormGetValues,
    UseFormRegister,
    UseFormSetValue,
    UseFormWatch,
} from 'react-hook-form/dist/types/form';
import { Controller } from 'react-hook-form';
import {
    KenttaError,
    KoodistoSelectOption,
    Perustiedot,
    ResolvedRakenne,
    Yhteystiedot,
} from '../../../../../types/types';
import DynamicFields from '../../Koulutustoimija/DynamicFields/DynamicFields';
import {
    AvainKevyestiBoldattu,
    Kentta,
    LomakeButton,
    NimiGroup,
    Rivi,
    Ruudukko,
    UloinKehys,
} from '../../LomakeFields/LomakeFields';
import DatePickerController from '../../../../Controllers/DatePickerController';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../../../api/lokalisaatio';
import { koodistotAtom } from '../../../../../api/koodisto';

type UusiOrgPerustiedotProps = {
    resolvedTyypit: KoodistoSelectOption[];
    rakenne: ResolvedRakenne;
    validationErrors: FieldErrors<Perustiedot>;
    formRegister: UseFormRegister<Perustiedot>;
    formControl: Control<Perustiedot>;
    handleJatka: () => void;
    openYtjModal: () => void;
    setPerustiedotValue: UseFormSetValue<Perustiedot>;
    setYhteystiedotValue: UseFormSetValue<Yhteystiedot>;
    watchPerustiedot: UseFormWatch<Perustiedot>;
    getPerustiedotValues: UseFormGetValues<Perustiedot>;
};

export default function PerustietoLomake({
    handleJatka,
    validationErrors,
    formControl,
    formRegister,
    openYtjModal,
    rakenne,
    resolvedTyypit,
    getPerustiedotValues,
    setPerustiedotValue,
}: UusiOrgPerustiedotProps) {
    const [i18n] = useAtom(languageAtom);
    const [koodistot] = useAtom(koodistotAtom);
    const [onYunnus, setOnYtunnus] = useState<boolean>(true);
    const { yritysmuoto } = getPerustiedotValues();
    return (
        <UloinKehys>
            <Rivi>
                <NimiGroup
                    error={validationErrors.nimi}
                    register={formRegister}
                    getValues={getPerustiedotValues}
                    setValue={setPerustiedotValue}
                />
            </Rivi>
            <Rivi>
                <Kentta
                    isRequired
                    label={'PERUSTIETO_ORGANISAATIOTYYPPI'}
                    error={validationErrors.organisaatioTyypit as KenttaError[]}
                >
                    <Controller
                        control={formControl}
                        name={'organisaatioTyypit'}
                        defaultValue={[]}
                        render={({ field: { ref, ...rest } }) => (
                            <CheckboxGroup
                                {...rest}
                                error={!!validationErrors['organisaatioTyypit']}
                                options={resolvedTyypit}
                            />
                        )}
                    />
                </Kentta>
            </Rivi>
            {rakenne.showYtj && (
                <>
                    <Rivi>
                        <Kentta label={''}>
                            <RadioGroup
                                value={onYunnus.toString()}
                                options={[
                                    { value: 'true', label: i18n.translate('PERUSTIETO_ON_YTUNNUS') },
                                    { value: 'false', label: i18n.translate('PERUSTIETO_EI_YTUNNUS') },
                                ]}
                                onChange={() => setOnYtunnus(!onYunnus)}
                            />
                        </Kentta>
                    </Rivi>
                    {onYunnus && (
                        <Rivi>
                            <Kentta isRequired label="Y-TUNNUS" error={validationErrors.ytunnus}>
                                <Input
                                    readOnly={true}
                                    error={!!validationErrors['ytunnus']}
                                    id={'ytunnus'}
                                    {...formRegister('ytunnus')}
                                    defaultValue={''}
                                />
                            </Kentta>
                            <LomakeButton label={'BUTTON_HAE_YTJ_TIEDOT'} onClick={openYtjModal} />
                        </Rivi>
                    )}
                </>
            )}
            {yritysmuoto && (
                <Rivi>
                    <Ruudukko>
                        <AvainKevyestiBoldattu key={'yritysmuoto_title'} label={'PERUSTIETO_YRITYSMUOTO'} />
                        <AvainKevyestiBoldattu key={'yritysmuoto_arvo'} label={yritysmuoto} translate={false} />
                    </Ruudukko>
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
                <Kentta isRequired label={'PERUSTIETO_PERUSTAMISPAIVA'} error={validationErrors.alkuPvm}>
                    <DatePickerController<Perustiedot>
                        name={'alkuPvm'}
                        form={formControl}
                        validationErrors={validationErrors}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta
                    isRequired
                    label={'PERUSTIETO_PAASIJAINTIKUNTA'}
                    error={validationErrors.kotipaikka as KenttaError}
                >
                    <Controller
                        control={formControl}
                        name={'kotipaikka'}
                        render={({ field }) => (
                            <Select
                                id={'PERUSTIETO_PAASIJAINTIKUNTA_SELECT'}
                                {...field}
                                ref={undefined}
                                options={koodistot.kuntaKoodisto.selectOptions()}
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
                                id={'PERUSTIETO_MUUT_KUNNAT_SELECT'}
                                {...rest}
                                isMulti
                                options={koodistot.kuntaKoodisto.selectOptions()}
                            />
                        )}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta isRequired label={'PERUSTIETO_MAA'} error={validationErrors.maa as KenttaError}>
                    <Controller
                        control={formControl}
                        name={'maa'}
                        defaultValue={koodistot.maatJaValtiotKoodisto.uri2SelectOption('maatjavaltiot1_fin')}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                id={'PERUSTIETO_MAA_SELECT'}
                                {...rest}
                                options={koodistot.maatJaValtiotKoodisto.selectOptions()}
                            />
                        )}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta isRequired label={'PERUSTIETO_OPETUSKIELI'} error={validationErrors.kielet as KenttaError}>
                    <Controller
                        control={formControl}
                        name={'kielet'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                isMulti
                                id={'PERUSTIETO_OPETUSKIELI_SELECT'}
                                {...rest}
                                options={koodistot.oppilaitoksenOpetuskieletKoodisto.selectOptions()}
                            />
                        )}
                    />
                </Kentta>
            </Rivi>
            <div>
                <Button onClick={handleJatka}>{i18n.translate('BUTTON_JATKA')}</Button>
            </div>
        </UloinKehys>
    );
}
