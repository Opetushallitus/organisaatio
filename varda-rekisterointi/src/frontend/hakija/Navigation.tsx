import React, { useContext } from 'react';
import classNames from 'classnames';
import { LanguageContext } from '../contexts';
import styles from './Navigation.module.css';

type Props = {
    currentStep: number,
}

const ORGANISAATION_TIEDOT= 1;
const KAYTTAJAN_YHTEYSTIEDOT = 2;
const YHTEENVETO = 3;

export default function Navigation(props: Props) {
    const { i18n } = useContext(LanguageContext);
    function stepClasses(step: number) {
        return classNames({
            [styles.step]: true,
            [styles.previous]: props.currentStep > step,
            [styles.current]: props.currentStep === step,
        });
    }
    function lineClasses(step: number) {
        return classNames({
            [styles.line]: true,
            [styles.previous]: props.currentStep > step,
        })
    }

    return (
        <nav className={styles.nav}>
            <div>
                <div className={stepClasses(ORGANISAATION_TIEDOT)}>
                    <div className={styles.circle}>1</div>
                    <div {...(props.currentStep === ORGANISAATION_TIEDOT ? {'aria-current': 'step'} : {} )} aria-label={i18n.translate('ORGANISAATION_TIEDOT')}>{i18n.translate('ORGANISAATION_TIEDOT')}</div>
                </div>
                <div className={lineClasses(1.5)}></div>
                <div className={stepClasses(2)}>
                    <div className={styles.circle}>2</div>
                    <div {...(props.currentStep === KAYTTAJAN_YHTEYSTIEDOT ? {'aria-current': 'step'} : {} )} aria-label={i18n.translate('KAYTTAJAN_YHTEYSTIEDOT')}>{i18n.translate('KAYTTAJAN_YHTEYSTIEDOT')}</div>
                </div>
                <div className={lineClasses(2.5)}></div>
                <div className={stepClasses(3)}>
                    <div className={styles.circle}>3</div>
                    <div {...(props.currentStep === YHTEENVETO ? {'aria-current': 'step'} : {} )} aria-label={i18n.translate('YHTEENVETO')} >{i18n.translate('YHTEENVETO')}</div>
                </div>
                <p className={styles.ariaLabel}>{`${i18n.translate('REKISTEROINTI_VAIHE')} ${props.currentStep}: ${i18n.translate('ORGANISAATION_TIEDOT')}`}</p>
            </div>
        </nav>
    )
}
