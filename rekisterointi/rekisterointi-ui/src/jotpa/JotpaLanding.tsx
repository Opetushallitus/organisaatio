import React from 'react';
import Markdown from 'react-markdown';

import { Header } from '../Header';
import { useLanguageContext } from '../LanguageContext';

import styles from './JotpaLanding.module.css';

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

const RahoitusLogo = () => (
    <svg width="39" height="37" viewBox="0 0 39 37" fill="none" xmlns="http://www.w3.org/2000/svg">
        <ellipse cx="19.1827" cy="18.5" rx="18.1827" ry="17.5" fill="white" stroke="#4C4C4C" strokeWidth="2" />
        <rect width="24" height="24" transform="translate(7 6)" fill="white" />
        <path
            fillRule="evenodd"
            clipRule="evenodd"
            d="M24.1482 10.3699C23.9887 11.006 22.4996 13.4998 22.4996 13.4998C22.4996 13.4998 24.9519 15.7913 26.1224 17.9814C27.2928 20.1715 28.1846 23.4724 26.9996 25.7369C25.8146 28.0014 23.4959 28.0014 22.2724 28.0014H15.8734C15.8593 28.0014 15.8268 28.0049 15.7781 28.01C15.0811 28.0832 11.063 28.5056 10.1065 24.273C9.68996 22.4301 10.5552 19.2228 12.0468 16.8235C12.8482 15.5343 14.9816 13.6331 14.9816 13.6331C14.9816 13.6331 13.5771 11.683 13.2889 10.8558C13.0007 10.0285 13.7807 8.99984 14.9816 8.45969C16.1199 7.94768 17.9268 8.40452 18.8548 8.63916C18.9058 8.65205 18.9542 8.66427 18.9996 8.67562C19.3829 8.77143 19.4817 8.69681 19.6584 8.56337C19.8837 8.39322 20.2357 8.12742 21.4659 7.99741C23.6607 7.76545 24.3077 9.7338 24.1482 10.3699ZM17.4576 12.6909H19.777L20.6484 11.1449C20.6484 11.1449 19.4582 11.6026 18.5963 11.3411C17.7344 11.0797 16.5442 10.9649 16.5442 10.9649L17.4576 12.6909ZM17.4028 15.3496H20.0787C20.0787 15.3496 22.3648 17.044 23.6012 19.294C24.8377 21.544 25.2724 25.1423 23.3205 25.2689C21.3687 25.3956 16.2058 25.3782 14.6369 25.2689C13.068 25.1597 12.6525 23.9248 12.8569 22.4611C13.0612 20.9974 13.6055 19.3297 14.6369 17.9998C15.6683 16.67 17.4028 15.3496 17.4028 15.3496Z"
            fill="#4C4C4C"
        />
    </svg>
);

const RekisterointiLogo = () => (
    <svg width="39" height="37" viewBox="0 0 39 37" fill="none" xmlns="http://www.w3.org/2000/svg">
        <ellipse cx="19.1827" cy="18.5" rx="18.1827" ry="17.5" fill="white" stroke="#4C4C4C" strokeWidth="2" />
        <path
            d="M18.2781 24.4062L24.3609 18.395L23.325 17.3713L18.2781 22.3587L15.5422 19.655L14.5063 20.6787L18.2781 24.4062ZM12.5938 29C12.1688 29 11.7969 28.8425 11.4781 28.5275C11.1594 28.2125 11 27.845 11 27.425V9.575C11 9.155 11.1594 8.7875 11.4781 8.4725C11.7969 8.1575 12.1688 8 12.5938 8H22.1828L28 13.7487V27.425C28 27.845 27.8406 28.2125 27.5219 28.5275C27.2031 28.8425 26.8313 29 26.4062 29H12.5938ZM21.3859 14.4575V9.575H12.5938V27.425H26.4062V14.4575H21.3859ZM12.5938 9.575V14.4575V9.575V27.425V9.575Z"
            fill="#4C4C4C"
        />
    </svg>
);

const OdotusLogo = () => (
    <svg width="39" height="37" viewBox="0 0 39 37" fill="none" xmlns="http://www.w3.org/2000/svg">
        <ellipse cx="19.1827" cy="18.5" rx="18.1827" ry="17.5" fill="white" stroke="#4C4C4C" strokeWidth="2" />
        <g clipPath="url(#clip0_528_2701)">
            <path
                fillRule="evenodd"
                clipRule="evenodd"
                d="M13 9V15H13.01L13 15.01L17 19L13 23L13.01 23.01H13V29H25V23.01H24.99L25 23L21 19L25 15.01L24.99 15H25V9H13ZM23 23.5V27H15V23.5L19 19.5L23 23.5ZM19 18.5L15 14.5V11H23V14.5L19 18.5Z"
                fill="#4C4C4C"
            />
        </g>
        <defs>
            <clipPath id="clip0_528_2701">
                <rect width="24" height="24" fill="white" transform="translate(7 7)" />
            </clipPath>
        </defs>
    </svg>
);

const KayttajaLogo = () => (
    <svg width="39" height="37" viewBox="0 0 39 37" fill="none" xmlns="http://www.w3.org/2000/svg">
        <ellipse cx="19.1827" cy="18.5" rx="18.1827" ry="17.5" fill="white" stroke="#4C4C4C" strokeWidth="2" />
        <path
            d="M18.85 17.1265C19.2833 16.6113 19.6042 16.0799 19.8125 15.5325C20.0208 14.985 20.125 14.349 20.125 13.6245C20.125 12.8999 20.0208 12.2639 19.8125 11.7165C19.6042 11.169 19.2833 10.6377 18.85 10.1225C20.1167 9.84873 21.2292 10.0339 22.1875 10.6779C23.1458 11.322 23.625 12.3042 23.625 13.6245C23.625 14.9448 23.1458 15.927 22.1875 16.571C21.2292 17.2151 20.1167 17.4002 18.85 17.1265ZM24.25 25V22.7297C24.25 21.9086 24.0333 21.1438 23.6 20.4353C23.1667 19.7268 22.4167 19.1311 21.35 18.6481C24.2333 19.0023 26.2042 19.5175 27.2625 20.1938C28.3208 20.87 28.85 21.7153 28.85 22.7297V25H24.25ZM27 18.0201V15.6049H24.5V14.1558H27V11.7406H28.5V14.1558H31V15.6049H28.5V18.0201H27ZM14.875 17.2473C13.775 17.2473 12.875 16.9091 12.175 16.2329C11.475 15.5566 11.125 14.6872 11.125 13.6245C11.125 12.5618 11.475 11.6923 12.175 11.0161C12.875 10.3398 13.775 10.0017 14.875 10.0017C15.975 10.0017 16.875 10.3398 17.575 11.0161C18.275 11.6923 18.625 12.5618 18.625 13.6245C18.625 14.6872 18.275 15.5566 17.575 16.2329C16.875 16.9091 15.975 17.2473 14.875 17.2473ZM7 25V22.7297C7 22.1662 7.15417 21.655 7.4625 21.1961C7.77083 20.7372 8.18333 20.395 8.7 20.1696C9.9 19.6544 10.9708 19.2841 11.9125 19.0586C12.8542 18.8332 13.8417 18.7205 14.875 18.7205C15.9083 18.7205 16.8917 18.8332 17.825 19.0586C18.7583 19.2841 19.825 19.6544 21.025 20.1696C21.5417 20.395 21.9583 20.7372 22.275 21.1961C22.5917 21.655 22.75 22.1662 22.75 22.7297V25H7ZM14.875 15.7981C15.525 15.7981 16.0625 15.5929 16.4875 15.1823C16.9125 14.7717 17.125 14.2524 17.125 13.6245C17.125 12.9965 16.9125 12.4773 16.4875 12.0667C16.0625 11.6561 15.525 11.4508 14.875 11.4508C14.225 11.4508 13.6875 11.6561 13.2625 12.0667C12.8375 12.4773 12.625 12.9965 12.625 13.6245C12.625 14.2524 12.8375 14.7717 13.2625 15.1823C13.6875 15.5929 14.225 15.7981 14.875 15.7981ZM8.5 23.5509H21.25V22.7297C21.25 22.4721 21.1833 22.2306 21.05 22.0052C20.9167 21.7798 20.7083 21.6026 20.425 21.4738C19.275 20.9586 18.3 20.6124 17.5 20.4353C16.7 20.2582 15.825 20.1696 14.875 20.1696C13.925 20.1696 13.0542 20.2582 12.2625 20.4353C11.4708 20.6124 10.4833 20.9586 9.3 21.4738C9.05 21.5865 8.85417 21.7596 8.7125 21.9931C8.57083 22.2266 8.5 22.4721 8.5 22.7297V23.5509Z"
            fill="#4C4C4C"
        />
    </svg>
);

export function JotpaLanding() {
    const { i18n } = useLanguageContext();
    return (
        <>
            <Header title={i18n.translate('otsikko_rekisterointi')} />
            <main>
                <div className={styles.bannerContainer}>
                    <img className={styles.banner} src="/jotpa_banner.png" alt="Jotpa" />
                </div>
                <div className="content">
                    <Markdown>{i18n.translate('etusivu_mika_jotpa')}</Markdown>
                    <h3>{i18n.translate('etusivu_prosessikuvaus')}</h3>
                    <div className={styles.process}>
                        <div className={styles.processPhase}>
                            <div className={styles.topLine} />
                            <div>
                                <RahoitusLogo />
                            </div>
                            <div className={styles.processInfo}>
                                <Markdown>{i18n.translate('etusivu_prosessi_rahoitushaku')}</Markdown>
                            </div>
                        </div>
                        <div className={styles.activePhase}>
                            <div>
                                <RekisterointiLogo />
                            </div>
                            <div className={styles.processInfo}>
                                <Markdown>{i18n.translate('etusivu_prosessi_rekisterointi')}</Markdown>
                            </div>
                        </div>
                        <div className={styles.processPhase}>
                            <div className={styles.middleLine} />
                            <div>
                                <OdotusLogo />
                            </div>
                            <div className={styles.processInfo}>
                                <Markdown>{i18n.translate('etusivu_prosessi_odotus')}</Markdown>
                            </div>
                        </div>
                        <div className={styles.processPhase}>
                            <div className={styles.bottomLine} />
                            <div>
                                <KayttajaLogo />
                            </div>
                            <div className={styles.processInfo}>
                                <Markdown>{i18n.translate('etusivu_prosessi_kayttaja')}</Markdown>
                            </div>
                        </div>
                    </div>
                    <Markdown>{i18n.translate('etusivu_rekisterointi')}</Markdown>
                    <p className={styles.link}>
                        <a href="/">{i18n.translate('linkki_palvelun_kayttoohje')}</a>
                        <ExternalLink />
                    </p>
                    <p className={styles.link}>
                        <a href="/">{i18n.translate('linkki_jotpa_esittely')}</a>
                        <ExternalLink />
                    </p>
                    <p className={styles.link}>
                        <a href="/">{i18n.translate('linkki_saavutettavuusseloste')}</a>
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
