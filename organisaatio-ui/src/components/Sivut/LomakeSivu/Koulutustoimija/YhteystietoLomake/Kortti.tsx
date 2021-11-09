import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import { postinumeroSchema } from '../../../../../ValidationSchemas/YhteystietoLomakeSchema';
import { Control } from 'react-hook-form/dist/types/form';
import { Yhteystiedot } from '../../../../../types/types';
import { useWatch } from 'react-hook-form';
import { useContext } from 'react';
import { KoodistoContext, LanguageContext } from '../../../../../contexts/contexts';

const postiOsoiteToimipaikkaFiName = 'kieli_fi#1.postiOsoiteToimipaikka';
const postiOsoiteToimipaikkaSvName = 'kieli_sv#1.postiOsoiteToimipaikka';
const kayntiOsoiteToimipaikkaSvName = 'kieli_sv#1.kayntiOsoiteToimipaikka';
const kayntiOsoiteToimipaikkaFiName = 'kieli_fi#1.kayntiOsoiteToimipaikka';

type OsoitteentoimipaikkaProps =
{
    name:
        | typeof postiOsoiteToimipaikkaFiName
        | typeof postiOsoiteToimipaikkaSvName
        | typeof kayntiOsoiteToimipaikkaSvName
        | typeof kayntiOsoiteToimipaikkaFiName;
    labelTxt: string;
    control: Control<Yhteystiedot>;
};

const OsoitteenToimipaikkaKentta = ({
    name,
    control,
}: OsoitteentoimipaikkaProps) => {
    const toimipaikka = useWatch({ control, name });
    return <span className={styles.ToimipaikkaText}>{toimipaikka}</span>;
};

type props = {
    kieli: 'kieli_fi#1' | 'kieli_sv#1' | 'kieli_en#1';
    toimipaikkaName: string;
    setYhteystiedotValue:
};
export const Kortti = ({ kieli, toimipaikkaName, setYhteystiedotValue, validationErrors, formControl }: props) => {
    const { i18n } = useContext(LanguageContext);
    const { postinumerotKoodisto } = useContext(KoodistoContext);
    const toimipaikkaOnChange = (): void => {
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
        };
    };
    return (
        <div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('YHTEYSTIEDOT_POSTIOSOITE_SUOMI')} *</label>
                    <Input
                        name={`${kieli}.postiOsoite`}
                        error={!!validationErrors['kieli_fi#1'] && validationErrors['kieli_fi#1'].postiOsoite}
                    />
                </div>
                <div className={styles.KenttaLyhyt}>
                    <label>{i18n.translate('YHTEYSTIEDOT_POSTINUMERO_SUOMI')}</label>
                    <Input
                        name={`${kieli}.postiOsoitePostiNro`}
                        onChange={toimipaikkaOnChange}
                        error={validationErrors['kieli_fi#1'] && !!validationErrors['kieli_fi#1'].postiOsoitePostiNro}
                    />
                </div>
                <OsoitteenToimipaikkaKentta
                    name={`${kieli}.postiOsoiteToimipaikka` as OsoitteentoimipaikkaProps['name']}
                    labelTxt={i18n.translate('YHTEYSTIEDOT_TOIMIPAIKKA_SUOMI')}
                    control={formControl}
                />
            </div>
        </div>
    );
};
