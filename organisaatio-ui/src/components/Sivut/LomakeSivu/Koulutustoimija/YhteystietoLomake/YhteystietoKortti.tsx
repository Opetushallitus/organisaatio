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

type props = {
    kieli: 'fi' | 'sv' | 'en';
    yhteystiedotRegister: UseFormRegister<Yhteystiedot>;
    setYhteystiedotValue: UseFormSetValue<Yhteystiedot>;
    formControl: Control<Yhteystiedot>;
    osoitteetOnEri: boolean;
};

const Kentta = ({ label, children }) => {
    return (
        <div className={styles.Rivi}>
            <div className={styles.Kentta}>
                <label>{label}</label>
                {children}
            </div>
        </div>
    );
};

const PostinumeroKentta = ({ children, toimipaikkaName: name, control, label }) => {
    const toimipaikka = useWatch({ control, name });
    return (
        <div className={styles.Rivi}>
            <div className={styles.KenttaLyhyt}>
                <label>{label}</label>
                {children}
            </div>
            <span className={styles.ToimipaikkaText}>{toimipaikka}</span>
        </div>
    );
};
const OtsikkoRivi = ({ label }) => (
    <div className={styles.EnsimmainenRivi}>
        <h3>{label}</h3>
    </div>
);

export const YhteystietoKortti = ({
    kieli: kortinKieli,
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
    const errorVisible = !!Object.keys(validationErrors).length;
    if (kortinKieli === 'en')
        return (
            <div key={kortinKieli} className={styles.KorttiKehys}>
                <OtsikkoRivi label={i18n.translate(`YHTEYSTIEDOTKORTTI_OTSIKKO_${kortinKieli}`)} />
                <Kentta label={i18n.translate('YHTEYSTIEDOT_POSTIOSOITE')}>
                    <Textarea {...yhteystiedotRegister(`${kortinKieli}.postiOsoite` as const)} error={errorVisible} />
                </Kentta>
                <Kentta label={i18n.translate('YHTEYSTIEDOT_PUHELINNUMERO')}>
                    <Input {...yhteystiedotRegister(`${kortinKieli}.puhelinnumero` as const)} />
                </Kentta>
                <Kentta label={i18n.translate('YHTEYSTIEDOT_SAHKOPOSTIOSOITE')}>
                    <Input {...yhteystiedotRegister(`${kortinKieli}.email` as const)} error={errorVisible} />
                </Kentta>
                <Kentta label={i18n.translate('YHTEYSTIEDOT_WWW_OSOITE')}>
                    <Input {...yhteystiedotRegister(`${kortinKieli}.www` as const)} />
                </Kentta>
            </div>
        );
    return (
        <div key={kortinKieli} className={styles.KorttiKehys}>
            <OtsikkoRivi label={i18n.translate(`YHTEYSTIEDOTKORTTI_OTSIKKO_${kortinKieli}`)} />
            <Kentta label={i18n.translate('YHTEYSTIEDOT_POSTIOSOITE')}>
                <Input {...yhteystiedotRegister(`${kortinKieli}.postiOsoite` as const)} />
            </Kentta>
            <PostinumeroKentta
                label={i18n.translate('YHTEYSTIEDOT_POSTINUMERO')}
                toimipaikkaName={`${kortinKieli}.postiOsoiteToimipaikka` as OsoitteentoimipaikkaProps['name']}
                control={formControl}
            >
                <Input
                    {...registerToimipaikkaUpdate(
                        `${kortinKieli}.postiOsoiteToimipaikka`,
                        yhteystiedotRegister(`${kortinKieli}.postiOsoitePostiNro` as const)
                    )}
                    error={errorVisible}
                />
            </PostinumeroKentta>
            {osoitteetOnEri && [
                <Kentta label={i18n.translate('YHTEYSTIEDOT_KAYNTIOSOITE')}>
                    <Input {...yhteystiedotRegister(`${kortinKieli}.kayntiOsoite` as const)} />
                </Kentta>,
                <PostinumeroKentta
                    label={i18n.translate('YHTEYSTIEDOT_POSTINUMERO')}
                    toimipaikkaName={`${kortinKieli}.kayntiOsoiteToimipaikka` as OsoitteentoimipaikkaProps['name']}
                    control={formControl}
                >
                    <Input
                        {...registerToimipaikkaUpdate(
                            `${kortinKieli}.kayntiOsoiteToimipaikka`,
                            yhteystiedotRegister(`${kortinKieli}.kayntiOsoitePostiNro` as const)
                        )}
                        error={errorVisible}
                    />
                </PostinumeroKentta>,
            ]}
            <Kentta label={i18n.translate('YHTEYSTIEDOT_PUHELINNUMERO')}>
                <Input
                    {...yhteystiedotRegister(`${kortinKieli}.puhelinnumero` as const)}
                    name={`${kortinKieli}.puhelinnumero`}
                />
            </Kentta>
            <Kentta label={i18n.translate('YHTEYSTIEDOT_SAHKOPOSTIOSOITE')}>
                <Input {...yhteystiedotRegister(`${kortinKieli}.email` as const)} error={errorVisible} />
            </Kentta>
            <Kentta label={i18n.translate('YHTEYSTIEDOT_WWW_OSOITE')}>
                <Input {...yhteystiedotRegister(`${kortinKieli}.www` as const)} />
            </Kentta>
        </div>
    );
};
