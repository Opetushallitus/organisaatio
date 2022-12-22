import React from 'react';
import Markdown from 'react-markdown';
import { Helmet } from 'react-helmet';

import { Header } from './JotpaHeader';
import { useLanguageContext } from '../LanguageContext';

import styles from './JotpaLanding.module.css';
import { JotpaProsessikuvaus } from './JotpaProsessikuvaus';

const ExternalLink = () => (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path
            fillRule="evenodd"
            clipRule="evenodd"
            d="M19 19H5V5H12V3H5C3.89 3 3 3.9 3 5V19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V12H19V19ZM14 3V5H17.59L7.76 14.83L9.17 16.24L19 6.41V10H21V3H14Z"
            fill="#3A7A10"
        />
    </svg>
);

export function JotpaLanding() {
    const { i18n } = useLanguageContext();
    return (
        <>
            <Helmet>
                <title>{i18n.translate('title')}</title>
            </Helmet>
            <Header title={i18n.translate('otsikko_rekisterointi')} />
            <main id="main">
                <div className={styles.bannerContainer}>
                    <img className={styles.banner} src="/jotpa_banner.png" alt="" />
                </div>
                <div className="content">
                    <Markdown>{i18n.translate('etusivu_mika_jotpa')}</Markdown>
                    <h3>{i18n.translate('etusivu_prosessikuvaus')}</h3>
                    <JotpaProsessikuvaus />
                    <Markdown>{i18n.translate('etusivu_rekisterointi')}</Markdown>
                    <p className={styles.link}>
                        <a
                            href={i18n.translate('linkki_palvelun_kayttoohje_osoite')}
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            {i18n.translate('linkki_palvelun_kayttoohje')}
                        </a>
                        <ExternalLink />
                    </p>
                    <p className={styles.link}>
                        <a
                            href={i18n.translate('linkki_jotpa_esittely_osoite')}
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            {i18n.translate('linkki_jotpa_esittely')}
                        </a>
                        <ExternalLink />
                    </p>
                    <p className={styles.link}>
                        <a
                            href={i18n.translate('linkki_saavutettavuusseloste_osoite')}
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            {i18n.translate('linkki_saavutettavuusseloste')}
                        </a>
                    </p>
                    <button
                        role="link"
                        className={styles.registerButton}
                        onClick={() => (window.location.href = '/hakija/jotpa/organisaatio')}
                    >
                        {i18n.translate('etusivu_aloitus_nappi')}
                    </button>
                </div>
            </main>
        </>
    );
}
