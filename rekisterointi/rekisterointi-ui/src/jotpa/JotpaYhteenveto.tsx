import React, { useEffect } from 'react';
import { useNavigate } from 'react-router';
import Markdown from 'react-markdown';

import { useJotpaRekisterointiSelector } from './store';
import { Header } from '../Header';
import { findPostitoimipaikka } from '../addressUtils';
import { useKoodistos } from '../KoodistoContext';
import { useLanguageContext } from '../LanguageContext';
import { RegistrationProgressBar } from '../RegistrationProgressBar';

import styles from './jotpa.module.css';

export function JotpaYhteenveto() {
    const navigate = useNavigate();
    const { language, i18n } = useLanguageContext();
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
                        <RegistrationProgressBar
                            currentPhase={3}
                            phaseTranslationKeys={['organisaatio_otsikko', 'paakayttaja_otsikko', 'yhteenveto_otsikko']}
                        />
                        <h2>{i18n.translate('yhteenveto_otsikko')}</h2>
                        <div className={styles.info}>
                            <Markdown>{i18n.translate('yhteenveto_info')}</Markdown>
                        </div>
                        <h3>{i18n.translate('organisaatio_otsikko')}</h3>
                        <label className="title">{i18n.translate('organisaatio_perustiedot_nimi')}</label>
                        <div>{initialOrganization?.ytjNimi.nimi}</div>
                        <label className="title">{i18n.translate('organisaatio_perustiedot_ytunnus')}</label>
                        <div>{initialOrganization?.ytunnus}</div>
                        <label className="title">{i18n.translate('organisaatio_perustiedot_yritysmuoto')}</label>
                        <div>{organizationForm?.yritysmuoto.label}</div>
                        <label className="title">{i18n.translate('organisaatio_perustiedot_organisaatiotyyppi')}</label>
                        <div>Koulutuksen järjestäjä</div>
                        <label className="title">{i18n.translate('organisaatio_perustiedot_kotipaikka')}</label>
                        <div>{organizationForm?.kotipaikka.label}</div>
                        <label className="title">{i18n.translate('organisaatio_perustiedot_alkamisaika')}</label>
                        <div>{organizationForm?.alkamisaika}</div>
                        <h3>{i18n.translate('organisaatio_yhteystiedot')}</h3>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_puhelinnumero')}</label>
                        <div>{organizationForm?.puhelinnumero}</div>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_email')}</label>
                        <div>{organizationForm?.email}</div>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_postiosoite')}</label>
                        <div>{organizationForm?.postiosoite}</div>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_postinumero')}</label>
                        <div>{organizationForm?.postinumero}</div>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_postitoimipaikka')}</label>
                        <div>
                            {organizationForm?.postinumero &&
                                findPostitoimipaikka(organizationForm?.postinumero, posti, language)}
                        </div>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_kayntiosoite')}</label>
                        <div>{kayntiosoite}</div>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_kayntipostinumero')}</label>
                        <div>{kayntipostinumero}</div>
                        <label className="title">
                            {i18n.translate('organisaatio_yhteystiedot_kayntipostitoimipaikka')}
                        </label>
                        <div>{kayntipostinumero && findPostitoimipaikka(kayntipostinumero, posti, language)}</div>
                        <h3>{i18n.translate('organisaatio_email')}</h3>
                        <label className="title">{i18n.translate('organisaatio_email')}</label>
                        <div>
                            {organizationForm?.emails.map((e) => (
                                <div key={e.email}>{e.email}</div>
                            ))}
                        </div>
                        <h3>{i18n.translate('paakayttaja_otsikko')}</h3>
                        <label className="title">{i18n.translate('paakayttaja_etunimi')}</label>
                        <div>{userForm?.etunimi}</div>
                        <label className="title">{i18n.translate('paakayttaja_sukunimi')}</label>
                        <div>{userForm?.sukunimi}</div>
                        <label className="title">{i18n.translate('paakayttaja_email')}</label>
                        <div>{userForm?.email}</div>
                        <label className="title">{i18n.translate('paakayttaja_asiointikieli')}</label>
                        <div>{userForm?.asiointikieli === 'fi' ? 'Suomi' : 'Ruotsi'}</div>
                        <label className="title">{i18n.translate('paakayttaja_saateteksti')}</label>
                        <div>{userForm?.info}</div>
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
                                onClick={() => navigate('/hakija/jotpa/paakayttaja', { replace: true })}
                            >
                                {i18n.translate('nappi_edellinen_vaihe')}
                            </button>
                            <input type="submit" value={i18n.translate('nappi_laheta')} />
                        </div>
                    </div>
                </main>
            </form>
        </>
    );
}
