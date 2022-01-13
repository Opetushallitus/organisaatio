import * as React from 'react';
import { useContext } from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Textarea from '@opetushallitus/virkailija-ui-components/Textarea';
import { postinumeroSchema } from '../../../../../ValidationSchemas/YhteystietoLomakeSchema';
import { Control, UseFormRegister, UseFormSetValue } from 'react-hook-form/dist/types/form';
import { Yhteystiedot } from '../../../../../types/types';
import { useWatch } from 'react-hook-form';
import { KoodistoContext } from '../../../../../contexts/KoodistoContext';
import { Kentta, KenttaLyhyt, Rivi } from '../../LomakeFields/LomakeFields';
import { ValidationResult } from 'joi';
import { LanguageContext } from '../../../../../contexts/LanguageContext';

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

type props = {
    kieli: 'fi' | 'sv' | 'en';
    yhteystiedotRegister: UseFormRegister<Yhteystiedot>;
    setYhteystiedotValue: UseFormSetValue<Yhteystiedot>;
    formControl: Control<Yhteystiedot>;
    osoitteetOnEri: boolean;
    validationErrors: ValidationResult;
    readOnly?: boolean;
    isYtj: boolean;
};

const RiviKentta = ({ label, children, isRequired = false }) => {
    return (
        <Rivi>
            <Kentta isRequired={isRequired} label={label}>
                {children}
            </Kentta>
        </Rivi>
    );
};

const PostinumeroKentta = ({ children, toimipaikkaName: name, control, label, isRequired = false }) => {
    const toimipaikka = useWatch({ control, name });
    return (
        <Rivi>
            <KenttaLyhyt isRequired label={label}>
                {children}
            </KenttaLyhyt>
            <span className={styles.ToimipaikkaText}>{toimipaikka}</span>
        </Rivi>
    );
};

const OtsikkoRivi = ({ label }) => {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.EnsimmainenRivi}>
            <h3>{i18n.translate(label)}</h3>
        </div>
    );
};

const getErrorDetails = (validationErrors) => {
    const details = validationErrors?.error?.details;
    if (details && details.length > 0) {
        const [lang, name] = details[0].path;
        return { lang, name };
    } else {
        return { lang: 'fi', name: 'no error' };
    }
};

export const YhteystietoKortti = ({
    kieli: kortinKieli,
    setYhteystiedotValue,
    validationErrors,
    formControl,
    osoitteetOnEri,
    yhteystiedotRegister,
    readOnly,
    isYtj,
}: props) => {
    const { postinumerotKoodisto } = useContext(KoodistoContext);
    const ytjReadOnly = isYtj && kortinKieli === 'fi';
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
    const error = getErrorDetails(validationErrors);
    if (kortinKieli === 'en')
        return (
            <div key={kortinKieli} className={styles.KorttiKehys}>
                <OtsikkoRivi label={`YHTEYSTIEDOTKORTTI_OTSIKKO_${kortinKieli}`} />
                <RiviKentta label="YHTEYSTIEDOT_POSTIOSOITE_MUU" isRequired>
                    <Textarea
                        disabled={readOnly}
                        {...yhteystiedotRegister(`${kortinKieli}.postiOsoite` as const)}
                        error={error.lang === kortinKieli && error.name === 'postiOsoite'}
                    />
                </RiviKentta>
                <RiviKentta label="YHTEYSTIEDOT_PUHELINNUMERO">
                    <Input
                        disabled={readOnly}
                        {...yhteystiedotRegister(`${kortinKieli}.puhelinnumero` as const)}
                        error={error.lang === kortinKieli && error.name === 'puhelinnumero'}
                    />
                </RiviKentta>
                <RiviKentta label="YHTEYSTIEDOT_SAHKOPOSTIOSOITE" isRequired>
                    <Input
                        disabled={readOnly}
                        {...yhteystiedotRegister(`${kortinKieli}.email` as const)}
                        error={error.lang === kortinKieli && error.name === 'email'}
                    />
                </RiviKentta>
                <RiviKentta label="YHTEYSTIEDOT_WWW_OSOITE">
                    <Input
                        disabled={readOnly}
                        {...yhteystiedotRegister(`${kortinKieli}.www` as const)}
                        error={error.lang === kortinKieli && error.name === 'www'}
                    />
                </RiviKentta>
            </div>
        );
    return (
        <div key={kortinKieli} className={styles.KorttiKehys}>
            <OtsikkoRivi label={`YHTEYSTIEDOTKORTTI_OTSIKKO_${kortinKieli}`} />
            <RiviKentta label="YHTEYSTIEDOT_POSTIOSOITE" isRequired>
                <Input
                    disabled={readOnly || ytjReadOnly}
                    {...yhteystiedotRegister(`${kortinKieli}.postiOsoite` as const)}
                    error={error.lang === kortinKieli && error.name === 'postiOsoite'}
                />
            </RiviKentta>
            <PostinumeroKentta
                isRequired
                label="YHTEYSTIEDOT_POSTINUMERO"
                toimipaikkaName={`${kortinKieli}.postiOsoiteToimipaikka` as OsoitteentoimipaikkaProps['name']}
                control={formControl}
            >
                <Input
                    disabled={readOnly || ytjReadOnly}
                    {...registerToimipaikkaUpdate(
                        `${kortinKieli}.postiOsoiteToimipaikka`,
                        yhteystiedotRegister(`${kortinKieli}.postiOsoitePostiNro` as const)
                    )}
                    error={error.lang === kortinKieli && error.name === 'postiOsoitePostiNro'}
                />
            </PostinumeroKentta>
            {osoitteetOnEri && [
                <RiviKentta label="YHTEYSTIEDOT_KAYNTIOSOITE">
                    <Input
                        disabled={readOnly}
                        {...yhteystiedotRegister(`${kortinKieli}.kayntiOsoite` as const)}
                        error={error.lang === kortinKieli && error.name === 'kayntiOsoite'}
                    />
                </RiviKentta>,
                <PostinumeroKentta
                    label="YHTEYSTIEDOT_POSTINUMERO'"
                    toimipaikkaName={`${kortinKieli}.kayntiOsoiteToimipaikka` as OsoitteentoimipaikkaProps['name']}
                    control={formControl}
                >
                    <Input
                        disabled={readOnly}
                        {...registerToimipaikkaUpdate(
                            `${kortinKieli}.kayntiOsoiteToimipaikka`,
                            yhteystiedotRegister(`${kortinKieli}.kayntiOsoitePostiNro` as const)
                        )}
                        error={error.lang === kortinKieli && error.name === 'kayntiOsoitePostiNro'}
                    />
                </PostinumeroKentta>,
            ]}
            <RiviKentta label="YHTEYSTIEDOT_PUHELINNUMERO">
                <Input
                    disabled={readOnly || ytjReadOnly}
                    {...yhteystiedotRegister(`${kortinKieli}.puhelinnumero` as const)}
                    name={`${kortinKieli}.puhelinnumero`}
                    error={error.lang === kortinKieli && error.name === 'puhelinnumero'}
                />
            </RiviKentta>
            <RiviKentta label="YHTEYSTIEDOT_SAHKOPOSTIOSOITE" isRequired>
                <Input
                    disabled={readOnly}
                    {...yhteystiedotRegister(`${kortinKieli}.email` as const)}
                    error={error.lang === kortinKieli && error.name === 'email'}
                />
            </RiviKentta>
            <RiviKentta label="YHTEYSTIEDOT_WWW_OSOITE">
                <Input
                    disabled={readOnly || ytjReadOnly}
                    {...yhteystiedotRegister(`${kortinKieli}.www` as const)}
                    error={error.lang === kortinKieli && error.name === 'www'}
                />
            </RiviKentta>
        </div>
    );
};
