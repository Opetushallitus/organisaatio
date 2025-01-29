import React from 'react';
import Markdown from 'react-markdown';
import { Helmet } from 'react-helmet';

import { Header } from './JotpaHeader';
import { useLanguageContext } from '../LanguageContext';
import { KayttajaLogo, OdotusLogo } from './JotpaProsessikuvaus';

import styles from './JotpaValmis.module.css';
import processStyles from './JotpaProsessikuvaus.module.css';

const SuccessLogo = () => (
    <svg width="121" height="117" viewBox="0 0 121 117" fill="none" xmlns="http://www.w3.org/2000/svg">
        <ellipse cx="60.5053" cy="58.3086" rx="58.5053" ry="56.3086" fill="white" stroke="#3A7A10" strokeWidth="3" />
        <path
            d="M57.5946 77.3127L77.1669 57.9707L73.8336 54.6767L57.5946 70.7246L48.7914 62.025L45.4581 65.319L57.5946 77.3127ZM39.3044 92.0938C37.9369 92.0938 36.7403 91.587 35.7147 90.5734C34.6891 89.5599 34.1763 88.3774 34.1763 87.026V29.5912C34.1763 28.2398 34.6891 27.0573 35.7147 26.0438C36.7403 25.0302 37.9369 24.5234 39.3044 24.5234H70.1585L88.876 43.0208V87.026C88.876 88.3774 88.3632 89.5599 87.3376 90.5734C86.312 91.587 85.1154 92.0938 83.7479 92.0938H39.3044ZM67.5944 45.3013V29.5912H39.3044V87.026H83.7479V45.3013H67.5944ZM39.3044 29.5912V45.3013V29.5912V87.026V29.5912Z"
            fill="#3A7A10"
        />
    </svg>
);

export function JotpaValmis() {
    const { i18n } = useLanguageContext();
    return (
        <>
            <Helmet>
                <meta name="robots" content="noindex" />
            </Helmet>
            <Header title={i18n.translate('otsikko_valmis')} />
            <main id="main">
                <div className="content" data-test-id="valmis-content">
                    <div className={styles.successContent}>
                        <div className={styles.success}>
                            <div>
                                <SuccessLogo />
                            </div>
                            <div className={styles.successDescription}>
                                <Markdown>{i18n.translate('valmis_info')}</Markdown>
                            </div>
                        </div>
                        <h3>{i18n.translate('valmis_seuraavat_vaiheet')}</h3>
                        <div className={processStyles.process}>
                            <div className={processStyles.processPhase}>
                                <div className={processStyles.topLine} />
                                <div>
                                    <OdotusLogo />
                                </div>
                                <div className={processStyles.processInfo}>
                                    <Markdown>{i18n.translate('valmis_odotus')}</Markdown>
                                </div>
                            </div>
                            <div className={processStyles.processPhase}>
                                <div className={processStyles.bottomLine} />
                                <div>
                                    <KayttajaLogo />
                                </div>
                                <div className={processStyles.processInfo}>
                                    <Markdown>{i18n.translate('valmis_kayttaja')}</Markdown>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </>
    );
}
