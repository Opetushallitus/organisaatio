import React, { useEffect, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router';
import { yupResolver } from '@hookform/resolvers/yup';

import { Header } from '../Header';
import { useJotpaRekisterointiDispatch, useJotpaRekisterointiSelector } from './store';
import { setForm, UserFormState, UserSchema } from '../userSlice';
import { Input } from '../Input';
import { useLanguageContext } from '../LanguageContext';
import { FormError } from '../FormError';

import styles from './jotpa.module.css';

export function JotpaUser() {
    const { language } = useLanguageContext();
    const navigate = useNavigate();
    const { form } = useJotpaRekisterointiSelector((state) => state.user);
    const dispatch = useJotpaRekisterointiDispatch();
    const {
        formState: { errors },
        handleSubmit,
        register,
    } = useForm<UserFormState>({
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
            <Header title="Koulutuksen järjestäjien rekisteröityminen Jotpaa varten" />
            <form onSubmit={handleSubmit(onSubmit)}>
                <main>
                    <div className="content">
                        <h2>Jotpa-pääkäyttäjän tiedot</h2>
                        <ul className={styles.infoList}>
                            <li>
                                Jotpa-pääkäyttäjä vastaa organisaation Jotpa-käyttöoikeuksista. Lomakkeella ilmoitettu
                                henkilö saa kutsun Jotpa-pääkäyttäjäksi.
                            </li>
                            <li>
                                Jotpa-pääkäyttäjän yhteystiedot tallennetaan Opetushallituksen Käyttöoikeuspalveluun.
                            </li>
                            <li>
                                Jotpa-pääkäyttäjän henkilötiedot tallennetaan Opetushallituksen Oppijanumerorekisteriin
                                hänen rekisteröityessään palvelun käyttäjäksi.
                            </li>
                        </ul>
                        <label className="title" htmlFor="etunimi">
                            Etunimi *
                        </label>
                        <Input name="etunimi" register={register} error={errors.etunimi} />
                        <label className="title" htmlFor="sukunimi">
                            Sukunimi *
                        </label>
                        <Input name="sukunimi" register={register} error={errors.sukunimi} />
                        <label className="title" htmlFor="email">
                            Jotpa-pääkäyttäjän sähköpostisoite (ei yhteiskäyttöinen) *
                        </label>
                        <Input name="email" register={register} error={errors.email} />
                        <label className="title">Asiointikieli *</label>
                        <div className={styles.radioButtons}>
                            <label htmlFor="fi">
                                <input
                                    id="fi"
                                    type="radio"
                                    {...register('asiointikieli')}
                                    value="fi"
                                    defaultValue={language}
                                />{' '}
                                Suomi
                            </label>
                            <br />
                            <label htmlFor="sv">
                                <input
                                    id="sv"
                                    type="radio"
                                    {...register('asiointikieli')}
                                    value="sv"
                                    defaultValue={language}
                                />{' '}
                                Ruotsi
                            </label>
                            <FormError error={errors.asiointikieli?.message} />
                        </div>
                        <label className="title" htmlFor="info">
                            Saateteksti pääkäyttäjälle
                        </label>
                        <textarea id="info" {...register('info')} />
                        <div className={styles.buttons}>
                            <button
                                role="link"
                                className={styles.cancelButton}
                                onClick={() => (window.location.href = '/hakija/logout?redirect=/jotpa')}
                            >
                                Keskeytä
                            </button>
                            <button role="link" className={styles.previousButton} onClick={() => navigate(-1)}>
                                Edellinen vaihe
                            </button>
                            <input type="submit" value="Seuraava vaihe" />
                        </div>
                    </div>
                </main>
            </form>
        </>
    );
}
