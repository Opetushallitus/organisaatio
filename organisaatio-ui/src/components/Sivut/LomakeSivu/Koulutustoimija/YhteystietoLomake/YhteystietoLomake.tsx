import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import { useState, useContext } from 'react';
import type { Organisaatio, Yhteystiedot, YhteystiedotOsoite } from '../../../../../types/types';
import { LanguageContext } from '../../../../../contexts/contexts';
import { KoodiUri, Nimi } from '../../../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormRegister } from 'react-hook-form/dist/types/form';
import { FieldValues } from 'react-hook-form/dist/types/fields';
import { useFieldArray, useForm, useWatch } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import Joi from 'joi';

export type Props = {
    yhteystiedot: Yhteystiedot[];
    handleOnChange: ({
        name,
        value,
    }: {
        name: keyof Organisaatio;
        value: { nimi: Nimi; alkuPvm: string }[] | Nimi | KoodiUri[] | Date | KoodiUri | Yhteystiedot[];
    }) => void;
    validationErrors?: FieldErrors<FieldValues>;
    formRegister?: UseFormRegister<FieldValues>;
    formControl?: Control<FieldValues>;
};

type SupportedOsoiteType = 'kaynti' | 'posti';
type SupportedYhteystietoType = 'www' | 'email' | 'numero';
type SupportedKieli = 'kieli_fi#1' | 'kieli_sv#1' | 'kieli_en#1';

const DEFAULT_LANGUAGE_CODE = 'kieli_fi#1';
const NAME_WWW = 'www';
const NAME_EMAIL = 'email';
const NAME_PHONE = 'numero';

// this should be defined in some common place relating to localization
const SUPPORTED_LANGUAGES = [
    { value: DEFAULT_LANGUAGE_CODE, label: 'Suomeksi' },
    { value: 'kieli_sv#1', label: 'Ruotsiksi' },
    { value: 'kieli_en#1', label: 'Englanniksi' },
];

const initializeOsoite = (kieli: string, osoiteTyyppi: SupportedOsoiteType): YhteystiedotOsoite => ({
    kieli,
    osoiteTyyppi,
    postinumeroUri: '',
    postitoimipaikka: '',
    osoite: '',

});

const isOsoite = (yhteystieto: Yhteystiedot): yhteystieto is YhteystiedotOsoite =>
    yhteystieto.hasOwnProperty('osoiteTyyppi');
/*
const isSupportedYhteystietoType = (name: string): name is SupportedYhteystietoType =>
    [NAME_EMAIL, NAME_PHONE, NAME_WWW].includes(name);
*/
export const getOsoite = (
    yhteystiedot: Yhteystiedot[],
    kieli: string,
    osoiteTyyppi: SupportedOsoiteType
): YhteystiedotOsoite => {
    const found = yhteystiedot.find(
        (yhteystieto: Yhteystiedot) =>
            isOsoite(yhteystieto) && yhteystieto.kieli === kieli && yhteystieto.osoiteTyyppi === osoiteTyyppi
    );
    if (found) {
        return found as YhteystiedotOsoite;
    }
    yhteystiedot.push(initializeOsoite(kieli, osoiteTyyppi));
    return getOsoite(yhteystiedot, kieli, osoiteTyyppi);
};

export const getYhteystieto = (
    yhteystiedot: Yhteystiedot[],
    kieli: string,
    osoiteTyyppi: SupportedYhteystietoType
): Yhteystiedot => {
    const found = yhteystiedot.find(
        (yhteystieto: Yhteystiedot) => yhteystieto.kieli === kieli && yhteystieto.hasOwnProperty(osoiteTyyppi)
    );
    if (found) {
        return found as Yhteystiedot;
    }
    yhteystiedot.push({ kieli, [osoiteTyyppi]: '' } as Yhteystiedot);
    return getYhteystieto(yhteystiedot, kieli, osoiteTyyppi);
};

export const yhteystietoLomakeSchema = Joi.object({
    postiOsoite: Joi.string().required(),
    postiOsoitePostiNro: Joi.string().required(),
    kayntiOsoite: Joi.string().required(),
    kayntiOsoitePostiNro: Joi.string().required(),
    puhelinnumero: Joi.string().required(),
    email: Joi.string().required(),
    www: Joi.string().required(),
    osoiteOnEri: Joi.boolean(),
});

const WatchedInput = ({ name, control, register }) => {
    const value = useWatch({
        control,
        name,
    });
    return <Input {...register(name)} defaultValue={value} />;
};

const WatchedCheckbox = ({ control, register, labelText, name, watch, value }) => {

    watch(name, false);
    console.log('name', name)
    return (
        <Checkbox {...register(name)} checked={value}>
            {labelText}
        </Checkbox>
    );
};

const YhteystietoLomake = ({ yhteystiedot, handleOnChange, formControl, formRegister }: Props): React.ReactElement => {
    const { i18n } = useContext(LanguageContext);
    const [kieleksi, setKieleksi] = useState<SupportedKieli>(DEFAULT_LANGUAGE_CODE);
    //const [postiSamakuinKaynti, setPostiSamakuinKaynti] = useState({ kieleksi: DEFAULT_LANGUAGE_CODE, onSama: false });
    /*
    const copyAddress = (copy: boolean) => {
        if (copy) {
            const postiYt = getOsoite(yhteystiedot, kieleksi, 'posti');
            const kayntiYt = getOsoite(yhteystiedot, kieleksi, 'kaynti');
            kayntiYt.osoite = postiYt.osoite;
            kayntiYt.postinumeroUri = postiYt.postinumeroUri;
        }
        handleOnChange({ name: 'yhteystiedot', value: yhteystiedot });
    };

    const handleYhteystietoOnChange = (event: SyntheticEvent) => {
        const element = event.target as HTMLInputElement;
        yhteystietoOnChange(element.name, element.value);
    };

    const yhteystietoOnChange = (name: string, value: string) => {
        isSupportedYhteystietoType(name) ? updateField(name, value) : updateAddress(name, value);
    };

    const updateField = (name: SupportedYhteystietoType, value: string) => {
        getYhteystieto(yhteystiedot, kieleksi, name)[name] = value;
        handleOnChange({ name: 'yhteystiedot', value: yhteystiedot });
    };

    const updateAddress = (name: string, value: string) => {
        const [osoiteTyyppi, attribute] = [...name.split('.')] as [SupportedOsoiteType, keyof Osoite];
        getOsoite(yhteystiedot, kieleksi, osoiteTyyppi)[attribute] = value;
        copyAddress(postiSamakuinKaynti.onSama);
    };
*/

    const {
        watch,
        getValues,
        register,
        formState: { errors: validationErrors },
        //handleSubmit,
        control,
    } = useForm({
        defaultValues: {
            'kieli_fi#1': [
                {
                    postiOsoite: getOsoite(yhteystiedot, 'kieli_fi#1', 'posti').osoite,
                    postiOsoitePostiNro: getOsoite(yhteystiedot, 'kieli_fi#1', 'posti').postinumeroUri,
                    kayntiOsoite: getOsoite(yhteystiedot, 'kieli_fi#1', 'kaynti').osoite,
                    kayntiOsoitePostiNro: getOsoite(yhteystiedot, 'kieli_fi#1', 'kaynti').postinumeroUri,
                    puhelinnumero: getYhteystieto(yhteystiedot, 'kieli_fi#1', NAME_PHONE)[NAME_PHONE],
                    email: getYhteystieto(yhteystiedot, 'kieli_fi#1', NAME_EMAIL)[NAME_EMAIL],
                    www: getYhteystieto(yhteystiedot, 'kieli_fi#1', NAME_WWW)[NAME_WWW],
                    osoiteOnEri: false,
                },
            ],
            'kieli_sv#1': [
                {
                    postiOsoite: getOsoite(yhteystiedot, 'kieli_sv#1', 'posti').osoite,
                    postiOsoitePostiNro: getOsoite(yhteystiedot, 'kieli_fi#1', 'posti').postinumeroUri,
                    kayntiOsoite: getOsoite(yhteystiedot, 'kieli_fi#1', 'kaynti').osoite,
                    kayntiOsoitePostiNro: getOsoite(yhteystiedot, 'kieli_fi#1', 'kaynti').postinumeroUri,
                    puhelinnumero: getYhteystieto(yhteystiedot, 'kieli_fi#1', NAME_PHONE)[NAME_PHONE],
                    email: getYhteystieto(yhteystiedot, 'kieli_fi#1', NAME_EMAIL)[NAME_EMAIL],
                    www: getYhteystieto(yhteystiedot, 'kieli_fi#1', NAME_WWW)[NAME_WWW],
                    osoiteOnEri: false,
                },
            ],
            'kieli_en#1': [
                {
                    postiOsoite: getOsoite(yhteystiedot, 'kieli_sv#1', 'posti').osoite,
                    postiOsoitePostiNro: getOsoite(yhteystiedot, 'kieli_fi#1', 'posti').postinumeroUri,
                    kayntiOsoite: getOsoite(yhteystiedot, 'kieli_fi#1', 'kaynti').osoite,
                    kayntiOsoitePostiNro: getOsoite(yhteystiedot, 'kieli_fi#1', 'kaynti').postinumeroUri,
                    puhelinnumero: getYhteystieto(yhteystiedot, 'kieli_fi#1', NAME_PHONE)[NAME_PHONE],
                    email: getYhteystieto(yhteystiedot, 'kieli_fi#1', NAME_EMAIL)[NAME_EMAIL],
                    www: getYhteystieto(yhteystiedot, 'kieli_fi#1', NAME_WWW)[NAME_WWW],
                    osoiteOnEri: false,
                },
            ],
        },
        resolver: joiResolver(yhteystietoLomakeSchema),
    });

    const { fields } = useFieldArray({
        control,
        name: kieleksi,
    });
    const osoiteOnEri = (getValues()[kieleksi])[0].osoiteOnEri;
    console.log('kieliValues', osoiteOnEri);
    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <RadioGroup
                        value={kieleksi}
                        options={SUPPORTED_LANGUAGES}
                        onChange={(e) => setKieleksi(e.target.value)}
                    />
                </div>
            </div>
            {fields.map((field, index) => (
                <div key={field.id}>
                    <div className={styles.Rivi}>
                        <WatchedCheckbox
                            value={osoiteOnEri}
                            watch={watch}
                            name={`${kieleksi}.${index}.osoiteOnEri`}
                            register={register}
                            control={control}
                            labelText={i18n.translate('YHTEYSTIEDOT_POSTIOSOITE_ON_ERI_KUIN_KAYNTIOSOITE')}
                        />
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_POSTIOSOITE')}</label>
                            <WatchedInput
                                register={register}
                                control={control}
                                name={`${kieleksi}.${index}.postiOsoite`}
                            />
                            {/*<Input

                        {...register(`kieli_fi#1.${index}.postiOsoite` as const)}
                        error={!!validationErrors['kieli_fi#1.postiOsoite']}
                        name="posti.osoite"
                        //onChange={handleYhteystietoOnChange}
                        //value={getOsoite(yhteystiedot, kieleksi, 'posti').osoite}
                    />*/}
                        </div>
                        <div className={styles.KenttaLyhyt}>
                            <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO')}</label>
                            <Input
                                {...register(`${kieleksi}.${index}.postiOsoitePostiNro` as const)}
                                error={!!validationErrors[`${kieleksi}.${index}.postiOsoitePostiNro`]}
                                name="posti.postinumeroUri"
                                //onChange={handleYhteystietoOnChange}
                                //value={getOsoite(yhteystiedot, kieleksi, 'posti').postinumeroUri}
                            />
                        </div>
                    </div>
                    {[osoiteOnEri &&
                        <div className={styles.Rivi}>
                            <div key={`${kieleksi}.${index}.kayntiOsoite`} className={styles.Kentta}>
                                <label>{i18n.translate('YHTEYSTIEDOT_KAYNTIOSOITE')}</label>
                                <Input
                                    //disabled={kieleksi === postiSamakuinKaynti.kieleksi && postiSamakuinKaynti.onSama}
                                    {...register(`${kieleksi}.${index}.kayntiOsoite` as const)}
                                    error={!!validationErrors[`${kieleksi}.${index}.kayntiOsoite`]}
                                    name="kaynti.osoite"
                                    //onChange={handleYhteystietoOnChange}
                                    //value={getOsoite(yhteystiedot, kieleksi, 'kaynti').osoite}
                                />
                            </div>
                            ,
                            <div key={`${kieleksi}.${index}.kayntiOsoitePostiNro`} className={styles.KenttaLyhyt}>
                                <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO')}</label>
                                <Input
                                    {...register(`${kieleksi}.${index}.kayntiOsoitePostiNro` as const)}
                                    error={!!validationErrors[`${kieleksi}.${index}.kayntiOsoitePostiNro`]}
                                    //disabled={kieleksi === postiSamakuinKaynti.kieleksi && postiSamakuinKaynti.onSama}
                                    //onChange={handleYhteystietoOnChange}
                                    //value={getOsoite(yhteystiedot, kieleksi, 'kaynti').postinumeroUri}
                                    //name="kaynti.postinumeroUri"
                                />
                            </div>
                        </div>,
                    ]}
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_PUHELINNUMERO')}</label>
                            <Input
                                {...register(`${kieleksi}.${index}.puhelinnumero` as const)}
                                error={!!validationErrors[`${kieleksi}.${index}.puhelinnumero`]}
                                //value={getYhteystieto(yhteystiedot, kieleksi, NAME_PHONE)[NAME_PHONE]}
                                name={NAME_PHONE}
                                //onChange={handleYhteystietoOnChange}
                            />
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_SAHKOPOSTIOSOITE')}</label>
                            <Input
                                {...register(`${kieleksi}.${index}.email` as const)}
                                error={!!validationErrors[`${kieleksi}.${index}.email`]}
                                //value={getYhteystieto(yhteystiedot, kieleksi, NAME_EMAIL)[NAME_EMAIL]}
                                name={NAME_EMAIL}
                                //onChange={handleYhteystietoOnChange}
                            />
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('YHTEYSTIEDOT_WWW_OSOITE')}</label>
                            <Input
                                {...register(`${kieleksi}.${index}.www` as const)}
                                error={!!validationErrors[`${kieleksi}.${index}.www`]}
                                //value={getYhteystieto(yhteystiedot, kieleksi, NAME_WWW)[NAME_WWW]}
                                name={NAME_WWW}
                                //onChange={handleYhteystietoOnChange}
                            />
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default YhteystietoLomake;
