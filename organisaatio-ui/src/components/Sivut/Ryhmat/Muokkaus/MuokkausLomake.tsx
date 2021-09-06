import * as React from 'react';
import styles from './MuokkausLomake.module.css';
import Icon from '@iconify/react';
import homeIcon from '@iconify/icons-fa-solid/home';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { Ryhma } from '../../../../types/types';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import PohjaSivu from '../../PohjaSivu/PohjaSivu';
import { useContext } from 'react';
import { KoodistoContext, LanguageContext } from '../../../../contexts/contexts';
import { mapKoodistoOptions, mapLocalizedKoodiToLang, mapValuesToSelect } from '../../../mappers';
import { FieldValues } from 'react-hook-form/dist/types/fields';
import { SubmitHandler, useForm, Controller } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import Joi from 'joi';

export type MuokkausLomakeProps = {
    onUusi: boolean;
    ryhma: Ryhma;
    handlePeruuta: () => void;
    handlePassivoi: () => void;
    handlePoista: () => void;
    handleTallenna: SubmitHandler<FieldValues>;
};

export const ryhmatLomakeSchema = Joi.object({
    nimiEn: Joi.string(),
    nimiFi: Joi.string(),
    nimiSv: Joi.string(),
    kuvaus2Fi: Joi.string(),
    kuvaus2Sv: Joi.string(),
    kuvaus2En: Joi.string(),
    ryhmatyypit: Joi.array().min(1).required(),
    kayttoryhmat: Joi.array().min(1).required(),
})
    .when(Joi.object({ nimiFi: Joi.string().required() }).unknown(), {
        then: Joi.object({ nimiSv: Joi.string().allow(''), nimiEn: Joi.string().allow('') }),
    })
    .when(Joi.object({ nimiSv: Joi.string().required() }).unknown(), {
        then: Joi.object({ nimiFi: Joi.string().allow(''), nimiEn: Joi.string().allow('') }),
    })
    .when(Joi.object({ nimiEn: Joi.string().required() }).unknown(), {
        then: Joi.object({ nimiFi: Joi.string().allow(''), nimiSv: Joi.string().allow('') }),
    })
    .when(Joi.object({ kuvaus2Fi: Joi.string().required() }).unknown(), {
        then: Joi.object({ kuvaus2Sv: Joi.string().allow(''), kuvaus2En: Joi.string().allow('') }),
    })
    .when(Joi.object({ kuvaus2Sv: Joi.string().required() }).unknown(), {
        then: Joi.object({ kuvaus2Fi: Joi.string().allow(''), kuvaus2En: Joi.string().allow('') }),
    })
    .when(Joi.object({ kuvaus2En: Joi.string().required() }).unknown(), {
        then: Joi.object({ kuvausSv: Joi.string().allow(''), kuvaus2Fi: Joi.string().allow('') }),
    })
    .or('nimiFi', 'nimiSv', 'nimiEn')
    .or('kuvaus2Fi', 'kuvaus2Sv', 'kuvaus2En')
    .and('ryhmatyypit', 'kayttoryhmat');

const MuokkausLomake = ({
    onUusi,
    ryhma,
    handlePeruuta,
    handlePassivoi,
    handlePoista,
    handleTallenna,
}: MuokkausLomakeProps) => {
    const { i18n, language } = useContext(LanguageContext);
    const { ryhmaTyypitKoodisto, kayttoRyhmatKoodisto } = useContext(KoodistoContext);

    const ryhmaTyypitOptions = mapKoodistoOptions(ryhmaTyypitKoodisto.koodit(), language);
    const kayttoRyhmatOptions = mapKoodistoOptions(kayttoRyhmatKoodisto.koodit(), language);

    const kayttoRyhmat = mapValuesToSelect(ryhma.kayttoryhmat, kayttoRyhmatOptions);
    const ryhmaTyypit = mapValuesToSelect(ryhma.ryhmatyypit, ryhmaTyypitOptions);
    const isDisabled = !ryhma || ryhma.status === 'PASSIIVINEN';
    const {
        register,
        formState: { errors: validationErrors },
        handleSubmit,
        control,
    } = useForm({ resolver: joiResolver(ryhmatLomakeSchema) });

    return (
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <a href="/organisaatio/ryhmat">
                        <Icon icon={homeIcon} />
                    </a>
                </div>
                <div>
                    <a href="/organisaatio/ryhmat">{i18n.translate('RYHMAT_KAIKKI_RYHMAT')}</a>
                </div>
            </div>
            <div className={styles.PaaKehys}>
                <div className={styles.ValiContainer}>
                    <div className={styles.ValiOtsikko}>
                        <h3>{i18n.translate('RYHMAT_RYHMA')}</h3>
                        <h1>{mapLocalizedKoodiToLang(language, 'nimi', ryhma)}</h1>
                    </div>
                </div>
                <div className={styles.PaaOsio}>
                    <div className={styles.OtsikkoRivi}>
                        <div className={styles.Otsikko}>
                            <h3>{i18n.translate('RYHMAT_RYHMAN_TIEDOT_OTSIKKO')}</h3>
                        </div>
                    </div>
                    <div className={styles.OidRivi}>
                        <span className={styles.AvainKevyestiBoldattu}>{i18n.translate('LABEL_OID')}</span>
                        <span className={styles.ReadOnly}>{ryhma.oid || ''}</span>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('RYHMAT_RYHMAN_NIMI')}</label>
                            <div className={styles.Rivi}>
                                <label htmlFor={'nimiFi'}>{i18n.translate('LABEL_SUOMEKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input
                                        disabled={isDisabled}
                                        error={!!validationErrors['nimiFi']}
                                        id={'nimiFi'}
                                        {...register('nimiFi')}
                                        defaultValue={ryhma.nimi['fi']}
                                    />
                                </div>
                            </div>
                            <div className={styles.Rivi}>
                                <label htmlFor={'nimiSv'}>{i18n.translate('LABEL_RUOTSIKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input
                                        disabled={isDisabled}
                                        error={!!validationErrors['nimiSv']}
                                        id={'nimiSv'}
                                        {...register('nimiSv')}
                                        defaultValue={ryhma.nimi['sv']}
                                    />
                                </div>
                            </div>
                            <div className={styles.Rivi}>
                                <label htmlFor={'nimiEn'}>{i18n.translate('LABEL_ENGLANNIKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input
                                        disabled={isDisabled}
                                        error={!!validationErrors['nimiEn']}
                                        id={'nimiEn'}
                                        {...register('nimiEn')}
                                        defaultValue={ryhma.nimi['en']}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('RYHMAT_RYHMAN_KUVAUS')}</label>
                            <div className={styles.Rivi}>
                                <label htmlFor={'kuvaus2Fi'}>{i18n.translate('LABEL_SUOMEKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input
                                        error={!!validationErrors['kuvaus2Fi']}
                                        id={'kuvaus2Fi'}
                                        {...register('kuvaus2Fi')}
                                        defaultValue={ryhma.kuvaus2['kieli_fi#1']}
                                    />
                                </div>
                            </div>
                            <div className={styles.Rivi}>
                                <label htmlFor={'kuvaus2Sv'}>{i18n.translate('LABEL_RUOTSIKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input
                                        error={!!validationErrors['kuvaus2Sv']}
                                        id={'kuvaus2Sv'}
                                        {...register('kuvaus2Sv')}
                                        defaultValue={ryhma.kuvaus2['kieli_sv#1']}
                                    />
                                </div>
                            </div>
                            <div className={styles.Rivi}>
                                <label htmlFor={'kuvaus2En'}>{i18n.translate('LABEL_ENGLANNIKSI')}</label>
                                <div className={styles.PitkaInput}>
                                    <Input
                                        error={!!validationErrors['kuvaus2En']}
                                        id={'kuvaus2En'}
                                        {...register('kuvaus2En')}
                                        defaultValue={ryhma.kuvaus2['kieli_en#1']}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('RYHMAT_RYHMAN_TYYPPI')}</label>
                            <Controller
                                control={control}
                                name={'ryhmatyypit'}
                                defaultValue={ryhmaTyypit}
                                render={({ field }) => (
                                    <Select
                                        {...field}
                                        error={!!validationErrors['ryhmatyypit']}
                                        isMulti
                                        options={ryhmaTyypitOptions}
                                        isDisabled={isDisabled}
                                    />
                                )}
                            />
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('RYHMAT_RYHMAN_KAYTTOTARKOITUS')}</label>
                            <Controller
                                control={control}
                                name={'kayttoryhmat'}
                                defaultValue={kayttoRyhmat}
                                render={({ field }) => (
                                    <Select
                                        {...field}
                                        error={!!validationErrors['kayttoryhmat']}
                                        isMulti
                                        options={kayttoRyhmatOptions}
                                        isDisabled={isDisabled}
                                    />
                                )}
                            />
                        </div>
                    </div>
                    {!onUusi && (
                        <div className={styles.AlinRivi}>
                            <Button name="passivoibutton" variant="outlined" onClick={handlePassivoi}>
                                {ryhma.status === 'AKTIIVINEN'
                                    ? i18n.translate('RYHMAT_PASSIVOI_RYHMA')
                                    : i18n.translate('RYHMAT_AKTIVOI_RYHMA')}
                            </Button>
                            ,
                            <Button name="poistabutton" variant="outlined" onClick={handlePoista}>
                                {i18n.translate('RYHMAT_POISTA_RYHMA')}
                            </Button>
                        </div>
                    )}
                </div>
            </div>
            <div className={styles.AlaBanneri}>
                <div>
                    <Button
                        name="peruutabutton"
                        variant="outlined"
                        className={styles.Versionappula}
                        onClick={handlePeruuta}
                    >
                        {i18n.translate('BUTTON_SULJE')}
                    </Button>
                    <Button
                        disabled={ryhma.status === 'PASSIIVINEN'}
                        name="tallennabutton"
                        className={styles.Versionappula}
                        onClick={handleSubmit(handleTallenna)}
                    >
                        {i18n.translate('BUTTON_TALLENNA')}
                    </Button>
                </div>
            </div>
        </PohjaSivu>
    );
};

export default MuokkausLomake;
