import styles from './LomakeFields.module.css';
import { useContext } from 'react';
import { LanguageContext } from '../../../../contexts/contexts';
import * as React from 'react';
import Button from '@opetushallitus/virkailija-ui-components/Button';

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
const AvainKevyestiBoldattu = ({ label }) => {
    const { i18n } = useContext(LanguageContext);
    return <span className={styles.AvainKevyestiBoldattu}>{i18n.translate(label)}</span>;
};
const ReadOnly = ({ value }) => {
    return <span className={styles.Kentta}>{value}</span>;
};
const ReadOnlyNimi = ({ value }) => {
    const { i18n } = useContext(LanguageContext);
    return <span className={styles.Kentta}>{i18n.translateNimi(value)}</span>;
};
const Kentta = ({ label, children }) => {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.Kentta}>
            <label>{i18n.translate(label)}</label>
            {children}
        </div>
    );
};
const KenttaLyhyt = ({ label, children }) => {
    const { i18n } = useContext(LanguageContext);
    return (
        <div className={styles.KenttaLyhyt}>
            <label>{i18n.translate(label)}</label>
            {children}
        </div>
    );
};
const LomakeButton = ({ onClick, label }) => {
    const { i18n } = useContext(LanguageContext);
    return (
        <Button className={styles.Nappi} variant="outlined" onClick={onClick}>
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
    AvainKevyestiBoldattu,
    ReadOnly,
    ReadOnlyNimi,
    LomakeButton,
};