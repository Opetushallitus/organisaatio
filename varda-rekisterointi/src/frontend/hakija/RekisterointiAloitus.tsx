import React, { useContext } from 'react';
import Header from './Header';
import { LanguageContext } from '../contexts';
import styles from './RekisterointiAloitus.module.css';
import { Language } from '../types/types';
import lapset from './Lapset.png';
import Footer from './Footer';

type Data = {
    kieli: Language;
    url: string;
};

const data: Record<Language, Data> = {
    fi: { kieli: 'fi', url: 'https://www.oph.fi/fi/varda' },
    sv: { kieli: 'sv', url: 'https://www.oph.fi/sv/varda' },
    en: { kieli: 'en', url: 'https://www.oph.fi/en/varda' },
};

export default function RekisterointiAloitus() {
    const { i18n, language } = useContext(LanguageContext);

    function renderByKieli(data: Data) {
        return (
            <div>
                <p>{i18n.translateWithLang('HAKIJA_ALOITUS_KUVAUS', data.kieli)}</p>
                <p>
                    <strong>{i18n.translateWithLang('HAKIJA_ALOITUS_OTSIKKO1', data.kieli)}</strong>
                </p>
                <p>{i18n.translateWithLang('HAKIJA_ALOITUS_TEKSTI1', data.kieli)}</p>
                <p>
                    <strong>{i18n.translateWithLang('HAKIJA_ALOITUS_OTSIKKO2', data.kieli)}</strong>
                </p>
                <p>{i18n.translateWithLang('HAKIJA_ALOITUS_TEKSTI2', data.kieli)}</p>
                <p>
                    <a
                        className="oph-link"
                        href={i18n.translateWithLang('LUE_LISAA_OHJE_URL', data.kieli)}
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        {i18n.translateWithLang('LUE_LISAA_OHJE', data.kieli)}
                    </a>
                    <br />
                    <br />
                    <a
                        className="oph-link"
                        href={i18n.translateWithLang('LUE_LISAA_SAAVUTETTAVUUS_URL', data.kieli)}
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        {i18n.translateWithLang('LUE_LISAA_SAAVUTETTAVUUS', data.kieli)}
                    </a>
                </p>
                <p>
                    <a
                        className={`oph-button oph-button-primary ${styles.varimuutos}`}
                        href="/varda-rekisterointi/hakija"
                    >
                        {i18n.translateWithLang('HAKIJA_ALOITA_REKISTEROITYMINEN', data.kieli)}
                    </a>
                </p>
                <p>
                    <a className="oph-link" href={data.url}>
                        {data.url}
                    </a>
                </p>
            </div>
        );
    }

    return (
        <div>
            <Header></Header>
            <div className={styles.section}>
                <div className={styles.child}>
                    <div className={styles.logo}>
                        <img src={lapset} alt={i18n.translate('KUVA_LAPSISTA')} />
                    </div>
                    {renderByKieli(data[language])}
                </div>
            </div>
            <Footer></Footer>
        </div>
    );
}
