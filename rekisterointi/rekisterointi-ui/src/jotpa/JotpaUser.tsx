import React from 'react';
import { useForm } from 'react-hook-form';
import * as yup from 'yup';
import { yupResolver } from '@hookform/resolvers/yup';

import { Header } from '../Header';

import styles from './jotpa.module.css';
import { useJotpaRekisterointiSelector } from './store';
import { useNavigate } from 'react-router';

type PaakayttajaForm = {
    etunimi: string;
    sukunimi: string;
    email: string;
    asiointikieli: string;
    saateteksti: string;
};

const PaakayttajaSchema = yup.object().shape({});

export function JotpaUser() {
    const navigate = useNavigate();
    const state = useJotpaRekisterointiSelector((state) => state);
    const {
        control,
        formState: { errors },
        handleSubmit,
    } = useForm<PaakayttajaForm>({
        resolver: yupResolver(PaakayttajaSchema),
    });

    const onSubmit = () => {};

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <Header title="Koulutuksen järjestäjien rekisteröityminen Jotpaa varten" />
            <main>
                <div className="content">
                    <h2>Jotpa-pääkäyttäjän tiedot</h2>
                    <ul className={styles.infoList}>
                        <li>
                            Jotpa-pääkäyttäjä vastaa organisaation Jotpa-käyttöoikeuksista. Lomakkeella ilmoitettu
                            henkilö saa kutsun Jotpa-pääkäyttäjäksi.
                        </li>
                        <li>Jotpa-pääkäyttäjän yhteystiedot tallennetaan Opetushallituksen Käyttöoikeuspalveluun.</li>
                        <li>
                            Jotpa-pääkäyttäjän henkilötiedot tallennetaan Opetushallituksen Oppijanumerorekisteriin
                            hänen rekisteröityessään palvelun käyttäjäksi.
                        </li>
                    </ul>
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
                {JSON.stringify(state)}
            </main>
        </form>
    );
}
