import React, { useEffect } from 'react';
import { useNavigate } from 'react-router';
import Markdown from 'react-markdown';
import axios, { AxiosResponse } from 'axios';

import { useJotpaRekisterointiSelector } from './store';
import { Header } from './JotpaHeader';
import { findPostitoimipaikka } from '../addressUtils';
import { useKoodistos } from '../KoodistoContext';
import { useLanguageContext } from '../LanguageContext';
import { RegistrationProgressBar } from '../RegistrationProgressBar';
import { getLanguageName, RekisterointiRequest } from '../types';

import styles from './jotpa.module.css';
import { OrganisationFormState } from '../organisationSlice';
import { UserFormState } from '../userSlice';
import { format, parse } from 'date-fns';

export function JotpaYhteenveto() {
    const navigate = useNavigate();
    const { language, i18n } = useLanguageContext();
    const { organisaatiotyypit, posti } = useKoodistos();
    const {
        organisation: { initialOrganisation, form: organisationForm },
        user: { form: userForm },
    } = useJotpaRekisterointiSelector((state) => state);

    useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

    const kayntiosoite = organisationForm?.copyKayntiosoite
        ? organisationForm.postiosoite
        : organisationForm?.kayntiosoite!;
    const kayntipostinumero = organisationForm?.copyKayntiosoite
        ? organisationForm.postinumero
        : organisationForm?.kayntipostinumero!;

    const onSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        e.stopPropagation();
        const organisation: OrganisationFormState = organisationForm!;
        const user: UserFormState = userForm!;
        await axios.post<string, AxiosResponse<string>, RekisterointiRequest>('/hakija/api/rekisterointi', {
            ...organisation,
            yritysmuoto: organisation.yritysmuoto.value!,
            kotipaikka: organisation.kotipaikka.value!,
            alkamisaika: format(parse(organisation.alkamisaika, 'd.M.yyyy', new Date()), 'yyyy-MM-dd'),
            postinumero: `posti_${organisation.postinumero}`,
            postitoimipaikka: findPostitoimipaikka(organisation.postinumero, posti, language)!,
            kayntiosoite,
            kayntipostinumero: `posti_${kayntipostinumero}`,
            kayntipostitoimipaikka: findPostitoimipaikka(kayntipostinumero, posti, language)!,
            emails: organisation.emails.map((e) => e.email).filter((e) => !!e) as string[],
            ...user,
        });
        window.location.href = '/hakija/logout?redirect=/jotpa/valmis';
    };

    return (
        <>
            <Header title={i18n.translate('otsikko_rekisterointi')} />
            <form onSubmit={onSubmit}>
                <main id="main">
                    <div id="content" className="content">
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
                        <div data-test-id="yrityksen-nimi">{initialOrganisation?.ytjNimi.nimi}</div>
                        <label className="title">{i18n.translate('organisaatio_perustiedot_ytunnus')}</label>
                        <div data-test-id="ytunnus">{initialOrganisation?.ytunnus}</div>
                        <label className="title">{i18n.translate('organisaatio_perustiedot_yritysmuoto')}</label>
                        <div data-test-id="yritysmuoto">{organisationForm?.yritysmuoto.label}</div>
                        <label className="title">{i18n.translate('organisaatio_perustiedot_organisaatiotyyppi')}</label>
                        <div data-test-id="organisaatiotyyppi">
                            {organisaatiotyypit.find((o) => o.uri === 'organisaatiotyyppi_01')?.nimi[language]}
                        </div>
                        <label className="title">{i18n.translate('organisaatio_perustiedot_kotipaikka')}</label>
                        <div data-test-id="kotipaikka">{organisationForm?.kotipaikka.label}</div>
                        <label className="title">{i18n.translate('organisaatio_perustiedot_alkamisaika')}</label>
                        <div data-test-id="alkamisaika">{organisationForm?.alkamisaika}</div>
                        <h3>{i18n.translate('organisaatio_yhteystiedot')}</h3>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_puhelinnumero')}</label>
                        <div data-test-id="puhelinnumero">{organisationForm?.puhelinnumero}</div>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_email')}</label>
                        <div data-test-id="organisaatio-email">{organisationForm?.email}</div>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_postiosoite')}</label>
                        <div data-test-id="postiosoite">{organisationForm?.postiosoite}</div>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_postinumero')}</label>
                        <div data-test-id="postinumero">{organisationForm?.postinumero}</div>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_postitoimipaikka')}</label>
                        <div data-test-id="postitoimipaikka">
                            {organisationForm?.postinumero &&
                                findPostitoimipaikka(organisationForm?.postinumero, posti, language)}
                        </div>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_kayntiosoite')}</label>
                        <div data-test-id="kayntiosoite">{kayntiosoite}</div>
                        <label className="title">{i18n.translate('organisaatio_yhteystiedot_kayntipostinumero')}</label>
                        <div data-test-id="kayntipostinumero">{kayntipostinumero}</div>
                        <label className="title">
                            {i18n.translate('organisaatio_yhteystiedot_kayntipostitoimipaikka')}
                        </label>
                        <div data-test-id="kayntipostitoimipaikka">
                            {kayntipostinumero && findPostitoimipaikka(kayntipostinumero, posti, language)}
                        </div>
                        <h3>{i18n.translate('organisaatio_email')}</h3>
                        <label className="title">{i18n.translate('organisaatio_email')}</label>
                        <div data-test-id="emails">
                            {organisationForm?.emails.map((e) => (
                                <div key={e.email}>{e.email}</div>
                            ))}
                        </div>
                        <h3>{i18n.translate('paakayttaja_otsikko')}</h3>
                        <label className="title">{i18n.translate('paakayttaja_etunimi')}</label>
                        <div data-test-id="etunimi">{userForm?.etunimi}</div>
                        <label className="title">{i18n.translate('paakayttaja_sukunimi')}</label>
                        <div data-test-id="sukunimi">{userForm?.sukunimi}</div>
                        <label className="title">{i18n.translate('paakayttaja_email')}</label>
                        <div data-test-id="paakayttaja-email">{userForm?.paakayttajaEmail}</div>
                        <label className="title">{i18n.translate('paakayttaja_asiointikieli')}</label>
                        <div data-test-id="asiointikieli">{getLanguageName(userForm?.asiointikieli!, language)}</div>
                        <label className="title">{i18n.translate('paakayttaja_saateteksti')}</label>
                        <div data-test-id="info">{userForm?.info}</div>
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
