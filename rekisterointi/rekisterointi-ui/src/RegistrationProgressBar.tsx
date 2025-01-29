import React from 'react';

import { useLanguageContext } from './LanguageContext';

import styles from './RegistrationProgressBar.module.css';

type RegistrationProgressBarProps = {
    currentPhase: number;
    phaseTranslationKeys: string[];
};

type PhaseProps = {
    phase: number;
};

const ActivePhase = ({ phase }: PhaseProps) => (
    <svg width="37" height="35" viewBox="0 0 37 35" fill="none" xmlns="http://www.w3.org/2000/svg">
        <ellipse cx="18.7286" cy="17.5" rx="18.1827" ry="17.5" fill="#3A7A10" />
        <text x="50%" y="67%" textAnchor="middle" fill="#FFFFFF" fontSize="large">
            {phase}
        </text>
    </svg>
);

const DonePhase = () => (
    <svg width="37" height="35" viewBox="0 0 37 35" fill="none" xmlns="http://www.w3.org/2000/svg">
        <ellipse cx="18.2257" cy="17.5" rx="18.1827" ry="17.5" fill="#3A7A10" />
        <path
            fillRule="evenodd"
            clipRule="evenodd"
            d="M15.1089 21.6698L10.7762 17.4998L9.30078 18.9098L15.1089 24.4998L27.577 12.4998L26.112 11.0898L15.1089 21.6698Z"
            fill="white"
        />
    </svg>
);

const UpcomingPhase = ({ phase }: PhaseProps) => (
    <svg width="40" height="37" viewBox="0 0 40 37" fill="none" xmlns="http://www.w3.org/2000/svg">
        <ellipse cx="19.8761" cy="18.5" rx="18.1827" ry="17.5" fill="white" stroke="#B2B2B2" strokeWidth="2" />
        <text x="50%" y="67%" textAnchor="middle" fill="#4c4c4c" fontSize="large">
            {phase}
        </text>
    </svg>
);

export const RegistrationProgressBar = ({ currentPhase, phaseTranslationKeys }: RegistrationProgressBarProps) => {
    const { i18n } = useLanguageContext();
    return (
        <div className={styles.progressBar} aria-hidden={true}>
            {phaseTranslationKeys.map((k, idx) => {
                const dottedLineClassName =
                    idx === 0
                        ? styles.leftLine
                        : idx === phaseTranslationKeys.length - 1
                        ? styles.rightLine
                        : styles.fullLine;
                const LogoElement =
                    idx === currentPhase - 1 ? (
                        <ActivePhase phase={currentPhase} />
                    ) : idx < currentPhase ? (
                        <DonePhase />
                    ) : (
                        <UpcomingPhase phase={idx + 1} />
                    );
                return (
                    <div key={k} className={styles.phase}>
                        <div className={dottedLineClassName} />
                        <div>{LogoElement}</div>
                        <div className={styles.phaseName}>
                            <span className={currentPhase === idx + 1 ? styles.activeName : undefined}>
                                {i18n.translate(k)}
                            </span>
                        </div>
                    </div>
                );
            })}
        </div>
    );
};
