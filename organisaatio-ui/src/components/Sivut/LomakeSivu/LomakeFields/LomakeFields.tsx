import styles from './LomakeFields.module.css';
import React, { ReactNode } from 'react';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../../api/lokalisaatio';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import IconWrapper from '../../../IconWapper/IconWrapper';
import { FieldError, Path, PathValue, UseFormRegister, UseFormRegisterReturn, UseFormSetValue } from 'react-hook-form';
import { KenttaError, LocalDate, Nimi } from '../../../../types/types';

const UloinKehys = (props: { children: ReactNode }) => <div className={styles.UloinKehys}>{props.children}</div>;
const YlaBanneri = (props: { children: ReactNode }) => <div className={styles.YlaBanneri}>{props.children}</div>;
const ValiContainer = (props: { children: ReactNode }) => <div className={styles.ValiContainer}>{props.children}</div>;
const ValiOtsikko = (props: { children: ReactNode }) => <div className={styles.ValiOtsikko}>{props.children}</div>;
const PaaOsio = (props: { children: ReactNode }) => <div className={styles.PaaOsio}>{props.children}</div>;
const AlaBanneri = (props: { children: ReactNode }) => <div className={styles.AlaBanneri}>{props.children}</div>;
const VersioContainer = (props: { children: ReactNode }) => (
    <div className={styles.VersioContainer}>{props.children}</div>
);
const ValiNappulat = (props: { children: ReactNode }) => <div className={styles.ValiNappulat}>{props.children}</div>;
const MuokattuKolumni = (props: { children: ReactNode }) => (
    <div className={styles.MuokattuKolumni}>{props.children}</div>
);
const Ruudukko = (props: { children: ReactNode }) => <div className={styles.Ruudukko}>{props.children}</div>;
const Rivi = (props: { children: ReactNode }) => <div className={styles.Rivi}>{props.children}</div>;
const ErrorWrapper: React.FC<{ error?: KenttaError[] }> = ({ error = [], children }) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <>
            {children}
            {error.map((e) =>
                e?.ref?.name ? (
                    <div key={e.ref.name} style={{ color: '#e44e4e', paddingBottom: '0.5rem', paddingLeft: '0.5rem' }}>
                        {i18n.translate(`${e.ref.name}.virheellinen`, false)}
                    </div>
                ) : undefined
            )}
        </>
    );
};
const AvainKevyestiBoldattu = ({ label, translate = true }: { label: string; translate?: boolean }) => {
    const [i18n] = useAtom(languageAtom);
    return <span className={styles.AvainKevyestiBoldattu}>{translate ? i18n.translate(label) : label}</span>;
};
const ReadOnly = ({ value }: { value: string | number }) => {
    return <span className={styles.Kentta}>{value}</span>;
};
const ReadOnlyNimi = ({ value: nimi }: { value: Nimi }) => {
    const nimiKeys = Object.entries(nimi || {})
        .map(([key, value]) => `${value} [${key}]`)
        .join(', ');
    return (
        <div>
            <span>{nimiKeys}</span>
        </div>
    );
};
const LabelLink = ({ value, to }: { value: string; to: string }) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <a href={to} target={'_blank'} rel={'noreferrer'}>
            {i18n.translate(value)}
        </a>
    );
};
const ReadOnlyDate = ({ value }: { value: LocalDate }) => {
    return <div className={styles.Kentta}>{value}</div>;
};
const Kentta: React.FC<{ error?: KenttaError | KenttaError[]; label: string; isRequired?: boolean }> = ({
    error,
    label,
    children,
    isRequired = false,
}) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <div className={styles.Kentta}>
            <ErrorWrapper error={([] as KenttaError[]).concat(error || [])}>
                <label className={isRequired ? styles.Required : undefined}>{i18n.translate(label)}</label>
                {children}
            </ErrorWrapper>
        </div>
    );
};
const NimiKentta = ({
    label,
    id,
    formRegisterReturn,
    field,
    error,
    copyToNames,
}: {
    label: string;
    id: string;
    field: keyof Nimi;
    formRegisterReturn: UseFormRegisterReturn;
    error?: FieldError;
    copyToNames?: (field: keyof Nimi) => void;
}) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <div className={styles.NimiKentta}>
            <ErrorWrapper error={([] as KenttaError[]).concat(error || [])}>
                <label className={styles.Required}>{i18n.translate(label)}</label>
                <Input
                    error={!!error}
                    id={id}
                    {...formRegisterReturn}
                    defaultValue={''}
                    suffix={
                        copyToNames && (
                            <div title={i18n.translate('KOPIOI_MUIHIN_NIMIIN')} onClick={() => copyToNames(field)}>
                                <IconWrapper
                                    icon="ci:copy"
                                    color={'gray'}
                                    height={'1.5rem'}
                                    name={'KOPIOI_MUIHIN_NIMIIN'}
                                />
                            </div>
                        )
                    }
                />
            </ErrorWrapper>
        </div>
    );
};

type NimiGroupFieldValues = { nimi?: Nimi };

type NimiGroupProps<T extends NimiGroupFieldValues> = {
    error?: { fi?: FieldError; sv?: FieldError; en?: FieldError };
    register: UseFormRegister<T>;
    getValues: () => T;
    setValue: UseFormSetValue<T>;
};

const NimiGroup = <T extends NimiGroupFieldValues>({ error, register, getValues, setValue }: NimiGroupProps<T>) => {
    const copyToNames = (field: keyof Nimi): void => {
        const muutosTiedot = getValues();
        setValue('nimi.sv' as Path<T>, muutosTiedot.nimi?.[field] as PathValue<T, Path<T>>);
        setValue('nimi.en' as Path<T>, muutosTiedot.nimi?.[field] as PathValue<T, Path<T>>);
    };
    return (
        <>
            {' '}
            <NimiKentta
                label={'LABEL_SUOMEKSI'}
                error={error?.fi}
                id={'organisaation_nimiFi'}
                field={'fi'}
                formRegisterReturn={register('nimi.fi' as Path<T>)}
                copyToNames={copyToNames}
            />
            <NimiKentta
                label={'LABEL_RUOTSIKSI'}
                error={error?.sv}
                id={'organisaation_nimiSv'}
                field={'sv'}
                formRegisterReturn={register('nimi.sv' as Path<T>)}
            />
            <NimiKentta
                label={'LABEL_ENGLANNIKSI'}
                error={error?.en}
                id={'organisaation_nimiEn'}
                field={'en'}
                formRegisterReturn={register('nimi.en' as Path<T>)}
            />
        </>
    );
};
const KenttaLyhyt = ({
    label,
    children,
    isRequired = false,
    error,
}: {
    label: string;
    children: ReactNode;
    isRequired?: boolean;
    error: KenttaError;
}) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <div className={styles.KenttaLyhyt}>
            <ErrorWrapper error={([] as KenttaError[]).concat(error || [])}>
                <label className={isRequired ? styles.Required : undefined}>{i18n.translate(label)}</label>
                {children}
            </ErrorWrapper>
        </div>
    );
};
type LomakeButtonProps = {
    onClick: () => void;
    label: string;
    disabled?: boolean;
    name?: string;
    icon?: () => ReactNode;
};
const LomakeButton = (props: LomakeButtonProps) => {
    return <LomakeIconButton {...props} />;
};
type LomakeIconButtonProps = LomakeButtonProps;
const LomakeIconButton = ({ onClick, label, icon, disabled, name }: LomakeIconButtonProps) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <Button variant={'outlined'} onClick={onClick} disabled={disabled} name={name}>
            {!!icon && <div className={`${styles.IconContainer}`}>{icon()}</div>}
            {i18n.translate(label)}
        </Button>
    );
};

const HiddenForm = () => {
    const [i18n] = useAtom(languageAtom);
    return (
        <UloinKehys>
            <h3>{i18n.translate('KATKETTY_LOMAKE')}</h3>
        </UloinKehys>
    );
};

export {
    UloinKehys,
    YlaBanneri,
    ValiContainer,
    ValiOtsikko,
    PaaOsio,
    AlaBanneri,
    VersioContainer,
    MuokattuKolumni,
    ValiNappulat,
    Ruudukko,
    Rivi,
    Kentta,
    KenttaLyhyt,
    NimiKentta,
    NimiGroup,
    AvainKevyestiBoldattu,
    ReadOnly,
    ReadOnlyNimi,
    ReadOnlyDate,
    LomakeButton,
    LabelLink,
    HiddenForm,
};
