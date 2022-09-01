import React, { useMemo } from 'react';
import { useFieldArray, useForm } from 'react-hook-form';
import * as yup from 'yup';
import { yupResolver } from '@hookform/resolvers/yup';

import { Header } from '../Header';
import { useKoodistos } from '../KoodistoContext';
import { useJotpaRekisterointiDispatch, useJotpaRekisterointiSelector } from './store';
import { Select } from '../Select';
import { Koodi, SelectOption } from '../types';
import { DatePicker } from '../DatePicker';
import { FormState, setForm } from '../organizationSlice';
import {
    EmailArraySchema,
    EmailSchema,
    KoodiSchema,
    PostinumeroSchema,
    PostiosoiteSchema,
    PuhelinnumeroSchema,
} from '../yupSchemas';
import { Input } from '../Input';

import styles from './JotpaOrganization.module.css';
import { FormError } from '../FormError';

type OrganizationForm = {
    yritysmuoto: SelectOption;
    kotipaikka: SelectOption;
    alkamisaika: Date;
    puhelinnumero: string;
    email: string;
    postiosoite: string;
    postinumero: string;
    copyKayntiosoite: boolean;
    kayntiosoite?: string;
    kayntipostinumero?: string;
    emails: { email?: string }[];
};

const findPostitoimipaikka = (postinumero: string, postinumerot: Koodi[]) => {
    const postinumeroUri = `posti_${postinumero}`;
    return postinumerot.find((p) => p.uri === postinumeroUri)?.nimi.fi;
};

const OrganizationSchema = (yritysmuodot: Koodi[], kunnat: Koodi[]): yup.SchemaOf<OrganizationForm> =>
    yup.object().shape({
        yritysmuoto: KoodiSchema(yritysmuodot),
        kotipaikka: KoodiSchema(kunnat),
        alkamisaika: yup.date().required(),
        puhelinnumero: PuhelinnumeroSchema,
        email: EmailSchema,
        postiosoite: PostiosoiteSchema.required(),
        postinumero: PostinumeroSchema.required(),
        copyKayntiosoite: yup.bool().required(),
        kayntiosoite: yup
            .string()
            .when(['copyKayntiosoite'], (copyKayntiosoite, schema) =>
                copyKayntiosoite ? schema.optional() : PostiosoiteSchema.required()
            ),
        kayntipostinumero: yup
            .string()
            .when(['copyKayntiosoite'], (copyKayntiosoite, schema) =>
                copyKayntiosoite ? schema.optional() : PostinumeroSchema.required()
            ),
        emails: EmailArraySchema,
    });

const AddEmailLogo = () => (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path fill-rule="evenodd" clip-rule="evenodd" d="M19 13H13V19H11V13H5V11H11V5H13V11H19V13Z" fill="#3A7A10" />
    </svg>
);

export function JotpaOrganization() {
    const { yritysmuodot, kunnat, postinumerot } = useKoodistos();
    const { loading, initialOrganization, form } = useJotpaRekisterointiSelector((state) => state.organization);
    const dispatch = useJotpaRekisterointiDispatch();
    const {
        control,
        formState: { errors },
        handleSubmit,
        register,
        watch,
        getValues,
        setError,
    } = useForm<OrganizationForm>({
        defaultValues: useMemo(() => {
            return form;
        }, [form]),
        resolver: yupResolver(OrganizationSchema(yritysmuodot, kunnat)),
    });
    const { fields: emailFields, append: appendEmail } = useFieldArray<OrganizationForm>({
        control,
        name: 'emails',
    });
    if (emailFields.length === 0) {
        appendEmail({ email: '' });
    }

    if (loading || !initialOrganization || !yritysmuodot) {
        return null;
    }
    const onSubmit = (data: OrganizationForm) => {
        const postitoimipaikka = findPostitoimipaikka(data.postinumero, postinumerot);
        const kayntipostitoimipaikka = data.copyKayntiosoite
            ? postitoimipaikka
            : findPostitoimipaikka(data.kayntipostinumero!, postinumerot);
        const isInvalidKayntipostitoimipaikka = !data.copyKayntiosoite && !kayntipostitoimipaikka;
        if (!postitoimipaikka || isInvalidKayntipostitoimipaikka) {
            if (!postitoimipaikka) {
                setError('postinumero', { message: 'Postinumerolle ei löydy postitoimipaikkaa' });
            }
            if (isInvalidKayntipostitoimipaikka) {
                setError('kayntipostinumero', {
                    message: 'Käynti osoitteen postinumerolle ei löydy postitoimipaikkaa',
                });
            }
            return;
        }

        const kayntiosoite = !data.copyKayntiosoite
            ? {
                  kayntiosoite: data.kayntiosoite!,
                  kayntipostinumero: data.kayntipostinumero!,
                  kayntipostitoimipaikka: kayntipostitoimipaikka!,
              }
            : {
                  kayntiosoite: data.postiosoite,
                  kayntipostinumero: data.postinumero,
                  kayntipostitoimipaikka: postitoimipaikka,
              };
        const formState: FormState = {
            yritysmuoto: data.yritysmuoto,
            kotipaikka: data.kotipaikka,
            alkamisaika: data.alkamisaika,
            puhelinnumero: data.puhelinnumero,
            email: data.email,
            emails: data.emails.filter((e) => !!e.email) as { email: string }[],
            postiosoite: data.postiosoite,
            postinumero: data.postinumero,
            postitoimipaikka,
            ...kayntiosoite,
        };

        dispatch(setForm(formState));
    };

    const addEmail = (e: React.MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();
        e.stopPropagation();
        appendEmail({ email: '' });
    };

    const postinumero = watch('postinumero');
    const copyKayntiosoite = watch('copyKayntiosoite');
    const kayntipostinumero = watch('kayntipostinumero');
    const postitoimipaikka = findPostitoimipaikka(postinumero, postinumerot);
    const kayntipostitoimipaikka = kayntipostinumero && findPostitoimipaikka(kayntipostinumero, postinumerot);
    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <Header title="Koulutuksen järjestäjien rekisteröityminen Jotpaa varten" />
            <main>
                <div className="content">
                    <h2>Organisaation perustiedot</h2>
                    <ul className={styles.infoList}>
                        <li>Tarkista, että tiedot ovat oikein ja täytä puuttuvat kohdat ennen jatkamista.</li>
                        <li>Anna organisaatiolle kuvaava nimi, jos kenttä on tyhjä.</li>
                        <li>
                            Esitäytetyt tiedot tulevat Yritys- ja yhteisötietojärjestelmästä ja Opetushallituksen
                            Organisaatiopalvelusta. Jos esitäytetyissä tiedoissa on virheitä, tiedot tulee päivittää
                            Yritys- ja yhteisötietojärjestelmään tai Organisaatiopalveluun.
                        </li>
                        <li>
                            Palveluntuottajan tiedot tallennetaan Opetushallituksen Organisaatiopalveluun, kun kunta on
                            hyväksynyt rekisteröitymisen. Huomaa, että kaikki kentät ovat pakollisia.
                        </li>
                    </ul>
                    <label>Organisaation nimi</label>
                    <div>{initialOrganization.ytjNimi.nimi}</div>
                    <label>Y-tunnus</label>
                    <div>{initialOrganization.ytunnus}</div>
                    <label htmlFor="yritysmuoto">Yritysmuoto *</label>
                    <Select<OrganizationForm>
                        name="yritysmuoto"
                        control={control}
                        error={errors.yritysmuoto?.value}
                        options={yritysmuodot.map((k) => ({ value: k.uri, label: k.nimi.fi || k.uri }))}
                    />
                    <label>Organisaatiotyyppi</label>
                    <div>Koulutuksen järjestäjä</div>
                    <label htmlFor="kotipaikka">Kotipaikka *</label>
                    <Select<OrganizationForm>
                        name="kotipaikka"
                        control={control}
                        error={errors.kotipaikka?.value}
                        options={kunnat.map((k) => ({ value: k.uri, label: k.nimi.fi || k.uri }))}
                    />
                    <label htmlFor="alkamisaika">Toiminnan alkamisaika *</label>
                    <DatePicker<OrganizationForm> name="alkamisaika" control={control} error={errors.alkamisaika} />
                    <h2>Organisaation yhteystiedot</h2>
                    <div className={styles.info}>
                        Tarkista, että tiedot ovat oikein ja täytä puuttuvat kohdat ennen jatkamista. Palveluntuottajan
                        yhteystiedot tallennetaan Opetushallituksen Organisaatiopalveluun, kun kunta on hyväksynyt
                        rekisteröitymisen.
                    </div>
                    <label htmlFor="puhelinnumero">Puhelinnumero *</label>
                    <Input name="puhelinnumero" register={register} error={errors.puhelinnumero} />
                    <label htmlFor="email">Yhteiskäyttöinen sähköpostiosoite *</label>
                    <Input name="email" register={register} error={errors.email} />
                    <label htmlFor="postiosoite">Postiosoite *</label>
                    <Input name="postiosoite" register={register} error={errors.postiosoite} />
                    <label htmlFor="postinumero">Postinumero *</label>
                    <Input name="postinumero" register={register} error={errors.postinumero} />
                    <label>Postitoimipaikka</label>
                    <div className={styles.postitoimipaikka}>{postitoimipaikka}</div>
                    <label>Käyntiosoite *</label>
                    <div>
                        <input type="checkbox" {...register('copyKayntiosoite')} /> Sama kuin postiosoite
                    </div>
                    {!copyKayntiosoite && (
                        <>
                            <Input
                                name="kayntiosoite"
                                required={false}
                                register={register}
                                error={errors.kayntiosoite}
                            />
                            <label>Käyntiosoitteen postinumero *</label>
                            <Input
                                name="kayntipostinumero"
                                required={false}
                                register={register}
                                error={errors.kayntipostinumero}
                            />
                            <label>Käyntiosoitteen postitoimipaikka</label>
                            <div className={styles.postitoimipaikka}>{kayntipostitoimipaikka}</div>
                        </>
                    )}
                    <h2>Sähköpostiosoite</h2>
                    <ul className={styles.infoList}>
                        <li>
                            Syötä yhden tai useamman henkilön sähköpostiosoite, jota käytetään palveluntuottajan
                            rekisteröitymiseen liittyvässä viestinnässä.
                        </li>
                        <li>
                            Sähköposteja lähetetään rekisteröitymisen vastaanottamisesta sekä rekisteröitymisen
                            hyväksymisestä tai hylkäämisestä.
                        </li>
                        <li>Sähköpostiosoitetta ei tallenneta Organisaatiopalveluun.</li>
                    </ul>
                    <label>Sähköpostiosoite *</label>
                    {emailFields.map((field, index) => {
                        const error = errors.emails?.[index]?.email;
                        return (
                            <div>
                                <input
                                    className={`${styles.emailInput} ${error ? styles.errorInput : ''}`}
                                    type="text"
                                    key={field.id}
                                    {...register(`emails.${index}.email`)}
                                />
                                <FormError error={error?.message} />
                            </div>
                        );
                    })}
                    <button className={styles.addEmailButton} onClick={addEmail}>
                        <AddEmailLogo />
                        Lisää sähköpostiosoite
                    </button>
                    <div className={styles.buttons}>
                        <button
                            role="link"
                            className={styles.cancelButton}
                            onClick={() => (window.location.href = '/hakija/logout?redirect=/jotpa')}
                        >
                            Keskeytä
                        </button>
                        <input type="submit" title="Seuraava vaihe" />
                    </div>
                </div>
                {JSON.stringify(getValues())}
                <br />
                {JSON.stringify(initialOrganization)}
            </main>
        </form>
    );
}
