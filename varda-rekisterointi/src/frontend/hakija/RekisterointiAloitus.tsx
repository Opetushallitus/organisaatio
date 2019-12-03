import React, { useContext } from 'react';
import Header from './Header';
import { LanguageContext } from '../contexts';
import styles from './RekisterointiAloitus.module.css';
import { Language } from '../types';
import lapset from './Lapset.png';

type Data = {
    kieli: Language,
    url: string,
}

const data: Array<Data> = [
    { kieli: 'fi', url: 'https://www.oph.fi/fi/palvelut/varhaiskasvatuksen-tietovaranto-varda' },
    { kieli: 'sv', url: 'https://www.oph.fi/sv/tjanster/informationsresursen-inom-smabarnspedagogiken-varda' },
];

export default function RekisterointiAloitus() {
    const { i18n } = useContext(LanguageContext);

    function renderByKieli(data: Data) {
        return <div>
            <p>
                {i18n.translateWithLang('HAKIJA_ALOITUS_KUVAUS', data.kieli)}
            </p>
            <p>
                <strong>{i18n.translateWithLang('HAKIJA_ALOITUS_OTSIKKO1', data.kieli)}</strong>
            </p>
            <p>
                {i18n.translateWithLang('HAKIJA_ALOITUS_TEKSTI1', data.kieli)}
            </p>
            <p>
                <strong>{i18n.translateWithLang('HAKIJA_ALOITUS_OTSIKKO2', data.kieli)}</strong>
            </p>
            <p>
                {i18n.translateWithLang('HAKIJA_ALOITUS_TEKSTI2', data.kieli)}
            </p>
            <p>
                <a className="oph-button oph-button-primary" href={`/varda-rekisterointi/hakija?locale=${data.kieli}`}>
                    {i18n.translateWithLang('HAKIJA_ALOITA_REKISTEROITYMINEN', data.kieli)}
                </a>
            </p>
            <p>
                <a className="oph-link" href={data.url}>
                    {data.url}
                </a>
            </p>
        </div>;
    }

    return <div>
        <Header hideLanguage={true}></Header>
        <div className={styles.section}>
            <div className={styles.child}>
                <div className={styles.logo}>
                    <img src={lapset} />
                </div>
                { data.map(renderByKieli) }
            </div>
        </div>
    </div>;
}
