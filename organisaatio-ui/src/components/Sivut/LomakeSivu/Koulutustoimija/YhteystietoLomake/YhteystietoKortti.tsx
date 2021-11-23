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
import { Kentta, KenttaLyhyt, Rivi } from '../../LomakeFields/LomakeFields';

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
    isFirst: boolean;
    kieli: 'fi' | 'sv' | 'en';
    yhteystiedotRegister: UseFormRegister<Yhteystiedot>;
    setYhteystiedotValue: UseFormSetValue<Yhteystiedot>;
    validationErrors: FieldErrors<Yhteystiedot>;
    formControl: Control<Yhteystiedot>;
    osoitteetOnEri: boolean;
};

const RiviKentta = ({ label, children }) => {
    return (
        <Rivi>
            <Kentta label={label}>{children}</Kentta>
        </Rivi>
    );
};

const PostinumeroKentta = ({ children, toimipaikkaName: name, control, label }) => {
    const toimipaikka = useWatch({ control, name });
    return (
        <Rivi>
            <KenttaLyhyt label={label}>{children}</KenttaLyhyt>
            <span className={styles.ToimipaikkaText}>{toimipaikka}</span>
        </Rivi>
    );
};
const OtsikkoRivi = ({ label }) => (
    <div className={styles.EnsimmainenRivi}>
        <h3>{label}</h3>
    </div>
);

export const YhteystietoKortti = ({
    isFirst,
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
                <RiviKentta label={i18n.translate('YHTEYSTIEDOT_POSTIOSOITE')}>
                    <Textarea {...yhteystiedotRegister(`${kortinKieli}.postiOsoite` as const)} error={errorVisible} />
                </RiviKentta>
                <RiviKentta label={i18n.translate('YHTEYSTIEDOT_PUHELINNUMERO')}>
                    <Input {...yhteystiedotRegister(`${kortinKieli}.puhelinnumero` as const)} />
                </RiviKentta>
                <RiviKentta label={i18n.translate('YHTEYSTIEDOT_SAHKOPOSTIOSOITE')}>
                    <Input {...yhteystiedotRegister(`${kortinKieli}.email` as const)} error={errorVisible} />
                </RiviKentta>
                <RiviKentta label={i18n.translate('YHTEYSTIEDOT_WWW_OSOITE')}>
                    <Input {...yhteystiedotRegister(`${kortinKieli}.www` as const)} />
                </RiviKentta>
            </div>
        );
    return (
        <div key={kortinKieli} className={styles.KorttiKehys}>
            <OtsikkoRivi label={i18n.translate(`YHTEYSTIEDOTKORTTI_OTSIKKO_${kortinKieli}`)} />
            <RiviKentta label={i18n.translate('YHTEYSTIEDOT_POSTIOSOITE')}>
                <Input {...yhteystiedotRegister(`${kortinKieli}.postiOsoite` as const)} />
            </RiviKentta>
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
                <RiviKentta label={i18n.translate('YHTEYSTIEDOT_KAYNTIOSOITE')}>
                    <Input {...yhteystiedotRegister(`${kortinKieli}.kayntiOsoite` as const)} />
                </RiviKentta>,
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
            <RiviKentta label={i18n.translate('YHTEYSTIEDOT_PUHELINNUMERO')}>
                <Input
                    {...yhteystiedotRegister(`${kortinKieli}.puhelinnumero` as const)}
                    name={`${kortinKieli}.puhelinnumero`}
                />
            </RiviKentta>
            <RiviKentta label={i18n.translate('YHTEYSTIEDOT_SAHKOPOSTIOSOITE')}>
                <Input {...yhteystiedotRegister(`${kortinKieli}.email` as const)} error={errorVisible} />
            </RiviKentta>
            <RiviKentta label={i18n.translate('YHTEYSTIEDOT_WWW_OSOITE')}>
                <Input {...yhteystiedotRegister(`${kortinKieli}.www` as const)} />
            </RiviKentta>
        </div>
    );
};
