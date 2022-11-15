import React, { useEffect, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router';
import { yupResolver } from '@hookform/resolvers/yup';
import Markdown from 'react-markdown';

import { Header } from './JotpaHeader';
import { useJotpaRekisterointiDispatch, useJotpaRekisterointiSelector } from './store';
import { setForm, UserFormState, UserSchema } from '../userSlice';
import { Input } from '../Input';
import { FormError } from '../FormError';
import { RegistrationProgressBar } from '../RegistrationProgressBar';
import { useLanguageContext } from '../LanguageContext';

import styles from './jotpa.module.css';

export function JotpaPaakayttaja() {
    const { i18n } = useLanguageContext();
    const navigate = useNavigate();
    const { form } = useJotpaRekisterointiSelector((state) => state.user);
    const dispatch = useJotpaRekisterointiDispatch();
    const {
        formState: { errors },
        handleSubmit,
        register,
    } = useForm<UserFormState>({
        mode: 'onTouched',
        defaultValues: useMemo(() => {
            return form;
        }, [form]),
        resolver: yupResolver(UserSchema),
    });

    useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

    const onSubmit = (data: UserFormState) => {
        dispatch(setForm(data));
        navigate('/hakija/jotpa/yhteenveto');
    };

    return (
        <>
            <Header title={i18n.translate('otsikko_rekisterointi')} />
            <form onSubmit={handleSubmit(onSubmit)} data-test-id="paakayttaja-form">
                <main id="main">
                    <div className="content">
                        <RegistrationProgressBar
                            currentPhase={2}
                            phaseTranslationKeys={['organisaatio_otsikko', 'paakayttaja_otsikko', 'yhteenveto_otsikko']}
                        />
                        <h2>{i18n.translate('paakayttaja_otsikko')}</h2>
                        <div className={styles.info}>
                            <Markdown>{i18n.translate('paakayttaja_info')}</Markdown>
                        </div>
                        <label className="title" htmlFor="etunimi">
                            {i18n.translate('paakayttaja_etunimi')} *
                        </label>
                        <Input name="etunimi" register={register} error={errors.etunimi} />
                        <label className="title" htmlFor="sukunimi">
                            {i18n.translate('paakayttaja_sukunimi')} *
                        </label>
                        <Input name="sukunimi" register={register} error={errors.sukunimi} />
                        <label className="title" htmlFor="paakayttajaEmail">
                            {i18n.translate('paakayttaja_email')} *
                        </label>
                        <Input name="paakayttajaEmail" register={register} error={errors.paakayttajaEmail} />
                        <label className="title">{i18n.translate('paakayttaja_asiointikieli')} *</label>
                        <div
                            role="radiogroup"
                            aria-invalid={!!errors.asiointikieli}
                            aria-errormessage="#error-asiointikieli"
                            aria-live="polite"
                            aria-required="true"
                            className={styles.radioButtons}
                        >
                            <label htmlFor="fi">
                                <input id="fi" type="radio" {...register('asiointikieli')} value="fi" /> Suomi
                            </label>
                            <br />
                            <label htmlFor="sv">
                                <input id="sv" type="radio" {...register('asiointikieli')} value="sv" /> Ruotsi
                            </label>
                            <FormError
                                id="error-asiointikieli"
                                error={errors.asiointikieli?.message}
                                inputId="asiointikieli"
                            />
                        </div>
                        <label className="title" htmlFor="info">
                            {i18n.translate('paakayttaja_saateteksti')}
                        </label>
                        <textarea id="info" aria-required={false} {...register('info')} />
                        <div className={styles.buttons}>
                            <button
                                role="link"
                                className={styles.cancelButton}
                                onClick={() => (window.location.href = '/hakija/logout?redirect=/jotpa')}
                            >
                                {i18n.translate('organisaatio_nappi_keskeyta')}
                            </button>
                            <button
                                role="link"
                                className={styles.previousButton}
                                onClick={() => navigate('/hakija/jotpa/organisaatio', { replace: true })}
                            >
                                {i18n.translate('nappi_edellinen_vaihe')}
                            </button>
                            <input type="submit" value={i18n.translate('organisaatio_nappi_seuraava_vaihe')} />
                        </div>
                    </div>
                </main>
            </form>
        </>
    );
}
