import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import { useState, useContext } from 'react';
import type { Organisaatio, Yhteystiedot } from '../../../../../types/types';
import { LanguageContext } from '../../../../../contexts/contexts';
import { KoodiUri, Nimi } from '../../../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormRegister, UseFormWatch } from 'react-hook-form/dist/types/form';
//import { useWatch } from 'react-hook-form';

export type Props = {
    yhteystiedot?: Yhteystiedot[];
    handleOnChange?: ({
        name,
        value,
    }: {
        name: keyof Organisaatio;
        value: { nimi: Nimi; alkuPvm: string }[] | Nimi | KoodiUri[] | Date | KoodiUri | Yhteystiedot[];
    }) => void;
    validationErrors: FieldErrors<Yhteystiedot>;
    formRegister: UseFormRegister<Yhteystiedot>;
    formControl: Control<Yhteystiedot>;
    watch: UseFormWatch<Yhteystiedot>;
};
type SupportedKieli = 'finnishAndSwedish' | 'english';

const DEFAULT_LANGUAGE_CODE = 'finnishAndSwedish';
// this should be defined in some common place relating to localization

const YhteystietoLomake = ({ formRegister, validationErrors, watch }: Props): React.ReactElement => {
    const { i18n } = useContext(LanguageContext);
    const [kieleksi, setKieleksi] = useState<SupportedKieli>(DEFAULT_LANGUAGE_CODE);
    const languageTabs = [
        { value: 'finnishAndSwedish', label: i18n.translate('YHTEYSTIEDOT_KIELIVALINNAT_SUOMEKSI_JA_RUOTSIKSI') },
        { value: 'english', label: i18n.translate('YHTEYSTIEDOT_KIELIVALINNAT_ENGLANNIKSI') },
    ];
    const osoitteetOnEri = watch('osoitteetOnEri', false);
    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <RadioGroup value={kieleksi} options={languageTabs} onChange={(e) => setKieleksi(e.target.value)} />
                </div>
            </div>
            {kieleksi === 'finnishAndSwedish' ? (
                <div>
                    <div className={styles.Rivi}>
                        <Checkbox {...formRegister('osoitteetOnEri')} checked={osoitteetOnEri}>
                            {i18n.translate('YHTEYSTIEDOT_POSTIOSOITE_ON_ERI_KUIN_KAYNTIOSOITE')}
                        </Checkbox>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_POSTIOSOITE_SUOMI')} *</label>
                            <Input
                                {...formRegister(`kieli_fi#1.postiOsoite`)}
                                error={!!validationErrors['kieli_fi#1'] && validationErrors['kieli_fi#1'].postiOsoite}
                            />
                        </div>
                        <div className={styles.KenttaLyhyt}>
                            <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO_SUOMI')}</label>
                            <Input
                                {...formRegister(`kieli_fi#1.postiOsoitePostiNro`)}
                                error={
                                    validationErrors['kieli_fi#1'] &&
                                    !!validationErrors['kieli_fi#1'].postiOsoitePostiNro
                                }
                            />
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_POSTIOSOITE_RUOTSI')}</label>
                            <Input
                                {...formRegister(`kieli_sv#1.postiOsoite`)}
                                error={validationErrors['kieli_sv#1'] && !!validationErrors['kieli_sv#1'].postiOsoite}
                            />
                        </div>
                        <div className={styles.KenttaLyhyt}>
                            <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO_RUOTSI')}</label>
                            <Input
                                {...formRegister(`kieli_sv#1.postiOsoitePostiNro`)}
                                error={
                                    validationErrors['kieli_sv#1'] &&
                                    !!validationErrors['kieli_sv#1'].postiOsoitePostiNro
                                }
                            />
                        </div>
                    </div>
                    {osoitteetOnEri && [
                        <div key={'osoiteOnEri_suomi'} className={styles.Rivi}>
                            <div key={'kieli_fi#1.kayntiOsoite'} className={styles.Kentta}>
                                <label>{i18n.translate('YHTEYSTIEDOT_KAYNTIOSOITE_SUOMI')}</label>
                                <Input {...formRegister('kieli_fi#1.kayntiOsoite')} />
                            </div>
                            <div key={`kieli_fi#1.kayntiOsoitePostiNro`} className={styles.KenttaLyhyt}>
                                <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO_SUOMI')}</label>
                                <Input {...formRegister('kieli_fi#1.kayntiOsoitePostiNro')} />
                            </div>
                        </div>,
                        <div key={'osoiteOnEri_ruotsi'} className={styles.Rivi}>
                            <div key={'kieli_sv#1.kayntiOsoite'} className={styles.Kentta}>
                                <label>{i18n.translate('YHTEYSTIEDOT_KAYNTIOSOITE_RUOTSI')}</label>
                                <Input {...formRegister('kieli_sv#1.kayntiOsoite')} />
                            </div>
                            <div key={`kieli_fi#1.kayntiOsoitePostiNro`} className={styles.KenttaLyhyt}>
                                <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO_RUOTSI')}</label>
                                <Input {...formRegister('kieli_sv#1.kayntiOsoitePostiNro')} />
                            </div>
                        </div>,
                    ]}
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_PUHELINNUMERO_SUOMI')}</label>
                            <Input {...formRegister(`kieli_fi#1.puhelinnumero` as const)} />
                        </div>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_PUHELINNUMERO_RUOTSI')}</label>
                            <Input {...formRegister(`kieli_sv#1.puhelinnumero` as const)} />
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_SAHKOPOSTIOSOITE_SUOMI')} *</label>
                            <Input
                                {...formRegister(`kieli_fi#1.email` as const)}
                                error={validationErrors['kieli_fi#1'] && !!validationErrors['kieli_fi#1'].email}
                            />
                        </div>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_SAHKOPOSTIOSOITE_RUOTSI')}</label>
                            <Input
                                {...formRegister(`kieli_sv#1.email` as const)}
                                error={validationErrors['kieli_sv#1'] && !!validationErrors['kieli_sv#1'].email}
                            />
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_WWW_OSOITE_SUOMI')}</label>
                            <Input {...formRegister('kieli_fi#1.www')} />
                        </div>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_WWW_OSOITE_RUOTSI')}</label>
                            <Input {...formRegister('kieli_sv#1.www')} />
                        </div>
                    </div>
                </div>
            ) : (
                <div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_POSTIOSOITE_ENGLANTI')}</label>
                            <Input {...formRegister('kieli_en#1.postiOsoite')} />
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_SAHKOPOSTIOSOITE_ENGLANTI')}</label>
                            <Input {...formRegister('kieli_en#1.email')} />
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_WWW_OSOITE_ENGLANTI')}</label>
                            <Input {...formRegister('kieli_en#1.www')} />
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default YhteystietoLomake;
