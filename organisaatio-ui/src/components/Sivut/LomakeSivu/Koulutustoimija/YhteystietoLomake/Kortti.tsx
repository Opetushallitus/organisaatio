import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import { postinumeroSchema } from '../../../../../ValidationSchemas/YhteystietoLomakeSchema';
import { Control, UseFormSetValue } from 'react-hook-form/dist/types/form';
import { Yhteystiedot } from '../../../../../types/types';
import { useWatch } from 'react-hook-form';
import { useContext } from 'react';
import { KoodistoContext, LanguageContext } from '../../../../../contexts/contexts';
import { FieldErrors } from 'react-hook-form/dist/types/errors';

const postiOsoiteToimipaikkaFiName = 'kieli_fi#1.postiOsoiteToimipaikka';
const postiOsoiteToimipaikkaSvName = 'kieli_sv#1.postiOsoiteToimipaikka';
const kayntiOsoiteToimipaikkaSvName = 'kieli_sv#1.kayntiOsoiteToimipaikka';
const kayntiOsoiteToimipaikkaFiName = 'kieli_fi#1.kayntiOsoiteToimipaikka';

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
    kieli: 'fi' | 'sv' | 'en';
    setYhteystiedotValue: UseFormSetValue<Yhteystiedot>;
    validationErrors: FieldErrors<Yhteystiedot>;
    formControl: Control<Yhteystiedot>;
};
const mapToimipaikkaName = (name: string, kieli: props['kieli']): OsoitteentoimipaikkaProps['name'] =>
    `${kieli}${name.search('kaynti') > -1 ? 'kaynti' : 'posti'}OsoiteToimipaikka` as OsoitteentoimipaikkaProps['name'];

export const Kortti = ({ kieli, setYhteystiedotValue, validationErrors, formControl }: props) => {
    const { i18n } = useContext(LanguageContext);
    const { postinumerotKoodisto } = useContext(KoodistoContext);
    const toimipaikkaOnChange = (e): void => {
        const koodit = postinumerotKoodisto.koodit();
        const { value: postinumero, name } = e.target;
        const toimipaikkaName = mapToimipaikkaName(name, kieli);
        if (postinumeroSchema.required().validate(postinumero)) {
            const postinumeroKoodi = koodit.find((koodi) => koodi.arvo === postinumero);
            if (postinumeroKoodi) {
                const {
                    nimi: { [kieli]: toimipaikka },
                } = postinumeroKoodi;
                setYhteystiedotValue(toimipaikkaName, toimipaikka as string);
            } else setYhteystiedotValue(toimipaikkaName, '');
        } else setYhteystiedotValue(toimipaikkaName, '');
    };
    return (
        <div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHTEYSTIEDOT_POSTIOSOITE')} *</label>
                    <Input name={`${kieli}.postiOsoite`} error={!!validationErrors[kieli]?.postiOsoite} />
                </div>
                <div className={styles.KenttaLyhyt}>
                    <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO')}</label>
                    <Input
                        name={`${kieli}.postiOsoitePostiNro`}
                        onChange={toimipaikkaOnChange}
                        error={!!validationErrors[kieli]?.postiOsoitePostiNro}
                    />
                </div>
                <OsoitteenToimipaikkaKentta
                    name={`${kieli}.postiOsoiteToimipaikka` as OsoitteentoimipaikkaProps['name']}
                    labelTxt={i18n.translate('YHTEYSTIEDOT_TOIMIPAIKKA')}
                    control={formControl}
                />
            </div>
        </div>
    );
};
