import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Textarea from '@opetushallitus/virkailija-ui-components/Textarea';
import { postinumeroSchema } from '../../../../../ValidationSchemas/YhteystietoLomakeSchema';
import { Control, UseFormRegister, UseFormRegisterReturn, UseFormSetValue } from 'react-hook-form';
import { KenttaError, Language, Yhteystiedot, YhteystiedotBase } from '../../../../../types/types';
import { Path, useWatch } from 'react-hook-form';
import { Kentta, KenttaLyhyt, Rivi } from '../../LomakeFields/LomakeFields';
import { ValidationResult } from 'joi';
import { useAtom } from 'jotai';
import { postinumerotKoodistoAtom } from '../../../../../api/koodisto';
import { languageAtom } from '../../../../../api/lokalisaatio';

type OsoitteentoimipaikkaProps = {
    name:
        | 'fi.postiOsoiteToimipaikka'
        | 'sv.postiOsoiteToimipaikka'
        | 'sv.kayntiOsoiteToimipaikka'
        | 'sv.kayntiOsoiteToimipaikka';
    labelTxt: string;
    control: Control<Yhteystiedot>;
};

type props = {
    kieli: Language;
    yhteystiedotRegister: UseFormRegister<Yhteystiedot>;
    setYhteystiedotValue: UseFormSetValue<Yhteystiedot>;
    formControl: Control<Yhteystiedot>;
    osoitteetOnEri: boolean;
    validationErrors: ValidationResult;
    readOnly?: boolean;
    isYtj: boolean;
};

const RiviKentta = ({
    error,
    label,
    children,
    isRequired = false,
}: React.PropsWithChildren<{ error?: KenttaError; label: string; isRequired?: boolean }>) => {
    return (
        <Rivi>
            <Kentta isRequired={isRequired} error={error} label={label}>
                {children}
            </Kentta>
        </Rivi>
    );
};

type PostinumeroKenttaProps = {
    children: React.ReactNode;
    toimipaikkaName: OsoitteentoimipaikkaProps['name'];
    isRequired?: boolean;
    control: Control<Yhteystiedot>;
    label: string;
    error: KenttaError;
};

const PostinumeroKentta = ({
    children,
    toimipaikkaName: name,
    control,
    label,
    isRequired,
    error,
}: PostinumeroKenttaProps) => {
    const toimipaikka = useWatch({ control, name });
    return (
        <Rivi>
            <KenttaLyhyt isRequired={isRequired ?? false} label={label} error={error}>
                {children}
            </KenttaLyhyt>
            <span className={styles.ToimipaikkaText}>{toimipaikka}</span>
        </Rivi>
    );
};

const OtsikkoRivi = ({ label }: { label: string }) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <div className={styles.EnsimmainenRivi}>
            <h3>{i18n.translate(label)}</h3>
        </div>
    );
};


type YhteystietoField = keyof YhteystiedotBase;

function hasFieldError(
    validationResult: ValidationResult,
    language: Language,
    name: YhteystietoField
): boolean {
    return (
        validationResult.error?.details.some(
            (detail) =>
                detail.path[0] === language &&
                detail.path[1] === name
        ) ?? false
    );
}

function getKenttaError(
    validationResult: ValidationResult,
    language: Language,
    name: YhteystietoField
): KenttaError {
    return {
        ref: {
            name: hasFieldError(validationResult, language, name)
                ? name
                : undefined,
        },
    };
}

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
    const [postinumerotKoodisto] = useAtom(postinumerotKoodistoAtom);
    const ytjReadOnly = isYtj && kortinKieli === 'fi';
    const registerToimipaikkaUpdate = (
        toimipaikkaName: Path<Yhteystiedot>,
        { onChange: originalOnchange, ...rest }: UseFormRegisterReturn
    ) => {
        const koodit = postinumerotKoodisto.koodit();
        const kieli = toimipaikkaName.substr(toimipaikkaName.indexOf('_') + 1, 2) as 'fi' | 'sv';
        const onChange = (e: React.ChangeEvent<HTMLInputElement>) => {
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
    const hasError = (name: YhteystietoField): boolean =>
        hasFieldError(validationErrors, kortinKieli, name);

    const errorFor = (name: YhteystietoField): KenttaError =>
        getKenttaError(validationErrors, kortinKieli, name);
    if (kortinKieli === 'en')
        return (
            <div className={styles.KorttiKehys}>
                <OtsikkoRivi label={`YHTEYSTIEDOTKORTTI_OTSIKKO_${kortinKieli}`} />
                <RiviKentta
                    label="YHTEYSTIEDOT_POSTIOSOITE_MUU"
                    isRequired
                    error={errorFor('postiOsoite')}
                >
                    <Textarea
                        disabled={readOnly}
                        {...yhteystiedotRegister(`${kortinKieli}.postiOsoite` as const)}
                        error={hasError('postiOsoite')}
                    />
                </RiviKentta>
                <RiviKentta label="YHTEYSTIEDOT_PUHELINNUMERO">
                    <Input
                        disabled={readOnly}
                        {...yhteystiedotRegister(`${kortinKieli}.puhelinnumero` as const)}
                        
                    />
                </RiviKentta>
                <RiviKentta
                    label="YHTEYSTIEDOT_SAHKOPOSTIOSOITE"
                    isRequired
                    error={errorFor('email')}
                >
                    <TietosuojeselosteLinkki />
                    <Input
                        disabled={readOnly}
                        {...yhteystiedotRegister(`${kortinKieli}.email` as const)}
                        error={hasError('email')}
                    />
                </RiviKentta>
                <RiviKentta label="YHTEYSTIEDOT_WWW_OSOITE">
                    <Input
                        disabled={readOnly}
                        {...yhteystiedotRegister(`${kortinKieli}.www` as const)}
                        error={hasError('www')}
                    />
                </RiviKentta>
            </div>
        );
    return (
        <div className={styles.KorttiKehys}>
            <OtsikkoRivi label={`YHTEYSTIEDOTKORTTI_OTSIKKO_${kortinKieli}`} />
            <RiviKentta label="YHTEYSTIEDOT_POSTIOSOITE" isRequired error={errorFor('postiOsoite')}>
                <Input
                    disabled={readOnly || ytjReadOnly}
                    {...yhteystiedotRegister(`${kortinKieli}.postiOsoite` as const)}
                    error={hasError('postiOsoite')}
                />
            </RiviKentta>
            <PostinumeroKentta
                isRequired
                label="YHTEYSTIEDOT_POSTINUMERO"
                toimipaikkaName={`${kortinKieli}.postiOsoiteToimipaikka` as OsoitteentoimipaikkaProps['name']}
                control={formControl}
                error={errorFor('postiOsoitePostiNro')}
            >
                <Input
                    disabled={readOnly || ytjReadOnly}
                    {...registerToimipaikkaUpdate(
                        `${kortinKieli}.postiOsoiteToimipaikka`,
                        yhteystiedotRegister(`${kortinKieli}.postiOsoitePostiNro` as const)
                    )}
                    error={hasError('postiOsoitePostiNro')}
                />
            </PostinumeroKentta>
            {osoitteetOnEri && [
                <RiviKentta key={`${kortinKieli}-kaynti`} label="YHTEYSTIEDOT_KAYNTIOSOITE">
                    <Input
                        disabled={readOnly}
                        {...yhteystiedotRegister(`${kortinKieli}.kayntiOsoite` as const)}
                        error={hasError('kayntiOsoite')}
                    />
                </RiviKentta>,
                <PostinumeroKentta
                    key={`${kortinKieli}-postinro`}
                    label="YHTEYSTIEDOT_POSTINUMERO'"
                    toimipaikkaName={`${kortinKieli}.kayntiOsoiteToimipaikka` as OsoitteentoimipaikkaProps['name']}
                    control={formControl}
                    error={errorFor('kayntiOsoitePostiNro')}
                >
                    <Input
                        disabled={readOnly}
                        {...registerToimipaikkaUpdate(
                            `${kortinKieli}.kayntiOsoiteToimipaikka`,
                            yhteystiedotRegister(`${kortinKieli}.kayntiOsoitePostiNro` as const)
                        )}
                        error={hasError('kayntiOsoitePostiNro')}
                    />
                </PostinumeroKentta>,
            ]}
            <RiviKentta label="YHTEYSTIEDOT_PUHELINNUMERO">
                <Input
                    disabled={readOnly || ytjReadOnly}
                    {...yhteystiedotRegister(`${kortinKieli}.puhelinnumero` as const)}
                    name={`${kortinKieli}.puhelinnumero`}
                    error={hasError('puhelinnumero')}
                />
            </RiviKentta>
            <RiviKentta label="YHTEYSTIEDOT_SAHKOPOSTIOSOITE" isRequired error={errorFor('email')}>
                <TietosuojeselosteLinkki />
                <Input
                    disabled={readOnly}
                    {...yhteystiedotRegister(`${kortinKieli}.email` as const)}
                    error={hasError('email')}
                />
            </RiviKentta>
            <RiviKentta label="YHTEYSTIEDOT_WWW_OSOITE">
                <Input
                    disabled={readOnly}
                    {...yhteystiedotRegister(`${kortinKieli}.www` as const)}
                    error={hasError('www')}
                />
            </RiviKentta>
        </div>
    );
};

function TietosuojeselosteLinkki() {
    const [i18n] = useAtom(languageAtom);
    const url = i18n.translate('SAHKOPOSTI_TIETOSUOJASELOSTE_LINKKI_URL');
    return (
        <a className={styles.TietosuojaLinkki} href={url} target="_blank" rel="noreferrer">
            {i18n.translate('SAHKOPOSTI_TIETOSUOJASELOSTE_LINKKI')}
        </a>
    );
}
