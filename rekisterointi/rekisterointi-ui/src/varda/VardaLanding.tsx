import React, { useContext } from 'react';

import Header from './Header';
import Footer from './Footer';
import { LanguageContext } from '../contexts';
import { Language } from '../types';

import styles from './VardaLanding.module.css';

type Data = {
    kieli: Language;
    url: string;
};

const data: Record<Language, Data> = {
    fi: { kieli: 'fi', url: 'https://www.oph.fi/fi/varda' },
    sv: { kieli: 'sv', url: 'https://www.oph.fi/sv/varda' },
    en: { kieli: 'en', url: 'https://www.oph.fi/en/varda' },
};

export function VardaLanding() {
    const { i18n, language } = useContext(LanguageContext);

    function renderByKieli(data: Data) {
        return (
            <div>
                <p>{i18n.translate('HAKIJA_ALOITUS_KUVAUS')}</p>
                <h1>{i18n.translate('HAKIJA_ALOITUS_OTSIKKO1')}</h1>
                <p>{i18n.translate('HAKIJA_ALOITUS_TEKSTI1')}</p>
                <h2>{i18n.translate('HAKIJA_ALOITUS_OTSIKKO2')}</h2>
                <p>{i18n.translate('HAKIJA_ALOITUS_TEKSTI2')}</p>
                <p>
                    <a
                        className={`oph-link ${styles.accessible_colors}`}
                        href={i18n.translate('LUE_LISAA_OHJE_URL')}
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        {i18n.translate('LUE_LISAA_OHJE')}
                    </a>
                    <br />
                    <br />
                    <a
                        className={`oph-link ${styles.accessible_colors}`}
                        href={i18n.translate('LUE_LISAA_SAAVUTETTAVUUS_URL')}
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        {i18n.translate('LUE_LISAA_SAAVUTETTAVUUS')}
                    </a>
                </p>
                <p>
                    <a className={`oph-button oph-button-primary ${styles.varimuutos}`} href="/hakija/aloitus">
                        {i18n.translate('HAKIJA_ALOITA_REKISTEROITYMINEN')}
                    </a>
                </p>
                <p>
                    <a className={`oph-link ${styles.accessible_colors}`} href={data.url}>
                        {data.url}
                    </a>
                </p>
            </div>
        );
    }

    return (
        <div>
            <Header></Header>
            <main className={styles.section}>
                <div className={styles.child}>
                    <div className={styles.logo}>
                        <img src="/Lapset.png" alt={i18n.translate('KUVA_LAPSISTA')} />
                    </div>
                    {renderByKieli(data[language])}
                </div>
            </main>
            <Footer></Footer>
        </div>
    );
}
