import React, { useEffect, useMemo } from 'react';
import { useFieldArray, useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { yupResolver } from '@hookform/resolvers/yup';
import Markdown from 'react-markdown';

import { Header } from './JotpaHeader';
import { useKoodistos } from '../KoodistoContext';
import { useJotpaRekisterointiDispatch, useJotpaRekisterointiSelector } from './store';
import { Select } from '../Select';
import { DatePicker } from '../DatePicker';
import { OrganisationFormState, OrganisationSchema, setForm } from '../organisationSlice';
import { Input } from '../Input';
import { FormError } from '../FormError';
import { useLanguageContext } from '../LanguageContext';
import { findPostitoimipaikka } from '../addressUtils';
import { RegistrationProgressBar } from '../RegistrationProgressBar';

import styles from './jotpa.module.css';

const AddEmailLogo = () => (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path fillRule="evenodd" clipRule="evenodd" d="M19 13H13V19H11V13H5V11H11V5H13V11H19V13Z" fill="#3A7A10" />
    </svg>
);

const RemoveEmailLogo = () => (
    <svg width="14" height="18" viewBox="0 0 14 18" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path
            fillRule="evenodd"
            clipRule="evenodd"
            d="M1 16C1 17.1 1.9 18 3 18H11C12.1 18 13 17.1 13 16V4H1V16ZM3 6H11V16H3V6ZM10.5 1L9.5 0H4.5L3.5 1H0V3H14V1H10.5Z"
            fill="#4C4C4C"
        />
    </svg>
);

export function JotpaOrganisaatio() {
    const { language, i18n } = useLanguageContext();
    const navigate = useNavigate();
    const { yritysmuodot, organisaatiotyypit, kunnat, posti, postinumerot } = useKoodistos();
    const { initialOrganisation, form } = useJotpaRekisterointiSelector((state) => state.organisation);
    const dispatch = useJotpaRekisterointiDispatch();
    const {
        control,
        formState: { errors },
        handleSubmit,
        register,
        watch,
    } = useForm<OrganisationFormState>({
        defaultValues: useMemo(() => {
            return form;
        }, [form]),
        resolver: yupResolver(OrganisationSchema(yritysmuodot, kunnat, postinumerot)),
    });
    const {
        fields: emailFields,
        append: appendEmail,
        remove: removeEmail,
    } = useFieldArray<OrganisationFormState>({
        control,
        name: 'emails',
    });

    useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

    if (emailFields.length === 0) {
        appendEmail({ email: '' }, { shouldFocus: false });
    }

    const onSubmit = (data: OrganisationFormState) => {
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
        const formState: OrganisationFormState = {
            yritysmuoto: data.yritysmuoto,
            kotipaikka: data.kotipaikka,
            alkamisaika: data.alkamisaika,
            puhelinnumero: data.puhelinnumero,
            email: data.email,
            emails: data.emails.filter((e) => !!e.email) as { email: string }[],
            postiosoite: data.postiosoite,
            postinumero: data.postinumero,
            copyKayntiosoite: data.copyKayntiosoite,
            ...kayntiosoite,
        };

        dispatch(setForm(formState));
        navigate('/hakija/jotpa/paakayttaja');
    };

    const preventDefault = (fn: () => void) => (e: React.MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();
        e.stopPropagation();
        fn();
    };

    const postinumero = watch('postinumero');
    const copyKayntiosoite = watch('copyKayntiosoite');
    const kayntipostinumero = watch('kayntipostinumero');
    const postitoimipaikka = findPostitoimipaikka(postinumero, posti, language);
    const kayntipostitoimipaikka = kayntipostinumero && findPostitoimipaikka(kayntipostinumero, posti, language);
    return (
        <>
            <Header title={i18n.translate('otsikko_rekisterointi')} />
            <form onSubmit={handleSubmit(onSubmit)}>
                <main>
                    <div className="content">
                        <RegistrationProgressBar
                            currentPhase={1}
                            phaseTranslationKeys={['organisaatio_otsikko', 'paakayttaja_otsikko', 'yhteenveto_otsikko']}
                        />
                        <h2>{i18n.translate('organisaatio_otsikko')}</h2>
                        <div className={styles.info}>
                            <Markdown>{i18n.translate('organisaatio_perustiedot_info')}</Markdown>
                        </div>
                        <div className="label">{i18n.translate('organisaatio_perustiedot_nimi')}</div>
                        <div data-test-id="yrityksen-nimi">{initialOrganisation?.ytjNimi.nimi ?? '...'}</div>
                        <div className="label">{i18n.translate('organisaatio_perustiedot_ytunnus')}</div>
                        <div data-test-id="ytunnus">{initialOrganisation?.ytunnus ?? '...'}</div>
                        <label className="title" htmlFor="yritysmuoto">
                            {i18n.translate('organisaatio_perustiedot_yritysmuoto')} *
                        </label>
                        <Select<OrganisationFormState>
                            name="yritysmuoto"
                            ariaLabel={i18n.translate('organisaatio_perustiedot_yritysmuoto')}
                            control={control}
                            error={errors.yritysmuoto?.value}
                            options={yritysmuodot.map((k) => ({ value: k.uri, label: k.nimi[language] || k.uri }))}
                        />
                        <div className="label">{i18n.translate('organisaatio_perustiedot_organisaatiotyyppi')}</div>
                        <div data-test-id="organisaatiotyyppi">
                            {organisaatiotyypit.find((o) => o.uri === 'organisaatiotyyppi_01')?.nimi[language] ?? '...'}
                        </div>
                        <label className="title" htmlFor="kotipaikka">
                            {i18n.translate('organisaatio_perustiedot_kotipaikka')} *
                        </label>
                        <Select<OrganisationFormState>
                            name="kotipaikka"
                            ariaLabel={i18n.translate('organisaatio_perustiedot_kotipaikka')}
                            control={control}
                            error={errors.kotipaikka?.value}
                            options={kunnat.map((k) => ({ value: k.uri, label: k.nimi[language] || k.uri }))}
                        />
                        <label className="title" htmlFor="alkamisaika">
                            {i18n.translate('organisaatio_perustiedot_alkamisaika')} *
                        </label>
                        <DatePicker<OrganisationFormState>
                            name="alkamisaika"
                            control={control}
                            error={errors.alkamisaika}
                        />
                        <h2>{i18n.translate('organisaatio_yhteystiedot')}</h2>
                        <div className={styles.info}>
                            <Markdown>{i18n.translate('organisaatio_yhteystiedot_info')}</Markdown>
                        </div>
                        <label className="title" htmlFor="puhelinnumero">
                            {i18n.translate('organisaatio_yhteystiedot_puhelinnumero')} *
                        </label>
                        <Input name="puhelinnumero" register={register} error={errors.puhelinnumero} />
                        <label className="title" htmlFor="email">
                            {i18n.translate('organisaatio_yhteystiedot_email')} *
                        </label>
                        <Input name="email" register={register} error={errors.email} />
                        <label className="title" htmlFor="postiosoite">
                            {i18n.translate('organisaatio_yhteystiedot_postiosoite')} *
                        </label>
                        <Input name="postiosoite" register={register} error={errors.postiosoite} />
                        <label className="title" htmlFor="postinumero">
                            {i18n.translate('organisaatio_yhteystiedot_postinumero')} *
                        </label>
                        <Input name="postinumero" register={register} error={errors.postinumero} />
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_postitoimipaikka')}</label>
                        <div className={styles.postitoimipaikka} data-test-id="postitoimipaikka">
                            {postitoimipaikka}
                        </div>
                        <div className={styles.copyKayntiosoite}>
                            <label htmlFor="copyKayntiosoite">
                                <input id="copyKayntiosoite" type="checkbox" {...register('copyKayntiosoite')} />{' '}
                                {i18n.translate('organisaatio_yhteystiedot_kopioi_osoite')}
                            </label>
                        </div>
                        {!copyKayntiosoite && (
                            <>
                                <label className="title" htmlFor="kayntiosoite">
                                    {i18n.translate('organisaatio_yhteystiedot_kayntiosoite')} *
                                </label>
                                <Input
                                    name="kayntiosoite"
                                    required={false}
                                    register={register}
                                    error={errors.kayntiosoite}
                                />
                                <label className="title" htmlFor="kayntipostinumero">
                                    {i18n.translate('organisaatio_yhteystiedot_kayntipostinumero')} *
                                </label>
                                <Input
                                    name="kayntipostinumero"
                                    required={false}
                                    register={register}
                                    error={errors.kayntipostinumero}
                                />
                                <label className="title">
                                    {i18n.translate('organisaatio_yhteystiedot_kayntipostitoimipaikka')}
                                </label>
                                <div className={styles.postitoimipaikka} data-test-id="kayntipostitoimipaikka">
                                    {kayntipostitoimipaikka}
                                </div>
                            </>
                        )}
                        <h2>{i18n.translate('organisaatio_email')}</h2>
                        <div className={styles.info}>
                            <Markdown>{i18n.translate('organisaatio_email_info')}</Markdown>
                        </div>
                        <label id="email-label" className="title" htmlFor="firstEmail">
                            {i18n.translate('organisaatio_email')} *
                        </label>
                        <FormError error={errors?.emails?.message} inputId="emails" />
                        {emailFields.map((field, index) => {
                            const error = errors.emails?.[index]?.email;
                            return (
                                <div key={field.id}>
                                    <div className={styles.emailRow}>
                                        <input
                                            aria-labelledby="email-label"
                                            id={index === 0 ? 'firstEmail' : undefined}
                                            className={`${styles.emailInput} ${error ? styles.errorInput : ''}`}
                                            type="text"
                                            {...register(`emails.${index}.email`)}
                                        />
                                        {index === 0 ? (
                                            <div className={styles.removeEmailPlaceholder} />
                                        ) : (
                                            <button
                                                id={`remove-email-${index}`}
                                                className={styles.removeEmailButton}
                                                onClick={preventDefault(() => removeEmail(index))}
                                                aria-label={i18n.translate('organisaatio_email_remove')}
                                            >
                                                <RemoveEmailLogo />
                                            </button>
                                        )}
                                    </div>
                                    <FormError error={error?.message} inputId={`email-${index}`} />
                                </div>
                            );
                        })}
                        <button
                            className={styles.addEmailButton}
                            onClick={preventDefault(() => appendEmail({ email: '' }))}
                            data-test-id="add-email"
                        >
                            <AddEmailLogo />
                            {i18n.translate('organisaatio_email_add')}
                        </button>
                        <div className={styles.buttons}>
                            <button
                                role="link"
                                className={styles.cancelButton}
                                onClick={() => (window.location.href = '/hakija/logout?redirect=/jotpa')}
                            >
                                {i18n.translate('organisaatio_nappi_keskeyta')}
                            </button>
                            <input type="submit" value={i18n.translate('organisaatio_nappi_seuraava_vaihe')} />
                        </div>
                    </div>
                </main>
            </form>
        </>
    );
}
