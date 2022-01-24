import * as React from 'react';
import { useContext, useState } from 'react';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import CheckboxGroup from '@opetushallitus/virkailija-ui-components/CheckboxGroup';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { KoodistoContext } from '../../../../../contexts/KoodistoContext';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormRegister, UseFormSetValue, UseFormWatch } from 'react-hook-form/dist/types/form';
import { Controller } from 'react-hook-form';
import { KoodistoSelectOption, Perustiedot, ResolvedRakenne, Yhteystiedot } from '../../../../../types/types';
import DynamicFields from '../../Koulutustoimija/DynamicFields/DynamicFields';
import {
    AvainKevyestiBoldattu,
    Kentta,
    LomakeButton,
    Rivi,
    Ruudukko,
    UloinKehys,
} from '../../LomakeFields/LomakeFields';
import DatePickerController from '../../../../Controllers/DatePickerController';
import { languageAtom } from '../../../../../contexts/LanguageContext';
import { useAtom } from 'jotai';

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
    const [i18n] = useAtom(languageAtom);
    const koodistot = useContext(KoodistoContext);
    const [onYunnus, setOnYtunnus] = useState<boolean>(true);
    const { yritysmuoto } = getPerustiedotValues();
    return (
        <UloinKehys>
            <Rivi>
                <Kentta isRequired label={'PERUSTIETO_NIMI_SUOMEKSI'}>
                    <Input
                        error={!!validationErrors.nimi?.fi}
                        id={'organisaation_nimiFi'}
                        {...formRegister('nimi.fi')}
                        defaultValue={''}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta isRequired label={'PERUSTIETO_NIMI_RUOTSIKSI'}>
                    <Input
                        error={!!validationErrors.nimi?.sv}
                        id={'organisaation_nimiSv'}
                        {...formRegister('nimi.sv')}
                        defaultValue={''}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta isRequired label={'PERUSTIETO_NIMI_ENGLANNIKSI'}>
                    <Input
                        error={!!validationErrors.nimi?.en}
                        id={'organisaation_nimiEn'}
                        {...formRegister('nimi.en')}
                        defaultValue={''}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta isRequired label={'PERUSTIETO_ORGANISAATIOTYYPPI'}>
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
                        <Kentta isRequired label={''}>
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
                            <Kentta isRequired label="Y-TUNNUS">
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
                <Kentta isRequired label={'PERUSTIETO_PERUSTAMISPAIVA'}>
                    <DatePickerController<Perustiedot>
                        name={'alkuPvm'}
                        form={formControl}
                        validationErrors={validationErrors}
                    />
                </Kentta>
            </Rivi>
            <Rivi>
                <Kentta isRequired label={'PERUSTIETO_PAASIJAINTIKUNTA'}>
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
                <Kentta isRequired label={'PERUSTIETO_MAA'}>
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
                <Kentta isRequired label={'PERUSTIETO_OPETUSKIELI'}>
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
