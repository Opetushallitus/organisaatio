import * as React from 'react';
import { useContext, useState } from 'react';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import CheckboxGroup from '@opetushallitus/virkailija-ui-components/CheckboxGroup';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { KoodistoContext, LanguageContext } from '../../../../../contexts/contexts';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormRegister, UseFormSetValue, UseFormWatch } from 'react-hook-form/dist/types/form';
import { Controller } from 'react-hook-form';
import { KoodistoSelectOption, Perustiedot, ResolvedRakenne, Yhteystiedot } from '../../../../../types/types';
import DynamicFields from '../../Koulutustoimija/DynamicFields/DynamicFields';
import { Kentta, LomakeButton, Rivi, UloinKehys } from '../../LomakeFields/LomakeFields';
import DatePickerController from '../../../../Controllers/DatePickerController';

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
    getPerustiedotValues: () => Perustiedot;
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
}: UusiOrgPerustiedotProps) {
    const { i18n } = useContext(LanguageContext);
    const koodistot = useContext(KoodistoContext);
    const [onYunnus, setOnYtunnus] = useState<boolean>(true);
    return (
        <UloinKehys>
            <Rivi>
                <Kentta label={'PERUSTIETO_NIMI_SUOMEKSI'}>
                    <Input
                        error={!!validationErrors['nimiFi']}
                        id={'organisaation_nimiFi'}
                        {...formRegister('nimi.fi')}
                        defaultValue={''}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta label={'PERUSTIETO_NIMI_RUOTSIKSI'}>
                    <Input
                        error={!!validationErrors['nimiSv']}
                        id={'organisaation_nimiSv'}
                        {...formRegister('nimi.sv')}
                        defaultValue={''}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta label={'PERUSTIETO_NIMI_ENGLANNIKSI'}>
                    <Input
                        error={!!validationErrors['nimiEn']}
                        id={'organisaation_nimiEn'}
                        {...formRegister('nimi.en')}
                        defaultValue={''}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta label={'PERUSTIETO_ORGANISAATIOTYYPPI'}>
                    <Controller
                        control={formControl}
                        name={'organisaatioTyypit'}
                        defaultValue={[]}
                        render={({ field: { ref, ...rest } }) => {
                            return <CheckboxGroup {...rest} options={resolvedTyypit} />;
                        }}
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
                                onChange={(e) => setOnYtunnus(!onYunnus)}
                            />
                        </Kentta>
                    </Rivi>
                    {onYunnus && (
                        <Rivi>
                            <Kentta label={'Y-TUNNUS'}>
                                <Input
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
                <Kentta label={'PERUSTIETO_PERUSTAMISPAIVA'}>
                    <DatePickerController<Perustiedot>
                        name={'alkuPvm'}
                        form={formControl}
                        validationErrors={validationErrors}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta label={'PERUSTIETO_PAASIJAINTIKUNTA'}>
                    <Controller
                        control={formControl}
                        name={'kotipaikka'}
                        render={({ field }) => (
                            <Select
                                id={'PERUSTIETO_PAASIJAINTIKUNTA_SELECT'}
                                {...field}
                                ref={undefined}
                                error={!!validationErrors['kotipaikka']}
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
                                error={!!validationErrors['muutKotipaikat']}
                                isMulti
                                options={koodistot.kuntaKoodisto.selectOptions()}
                            />
                        )}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta label={'PERUSTIETO_MAA'}>
                    <Controller
                        control={formControl}
                        name={'maa'}
                        defaultValue={koodistot.maatJaValtiotKoodisto.uri2SelectOption('maatjavaltiot1_fin')}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
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
                <Kentta label={'PERUSTIETO_OPETUSKIELI'}>
                    <Controller
                        control={formControl}
                        name={'kielet'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
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
            <div>
                <Button onClick={handleJatka}>{i18n.translate('BUTTON_JATKA')}</Button>
            </div>
        </UloinKehys>
    );
}
