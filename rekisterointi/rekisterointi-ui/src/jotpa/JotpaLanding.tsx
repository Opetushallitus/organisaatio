import React from 'react';

import { Header } from '../Header';

import styles from './JotpaLanding.module.css';

export function JotpaLanding() {
    return (
        <>
            <Header title="Koulutuksen järjestäjien rekisteröityminen Jotpaa varten" />
            <main>
                <div className={styles.bannerContainer}>
                    <img className={styles.banner} src="/jotpa_banner.png" alt="Jotpa" />
                </div>
                <div className="content">
                    <p>Koulutuksen järjestäjien on rekisteröitävä tietonsa tietovarantoon (Jotpa) 1.1.2023 alkaen. </p>
                    <h2>Varmista ensin, että organisaatio on </h2>
                    <ul>
                        <li>merkitty kaupparekisteriin tai Yritys- ja yhteisötietojärjestelmään ja että</li>
                        <li>sinulla on järjestelmään merkitty oikeus asioida ko. organisaation puolesta.</li>
                    </ul>
                    <h2>Mikä on Jotpa?</h2>
                    <p>
                        Jatkuvan oppimisen ja työllisyyden palvelukeskus (lyhyemmin JOTPA) on uusi viranomainen, joka
                        edistää työikäisten osaamisen kehittämistä ja osaavan työvoiman saatavuutta.
                    </p>
                    <h2>Rekisteröityminen</h2>
                    <ul>
                        <li>Koulutuksen järjestäjä rekisteröi tiedot organisaatiosta alla olevan linkin kautta.</li>
                        <li>
                            Rekisteröityminen edellyttää vahvaa tunnistautumista (suomi.fi) henkilökohtaisilla
                            verkkopankkitunnuksilla tai mobiilivarmenteella. Rekisteröidessä asioidaan organisaation
                            puolesta. Jos listassa on monta organisaatiota, valitsethan rekisteröityvän koulutuksen
                            järjestäjän organisaation.
                        </li>
                    </ul>
                    <p>
                        <a href="/">Ohjeet palvelun käyttöön</a>
                    </p>
                    <p>
                        <a href="/">Jatkuvan oppimisen ja työllisyyden palvelukeskus (JOTPA) esittely</a>
                    </p>
                    <p>
                        <a href="/">Saavutettavuusseloste</a>
                    </p>
                    <button
                        role="link"
                        className={styles.registerButton}
                        onClick={() => (window.location.href = '/hakija/jotpa/aloitus')}
                    >
                        Aloita rekisteröityminen
                    </button>
                </div>
            </main>
        </>
    );
}
