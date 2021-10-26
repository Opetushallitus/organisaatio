import * as React from 'react';
import { useContext, useState } from 'react';
import styles from './PerustietoLomake.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import CheckboxGroup from '@opetushallitus/virkailija-ui-components/CheckboxGroup';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { KoodistoContext, LanguageContext } from '../../../../../contexts/contexts';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormRegister, UseFormSetValue, UseFormWatch } from 'react-hook-form/dist/types/form';
import { Controller } from 'react-hook-form';
import { KoodistoSelectOption, Perustiedot, ResolvedRakenne, Yhteystiedot } from '../../../../../types/types';

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
};

export default function PerustietoLomake(props: UusiOrgPerustiedotProps) {
    const { handleJatka, validationErrors, formControl, formRegister, rakenne, resolvedTyypit } = props;
    const { i18n } = useContext(LanguageContext);
    const { kuntaKoodisto, maatJaValtiotKoodisto, oppilaitoksenOpetuskieletKoodisto } = useContext(KoodistoContext);
    const [onYunnus, setOnYtunnus] = useState<boolean>(true);
    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_ORGANISAATIOTYYPPI')}</label>
                    <Controller
                        control={formControl}
                        name={'organisaatioTyypit'}
                        defaultValue={[]}
                        render={({ field: { ref, ...rest } }) => {
                            return <CheckboxGroup {...rest} options={resolvedTyypit} />;
                        }}
                    />
                </div>
            </div>
            {rakenne.showYtj && (
                <>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <RadioGroup
                                value={onYunnus.toString()}
                                options={[
                                    { value: 'true', label: i18n.translate('PERUSTIETO_ON_YTUNNUS') },
                                    { value: 'false', label: i18n.translate('PERUSTIETO_EI_YTUNNUS') },
                                ]}
                                onChange={(e) => setOnYtunnus(!onYunnus)}
                            />
                        </div>
                    </div>
                    {onYunnus && (
                        <div className={styles.Rivi}>
                            <div className={styles.Kentta}>
                                <label>Y-tunnus</label>
                                <Input
                                    error={!!validationErrors['ytunnus']}
                                    id={'ytunnus'}
                                    {...formRegister('ytunnus')}
                                    defaultValue={''}
                                />
                            </div>
                            <Button className={styles.Nappi} variant="outlined" onClick={props.openYtjModal}>
                                {i18n.translate('BUTTON_HAE_YTJ_TIEDOT')}
                            </Button>
                        </div>
                    )}
                </>
            )}
            <div className={styles.Rivi}>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('PERUSTIETO_NIMI_SUOMEKSI')}</label>
                    <Input
                        error={!!validationErrors['nimiFi']}
                        id={'organisaation_nimiFi'}
                        {...formRegister('nimi.fi')}
                        defaultValue={''}
                    />
                </div>{' '}
            </div>
            <div className={styles.Rivi}>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('PERUSTIETO_NIMI_RUOTSIKSI')}</label>
                    <Input
                        error={!!validationErrors['nimiSv']}
                        id={'organisaation_nimiSv'}
                        {...formRegister('nimi.sv')}
                        defaultValue={''}
                    />
                </div>{' '}
            </div>
            <div className={styles.Rivi}>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('PERUSTIETO_NIMI_ENGLANNIKSI')}</label>
                    <Input
                        error={!!validationErrors['nimiEn']}
                        id={'organisaation_nimiEn'}
                        {...formRegister('nimi.en')}
                        defaultValue={''}
                    />
                </div>
            </div>

            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_PERUSTAMISPAIVA')}</label>
                    <Controller
                        control={formControl}
                        name={'alkuPvm'}
                        render={({ field: { ref, ...rest } }) => (
                            <DatePickerInput error={!!validationErrors['alkuPvm']} {...rest} />
                        )}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_PAASIJAINTIKUNTA')}</label>
                    <Controller
                        control={formControl}
                        name={'kotipaikka'}
                        render={({ field }) => (
                            <Select
                                id="PERUSTIETO_PAASIJAINTIKUNTA_SELECT"
                                {...field}
                                ref={undefined}
                                error={!!validationErrors['kotipaikkaUri']}
                                options={kuntaKoodisto.selectOptions()}
                            />
                        )}
                    />
                </div>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_MUUT_KUNNATs')}</label>
                    <Controller
                        control={formControl}
                        name={'muutKotipaikat'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                id="PERUSTIETO_MUUT_KUNNAT_SELECT"
                                {...rest}
                                error={!!validationErrors['muutKotipaikatUris']}
                                isMulti
                                options={kuntaKoodisto.selectOptions()}
                            />
                        )}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_MAA')}</label>
                    <Controller
                        control={formControl}
                        name={'maa'}
                        defaultValue={maatJaValtiotKoodisto.uri2SelectOption('maatjavaltiot1_fin')}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                id="PERUSTIETO_MAA_SELECT"
                                {...rest}
                                error={!!validationErrors['maaUri']}
                                options={maatJaValtiotKoodisto.selectOptions()}
                            />
                        )}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_OPETUSKIELI')}</label>
                    <Controller
                        control={formControl}
                        name={'kielet'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                isMulti
                                id="PERUSTIETO_OPETUSKIELI_SELECT"
                                {...rest}
                                error={!!validationErrors['kieletUris']}
                                options={oppilaitoksenOpetuskieletKoodisto.selectOptions()}
                            />
                        )}
                    />
                </div>
            </div>
            <div>
                <Button onClick={handleJatka}>{i18n.translate('BUTTON_JATKA')}</Button>
            </div>
        </div>
    );
}
