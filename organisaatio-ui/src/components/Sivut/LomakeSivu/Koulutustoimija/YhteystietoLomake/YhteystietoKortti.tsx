import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Textarea from '@opetushallitus/virkailija-ui-components/Textarea';
import { postinumeroSchema } from '../../../../../ValidationSchemas/YhteystietoLomakeSchema';
import { Control, UseFormRegister, UseFormSetValue } from 'react-hook-form/dist/types/form';
import { Yhteystiedot } from '../../../../../types/types';
import { useWatch } from 'react-hook-form';
import { useContext } from 'react';
import { KoodistoContext, LanguageContext } from '../../../../../contexts/contexts';
import { FieldErrors } from 'react-hook-form/dist/types/errors';

const postiOsoiteToimipaikkaFiName = 'fi.postiOsoiteToimipaikka';
const postiOsoiteToimipaikkaSvName = 'sv.postiOsoiteToimipaikka';
const kayntiOsoiteToimipaikkaSvName = 'sv.kayntiOsoiteToimipaikka';
const kayntiOsoiteToimipaikkaFiName = 'fi.kayntiOsoiteToimipaikka';

type OsoitteentoimipaikkaProps = {
    name:
        | typeof postiOsoiteToimipaikkaFiName
        | typeof postiOsoiteToimipaikkaSvName
        | typeof kayntiOsoiteToimipaikkaSvName
        | typeof kayntiOsoiteToimipaikkaFiName;
    labelTxt: string;
    control: Control<Yhteystiedot>;
};

const OsoitteenToimipaikkaKentta = ({ name, control }: OsoitteentoimipaikkaProps) => {
    const toimipaikka = useWatch({ control, name });
    return <span className={styles.ToimipaikkaText}>{toimipaikka}</span>;
};

type props = {
    isFirst: boolean;
    kieli: 'fi' | 'sv' | 'en';
    yhteystiedotRegister: UseFormRegister<Yhteystiedot>;
    setYhteystiedotValue: UseFormSetValue<Yhteystiedot>;
    validationErrors: FieldErrors<Yhteystiedot>;
    formControl: Control<Yhteystiedot>;
    osoitteetOnEri: boolean;
};

export const YhteystietoKortti = ({
    isFirst,
    kieli,
    setYhteystiedotValue,
    validationErrors,
    formControl,
    osoitteetOnEri,
    yhteystiedotRegister,
}: props) => {
    const { i18n } = useContext(LanguageContext);
    const { postinumerotKoodisto } = useContext(KoodistoContext);
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
    const errorVisible: boolean = isFirst && !!Object.keys(validationErrors).length;

    if (kieli === 'en')
        return (
            <div key={kieli} className={styles.KorttiKehys}>
                {' '}
                <div className={styles.EnsimmainenRivi}>
                    <h3>{kieli}</h3>
                </div>
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('YHTEYSTIEDOT_POSTIOSOITE')} *</label>
                        <Textarea {...yhteystiedotRegister(`${kieli}.postiOsoite` as const)} error={errorVisible} />
                    </div>
                </div>
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('YHTEYSTIEDOT_PUHELINNUMERO')}</label>
                        <Input name={`${kieli}.puhelinnumero`} />
                    </div>
                </div>
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('YHTEYSTIEDOT_SAHKOPOSTIOSOITE')} *</label>
                        <Input {...yhteystiedotRegister(`${kieli}.email` as const)} error={errorVisible} />
                    </div>
                </div>
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('YHTEYSTIEDOT_WWW_OSOITE')}</label>
                        <Input {...yhteystiedotRegister(`${kieli}.www` as const)} />
                    </div>
                </div>
            </div>
        );
    return (
        <div key={kieli} className={styles.KorttiKehys}>
            <div className={styles.EnsimmainenRivi}>
                <h3>{kieli}</h3>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHTEYSTIEDOT_POSTIOSOITE')} *</label>
                    <Input {...yhteystiedotRegister(`${kieli}.postiOsoite` as const)} error={errorVisible} />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.KenttaLyhyt}>
                    <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO')}</label>
                    <Input
                        {...registerToimipaikkaUpdate(
                            `${kieli}.postiOsoiteToimipaikka`,
                            yhteystiedotRegister(`${kieli}.postiOsoitePostiNro` as const)
                        )}
                        error={errorVisible}
                    />
                </div>
                <OsoitteenToimipaikkaKentta
                    name={`${kieli}.postiOsoiteToimipaikka` as OsoitteentoimipaikkaProps['name']}
                    labelTxt={i18n.translate('YHTEYSTIEDOT_TOIMIPAIKKA')}
                    control={formControl}
                />
            </div>
            {osoitteetOnEri && [
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('YHTEYSTIEDOT_KAYNTIOSOITE')}</label>
                        <Input {...yhteystiedotRegister(`${kieli}.kayntiOsoite` as const)} />
                    </div>
                </div>,
                <div className={styles.Rivi}>
                    <div className={styles.KenttaLyhyt}>
                        <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO')}</label>
                        <Input
                            {...registerToimipaikkaUpdate(
                                `${kieli}.kayntiOsoiteToimipaikka`,
                                yhteystiedotRegister(`${kieli}.kayntiOsoitePostiNro` as const)
                            )}
                            error={errorVisible}
                        />
                    </div>
                    <OsoitteenToimipaikkaKentta
                        name={`${kieli}.kayntiOsoiteToimipaikka` as OsoitteentoimipaikkaProps['name']}
                        labelTxt={i18n.translate('YHTEYSTIEDOT_TOIMIPAIKKA')}
                        control={formControl}
                    />
                </div>,
            ]}
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHTEYSTIEDOT_PUHELINNUMERO')}</label>
                    <Input name={`${kieli}.puhelinnumero`} />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHTEYSTIEDOT_SAHKOPOSTIOSOITE')} *</label>
                    <Input {...yhteystiedotRegister(`${kieli}.email` as const)} error={errorVisible} />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHTEYSTIEDOT_WWW_OSOITE')}</label>
                    <Input {...yhteystiedotRegister(`${kieli}.www` as const)} />
                </div>
            </div>
        </div>
    );
};
