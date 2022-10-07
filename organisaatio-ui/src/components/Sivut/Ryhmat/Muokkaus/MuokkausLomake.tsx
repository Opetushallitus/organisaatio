import * as React from 'react';
import styles from './MuokkausLomake.module.css';
import homeIcon from '@iconify/icons-fa-solid/home';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { Ryhma } from '../../../../types/types';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import PohjaSivu from '../../PohjaSivu/PohjaSivu';
import { FieldValues } from 'react-hook-form/dist/types/fields';
import { Controller, SubmitHandler, useForm } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import { Link } from 'react-router-dom';
import RyhmatLomakeSchema from '../../../../ValidationSchemas/RyhmatLomakeSchema';
import { AlaBanneri, LomakeButton, VersioContainer } from '../../LomakeSivu/LomakeFields/LomakeFields';
import Muokattu from '../../../Muokattu/Muokattu';
import IconWrapper from '../../../IconWapper/IconWrapper';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../../api/lokalisaatio';
import { kayttoRyhmatKoodistoAtom, ryhmaTyypitKoodistoAtom } from '../../../../api/koodisto';

export type MuokkausLomakeProps = {
    onUusi: boolean;
    ryhma: Ryhma;
    handlePeruuta: () => void;
    handlePassivoi: () => void;
    handlePoista: () => void;
    handleTallenna: SubmitHandler<FieldValues>;
};

const MuokkausLomake = ({
    onUusi,
    ryhma,
    handlePeruuta,
    handlePassivoi,
    handlePoista,
    handleTallenna,
}: MuokkausLomakeProps) => {
    const [i18n] = useAtom(languageAtom);
    const [kayttoRyhmatKoodisto] = useAtom(kayttoRyhmatKoodistoAtom);
    const [ryhmaTyypitKoodisto] = useAtom(ryhmaTyypitKoodistoAtom);
    const ryhmaTyypitOptions = ryhmaTyypitKoodisto.selectOptions();
    const kayttoRyhmatOptions = kayttoRyhmatKoodisto.selectOptions();
    const kayttoRyhmat = ryhma.kayttoryhmat.map((koodiUri) => kayttoRyhmatKoodisto.uri2SelectOption(koodiUri));
    const ryhmaTyypit = ryhma.ryhmatyypit.map((koodiUri) => ryhmaTyypitKoodisto.uri2SelectOption(koodiUri));
    const isDisabled = !ryhma || ryhma.status === 'PASSIIVINEN';
    const {
        register,
        formState: { errors: validationErrors },
        handleSubmit,
        control,
    } = useForm({ resolver: joiResolver(RyhmatLomakeSchema) });
    return (
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <Link to={'/ryhmat'}>
                        <IconWrapper icon={homeIcon} />
                    </Link>
                </div>
                <div>
                    <Link to={'/ryhmat'}>{i18n.translate('RYHMAT_KAIKKI_RYHMAT')}</Link>
                </div>
            </div>
            <div className={styles.PaaKehys}>
                <div className={styles.ValiContainer}>
                    <div className={styles.ValiOtsikko}>
                        <h3>{i18n.translate('RYHMAT_RYHMA')}</h3>
                        <h1>{i18n.translateNimi(ryhma.nimi)}</h1>
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
                                render={({ field: { ref: _ref, ...field } }) => (
                                    <Select
                                        id={'RYHMALOMAKE_RYHMAN_TYYPPI_SELECT'}
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
                                render={({ field: { ref: _ref, ...field } }) => (
                                    <Select
                                        id={'RYHMALOMAKE_RYHMAN_KAYTTOTARKOITUS_SELECT'}
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
                            <Button name={'passivoibutton'} variant={'outlined'} onClick={handlePassivoi}>
                                {ryhma.status === 'AKTIIVINEN'
                                    ? i18n.translate('RYHMAT_PASSIVOI_RYHMA')
                                    : i18n.translate('RYHMAT_AKTIVOI_RYHMA')}
                            </Button>
                            ,
                            <Button name={'poistabutton'} variant={'outlined'} onClick={handlePoista}>
                                {i18n.translate('RYHMAT_POISTA_RYHMA')}
                            </Button>
                        </div>
                    )}
                </div>
            </div>
            <AlaBanneri>
                <VersioContainer>{ryhma.oid && <Muokattu oid={ryhma.oid} />}</VersioContainer>
                <div>
                    <LomakeButton label={'BUTTON_SULJE'} name={'peruutabutton'} onClick={handlePeruuta} />
                    <LomakeButton
                        label={'BUTTON_TALLENNA'}
                        disabled={ryhma.status === 'PASSIIVINEN'}
                        onClick={() => {
                            handleSubmit(handleTallenna)();
                        }}
                    />
                </div>
            </AlaBanneri>
        </PohjaSivu>
    );
};

export default MuokkausLomake;
