import React, { useState, useContext } from 'react';
import Button from './Button';
import { LanguageContext } from './contexts';
import styles from './Wizard.module.css';

type Props = {
    getNavigation: (currentStep: number) => React.ReactNode;
    children: React.ReactNodeArray;
    disabled: boolean;
    validate: (currentStep: number) => boolean;
    submit: () => Promise<void>;
    loading: boolean;
    error?: string;
    isVirkailija?: boolean;
};

export default function Wizard(props: Props) {
    const { i18n } = useContext(LanguageContext);
    const [currentStep, setCurrentStep] = useState(1);

    const steps = props.children.length;
    const child = props.children[currentStep - 1];

    function isPrev() {
        return currentStep > 1 && currentStep <= steps;
    }

    function isNext() {
        return currentStep < steps;
    }

    function isLast() {
        return currentStep >= steps;
    }

    function isSubmit() {
        return currentStep === steps;
    }

    function prev() {
        setCurrentStep(currentStep - 1);
    }

    function next() {
        if (props.validate(currentStep)) {
            setCurrentStep(currentStep + 1);
        }
    }

    async function logout() {
        window.location.href = props.isVirkailija
            ? '/varda-rekisterointi/virkailija'
            : '/varda-rekisterointi/hakija/logout';
    }

    async function submit() {
        try {
            await props.submit();
            next();
        } catch (error) {
            console.log(error);
        }
    }

    return (
        <main>
            {isLast() ? null : props.getNavigation(currentStep)}
            <section className={styles.section}>
                <div className={styles.child}>{child}</div>
                <div className={styles.buttons}>
                    {props.error ? <span className="oph-error">{props.error}</span> : null}
                    {[1, 2, 3].includes(currentStep) && (
                        <Button disabled={props.disabled || props.loading} styling="cancel" onClick={logout}>
                            {i18n.translate('KESKEYTÄ')}
                        </Button>
                    )}
                    {isPrev() ? (
                        <Button disabled={props.disabled || props.loading} styling="ghost" onClick={prev}>
                            {i18n.translate('EDELLINEN_VAIHE')}
                        </Button>
                    ) : null}
                    {isNext() ? (
                        <Button
                            type="submit"
                            disabled={props.disabled || props.loading}
                            styling="primary"
                            onClick={next}
                        >
                            {i18n.translate('SEURAAVA_VAIHE')}
                        </Button>
                    ) : null}
                    {isSubmit() ? (
                        <Button
                            type="submit"
                            disabled={props.disabled || props.loading}
                            loading={props.loading}
                            styling="primary"
                            onClick={submit}
                        >
                            {i18n.translate('LAHETA_HYVAKSYTTAVAKSI')}
                        </Button>
                    ) : null}
                </div>
            </section>
        </main>
    );
}
