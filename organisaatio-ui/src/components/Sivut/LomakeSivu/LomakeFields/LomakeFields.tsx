import styles from './LomakeFields.module.css';
import * as React from 'react';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../../api/lokalisaatio';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import IconWrapper from '../../../IconWapper/IconWrapper';
import { FieldError, Path } from 'react-hook-form';
import { Nimi } from '../../../../types/types';

const UloinKehys = (props) => <div className={styles.UloinKehys}>{props.children}</div>;
const YlaBanneri = (props) => <div className={styles.YlaBanneri}>{props.children}</div>;
const ValiContainer = (props) => <div className={styles.ValiContainer}>{props.children}</div>;
const ValiOtsikko = (props) => <div className={styles.ValiOtsikko}>{props.children}</div>;
const PaaOsio = (props) => <div className={styles.PaaOsio}>{props.children}</div>;
const AlaBanneri = (props) => <div className={styles.AlaBanneri}>{props.children}</div>;
const VersioContainer = (props) => <div className={styles.VersioContainer}>{props.children}</div>;
const ValiNappulat = (props) => <div className={styles.ValiNappulat}>{props.children}</div>;
const MuokattuKolumni = (props) => <div className={styles.MuokattuKolumni}>{props.children}</div>;
const Ruudukko = (props) => <div className={styles.Ruudukko}>{props.children}</div>;
const Rivi = (props) => <div className={styles.Rivi}>{props.children}</div>;
const AvainKevyestiBoldattu = ({ label, translate = true }) => {
    const [i18n] = useAtom(languageAtom);
    return <span className={styles.AvainKevyestiBoldattu}>{translate ? i18n.translate(label) : label}</span>;
};
const ReadOnly = ({ value }) => {
    return <span className={styles.Kentta}>{value}</span>;
};
const ReadOnlyNimi = ({ value: nimi }) => {
    const nimiKeys = Object.entries(nimi || {})
        .map(([key, value]) => `${value} [${key}]`)
        .join(', ');
    return (
        <div>
            <span>{nimiKeys}</span>
        </div>
    );
};
const LabelLink = ({ value, to }) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <a href={to} target={'_blank'} rel={'noreferrer'}>
            {i18n.translate(value)}
        </a>
    );
};
const ReadOnlyDate = ({ value }) => {
    return <div className={styles.Kentta}>{value}</div>;
};
const Kentta = ({ label, children, isRequired = false }) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <div className={styles.Kentta}>
            <label>
                {i18n.translate(label)} {isRequired && '*'}
            </label>
            {children}
        </div>
    );
};
const NimiKentta = ({
    label,
    id,
    formRegister,
    field,
    error,
    copyToNames,
}: {
    label: string;
    id: string;
    field: Path<Nimi>;
    formRegister: any;
    error?: FieldError;
    copyToNames?: (field: Path<Nimi>) => void;
}) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <Kentta isRequired label={label}>
            <Input
                error={!!error}
                id={id}
                {...formRegister(`nimi.${field}`)}
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
        </Kentta>
    );
};
const KenttaLyhyt = ({ label, children, isRequired = false }) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <div className={styles.KenttaLyhyt}>
            <label>
                {i18n.translate(label)} {isRequired && '*'}
            </label>
            {children}
        </div>
    );
};

const LomakeButton = (props) => {
    return <LomakeIconButton icon={undefined} {...props} />;
};
const LomakeIconButton = ({ onClick, label, icon, ...rest }) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <Button variant={'outlined'} onClick={onClick} {...rest}>
            {!!icon && <div className={`${styles.IconContainer}`}>{icon()}</div>}
            {i18n.translate(label)}
        </Button>
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
    AvainKevyestiBoldattu,
    ReadOnly,
    ReadOnlyNimi,
    ReadOnlyDate,
    LomakeButton,
    LabelLink,
};
