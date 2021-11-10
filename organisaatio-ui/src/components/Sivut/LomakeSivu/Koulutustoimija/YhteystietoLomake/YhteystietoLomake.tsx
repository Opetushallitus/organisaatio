import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import { useState, useContext } from 'react';
import type { Yhteystiedot } from '../../../../../types/types';
import { KoodistoContext, LanguageContext } from '../../../../../contexts/contexts';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormRegister, UseFormSetValue, UseFormWatch } from 'react-hook-form/dist/types/form';
import { useWatch } from 'react-hook-form';
import { postinumeroSchema } from '../../../../../ValidationSchemas/YhteystietoLomakeSchema';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { YhteystiedotKortit } from './YhteystiedotKortit';
import { Kortti } from './Kortti';

export type Props = {
    setYhteystiedotValue: UseFormSetValue<Yhteystiedot>;
    yhteystiedot?: Yhteystiedot[];
    validationErrors: FieldErrors<Yhteystiedot>;
    formRegister: UseFormRegister<Yhteystiedot>;
    formControl: Control<Yhteystiedot>;
    watch: UseFormWatch<Yhteystiedot>;
};
type SupportedKieli = 'finnishAndSwedish' | 'english';

const postiOsoiteToimipaikkaFiName = 'kieli_fi#1.postiOsoiteToimipaikka';
const postiOsoiteToimipaikkaSvName = 'kieli_sv#1.postiOsoiteToimipaikka';
const kayntiOsoiteToimipaikkaSvName = 'kieli_sv#1.kayntiOsoiteToimipaikka';
const kayntiOsoiteToimipaikkaFiName = 'kieli_fi#1.kayntiOsoiteToimipaikka';

const DEFAULT_LANGUAGE_CODE = 'finnishAndSwedish';

const OsoitteenToimipaikkaKentta = ({
    name,
    control,
}: {
    name:
        | typeof postiOsoiteToimipaikkaFiName
        | typeof postiOsoiteToimipaikkaSvName
        | typeof kayntiOsoiteToimipaikkaSvName
        | typeof kayntiOsoiteToimipaikkaFiName;
    labelTxt: string;
    control: Control<Yhteystiedot>;
}) => {
    const toimipaikka = useWatch({ control, name });
    return <span className={styles.ToimipaikkaText}>{toimipaikka}</span>;
};

const YhteystietoLomake = ({
    formRegister,
    validationErrors,
    watch,
    formControl,
    setYhteystiedotValue,
}: Props): React.ReactElement => {
    const { i18n } = useContext(LanguageContext);
    const { postinumerotKoodisto } = useContext(KoodistoContext);
    const [kieleksi, setKieleksi] = useState<SupportedKieli>(DEFAULT_LANGUAGE_CODE);
    const languageTabs = [
        { value: 'finnishAndSwedish', label: i18n.translate('YHTEYSTIEDOT_KIELIVALINNAT_SUOMEKSI_JA_RUOTSIKSI') },
        { value: 'english', label: i18n.translate('YHTEYSTIEDOT_KIELIVALINNAT_ENGLANNIKSI') },
    ];

    const osoitteetOnEri = watch('osoitteetOnEri', false);

    const registerToimipaikkaUpdate = (toimipaikkaName, { onChange: originalOnchange, ...rest }) => {
        const koodit = postinumerotKoodisto.koodit();
        const kieli = toimipaikkaName.substr(toimipaikkaName.indexOf('_') + 1, 2) as 'fi' | 'sv';
        const onChange = (e) => {
            const postinumero = e.target.value;
            if (postinumeroSchema.required().validate(postinumero)) {
                const postinumeroKoodi = koodit.find((koodi) => koodi.arvo === postinumero);
                if (postinumeroKoodi) {
                    const {
                        nimi: { [kieli]: toimipaikka },
                    } = postinumeroKoodi;
                    setYhteystiedotValue(toimipaikkaName, toimipaikka);
                } else setYhteystiedotValue(toimipaikkaName, '');
            } else setYhteystiedotValue(toimipaikkaName, '');
            originalOnchange(e);
        };
        return { onChange, ...rest };
    };

    const handleAddOsoite = () => {};

    const handleRemoveOsoite = () => {};

    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <RadioGroup value={kieleksi} options={languageTabs} onChange={(e) => setKieleksi(e.target.value)} />
                </div>
                <Button>Lisää uusi osoite</Button>
                <YhteystiedotKortit osoitteet={[]} />
            </div>
            <div>
                {['fi', 'sv', 'en'].map((kieli) => (
                    <Kortti
                        kieli={kieli as 'fi' | 'sv' | 'en'}
                        setYhteystiedotValue={setYhteystiedotValue}
                        validationErrors={validationErrors}
                        formControl={formControl}
                    />
                ))}
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
                                {...registerToimipaikkaUpdate(
                                    postiOsoiteToimipaikkaFiName,
                                    formRegister(`kieli_fi#1.postiOsoitePostiNro`)
                                )}
                                error={
                                    validationErrors['kieli_fi#1'] &&
                                    !!validationErrors['kieli_fi#1'].postiOsoitePostiNro
                                }
                            />
                        </div>
                        <OsoitteenToimipaikkaKentta
                            name={postiOsoiteToimipaikkaFiName}
                            labelTxt={i18n.translate('YHTEYSTIEDOT_TOIMIPAIKKA_SUOMI')}
                            control={formControl}
                        />
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
                                {...registerToimipaikkaUpdate(
                                    postiOsoiteToimipaikkaSvName,
                                    formRegister(`kieli_sv#1.postiOsoitePostiNro`)
                                )}
                                error={
                                    validationErrors['kieli_sv#1'] &&
                                    !!validationErrors['kieli_sv#1'].postiOsoitePostiNro
                                }
                            />
                        </div>
                        <OsoitteenToimipaikkaKentta
                            name={postiOsoiteToimipaikkaSvName}
                            labelTxt={i18n.translate('YHTEYSTIEDOT_TOIMIPAIKKA_RUOTSI')}
                            control={formControl}
                        />
                    </div>
                    {osoitteetOnEri && [
                        <div key={'osoiteOnEri_suomi'} className={styles.Rivi}>
                            <div key={'kieli_fi#1.kayntiOsoite'} className={styles.Kentta}>
                                <label>{i18n.translate('YHTEYSTIEDOT_KAYNTIOSOITE_SUOMI')}</label>
                                <Input {...formRegister('kieli_fi#1.kayntiOsoite')} />
                            </div>
                            <div key={`kieli_fi#1.kayntiOsoitePostiNro`} className={styles.KenttaLyhyt}>
                                <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO_SUOMI')}</label>
                                <Input
                                    {...registerToimipaikkaUpdate(
                                        kayntiOsoiteToimipaikkaFiName,
                                        formRegister('kieli_fi#1.kayntiOsoitePostiNro')
                                    )}
                                />
                            </div>
                            <OsoitteenToimipaikkaKentta
                                name={kayntiOsoiteToimipaikkaFiName}
                                labelTxt={i18n.translate('YHTEYSTIEDOT_TOIMIPAIKKA_SUOMI')}
                                control={formControl}
                            />
                        </div>,
                        <div key={'osoiteOnEri_ruotsi'} className={styles.Rivi}>
                            <div key={'kieli_sv#1.kayntiOsoite'} className={styles.Kentta}>
                                <label>{i18n.translate('YHTEYSTIEDOT_KAYNTIOSOITE_RUOTSI')}</label>
                                <Input {...formRegister('kieli_sv#1.kayntiOsoite')} />
                            </div>
                            <div key={`kieli_fi#1.kayntiOsoitePostiNro`} className={styles.KenttaLyhyt}>
                                <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO_RUOTSI')}</label>
                                <Input
                                    {...registerToimipaikkaUpdate(
                                        kayntiOsoiteToimipaikkaSvName,
                                        formRegister('kieli_sv#1.kayntiOsoitePostiNro')
                                    )}
                                />
                            </div>
                            <OsoitteenToimipaikkaKentta
                                name={kayntiOsoiteToimipaikkaSvName}
                                labelTxt={i18n.translate('YHTEYSTIEDOT_TOIMIPAIKKA_SUOMI')}
                                control={formControl}
                            />
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
