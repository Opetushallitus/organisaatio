import React, { useEffect } from 'react';
import { useNavigate } from 'react-router';

import { useJotpaRekisterointiSelector } from './store';
import { Header } from '../Header';
import { findPostitoimipaikka } from '../addressUtils';
import { useKoodistos } from '../KoodistoContext';
import { useLanguageContext } from '../LanguageContext';

import styles from './jotpa.module.css';

export function JotpaYhteenveto() {
    const navigate = useNavigate();
    const { language } = useLanguageContext();
    const { posti } = useKoodistos();
    const {
        organization: { initialOrganization, form: organizationForm },
        user: { form: userForm },
    } = useJotpaRekisterointiSelector((state) => state);

    useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

    const onSubmit = () => {};

    const kayntiosoite = organizationForm?.copyKayntiosoite
        ? organizationForm.postiosoite
        : organizationForm?.kayntiosoite;
    const kayntipostinumero = organizationForm?.copyKayntiosoite
        ? organizationForm.postinumero
        : organizationForm?.kayntipostinumero;
    return (
        <>
            <Header title="Koulutuksen järjestäjien rekisteröityminen Jotpaa varten" />
            <form onSubmit={onSubmit}>
                <main>
                    <div className="content">
                        <h2>Yhteenveto</h2>
                        <ul className={styles.infoList}>
                            <li>
                                Tarkista että tiedot ovat oikein. Jos havaitset antamissasi tiedoissa virheitä, palaa
                                edelliseen vaiheeseen.
                            </li>
                            <li>
                                <b>Lähetä tiedot tarkistuksen jälkeen.</b>
                            </li>
                        </ul>
                        <h3>Organisaation perustiedot</h3>
                        <label className="title">Organisaation nimi</label>
                        <div>{initialOrganization?.ytjNimi.nimi}</div>
                        <label className="title">Y-tunnus</label>
                        <div>{initialOrganization?.ytunnus}</div>
                        <label className="title">Yritysmuoto</label>
                        <div>{organizationForm?.yritysmuoto.label}</div>
                        <label className="title">Organisaatiotyyppi</label>
                        <div>Koulutuksen järjestäjä</div>
                        <label className="title">Kotipaikka</label>
                        <div>{organizationForm?.kotipaikka.label}</div>
                        <label className="title">Toiminnan alkamisaika</label>
                        <div>{organizationForm?.alkamisaika}</div>
                        <h3>Organisaation yhteystiedot</h3>
                        <label className="title">Puhelinnumero</label>
                        <div>{organizationForm?.puhelinnumero}</div>
                        <label className="title">Yhteiskäyttöinen sähköpostiosoite</label>
                        <div>{organizationForm?.email}</div>
                        <label className="title">Postiosoite</label>
                        <div>{organizationForm?.postiosoite}</div>
                        <label className="title">Postinumero</label>
                        <div>{organizationForm?.postinumero}</div>
                        <label className="title">Postitoimipaikka</label>
                        <div>
                            {organizationForm?.postinumero &&
                                findPostitoimipaikka(organizationForm?.postinumero, posti, language)}
                        </div>
                        <label className="title">Käyntiosoite</label>
                        <div>{kayntiosoite}</div>
                        <label className="title">Käyntiosoitteen postinumero</label>
                        <div>{kayntipostinumero}</div>
                        <label className="title">Käyntiosoitteen postitoimipaikka</label>
                        <div>{kayntipostinumero && findPostitoimipaikka(kayntipostinumero, posti, language)}</div>
                        <h3>Sähköpostiosoite</h3>
                        <label className="title">Sähköpostiosoite</label>
                        <div>
                            {organizationForm?.emails.map((e) => (
                                <div key={e.email}>{e.email}</div>
                            ))}
                        </div>
                        <h3>Jotpa-pääkäyttäjän tiedot</h3>
                        <label className="title">Etunimi</label>
                        <div>{userForm?.etunimi}</div>
                        <label className="title">Sukunimi</label>
                        <div>{userForm?.sukunimi}</div>
                        <label className="title">Jotpa-pääkäyttäjän sähköpostisoite (ei yhteiskäyttöinen)</label>
                        <div>{userForm?.email}</div>
                        <label className="title">Asiointikieli</label>
                        <div>{userForm?.asiointikieli === 'fi' ? 'Suomi' : 'Ruotsi'}</div>
                        <label className="title">Saateteksti</label>
                        <div>{userForm?.info}</div>
                        <div className={styles.buttons}>
                            <button
                                role="link"
                                className={styles.cancelButton}
                                onClick={() => (window.location.href = '/hakija/logout?redirect=/jotpa')}
                            >
                                Keskeytä
                            </button>
                            <button
                                role="link"
                                className={styles.previousButton}
                                onClick={() => navigate('/hakija/jotpa/paakayttaja', { replace: true })}
                            >
                                Edellinen vaihe
                            </button>
                            <input type="submit" value="Lähetä hyväksyttäväksi ja kirjaudu ulos" />
                        </div>
                    </div>
                </main>
            </form>
        </>
    );
}
