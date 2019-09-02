import React, { useContext } from 'react';
import classNames from 'classnames/bind';
import { LanguageContext } from '../contexts';

type Props = {
    currentStep: number,
}

export default function Navigation(props: Props) {
    const { i18n } = useContext(LanguageContext);
    function stepClasses(step: number) {
        return classNames({
            'step': true,
            'previous': props.currentStep > step,
            'current': props.currentStep === step,
        });
    }
    function lineClasses(step: number) {
        return classNames({
            'line': true,
            'previous': props.currentStep > step,
        })
    }
    return (
        <nav>
            <div>
                <div className={stepClasses(1)}>
                    <div className="circle">1</div>
                    <div>{i18n.translate('ORGANISAATION_TIEDOT')}</div>
                </div>
                <div className={lineClasses(1.5)}></div>
                <div className={stepClasses(2)}>
                    <div className="circle">2</div>
                    <div>{i18n.translate('KAYTTAJAN_YHTEYSTIEDOT')}</div>
                </div>
                <div className={lineClasses(2.5)}></div>
                <div className={stepClasses(3)}>
                    <div className="circle">3</div>
                    <div>{i18n.translate('YHTEENVETO')}</div>
                </div>
            </div>
        </nav>
    )
}
