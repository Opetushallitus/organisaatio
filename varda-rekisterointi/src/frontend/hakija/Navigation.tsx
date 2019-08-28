import React from 'react';
import classNames from 'classnames/bind';

type Props = {
    currentStep: number,
}

export default function Navigation(props: Props) {
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
                    <div>Organisaation tiedot</div>
                </div>
                <div className={lineClasses(1.5)}></div>
                <div className={stepClasses(2)}>
                    <div className="circle">2</div>
                    <div>Varda-pääkäyttäjän yhteystiedot</div>
                </div>
                <div className={lineClasses(2.5)}></div>
                <div className={stepClasses(3)}>
                    <div className="circle">3</div>
                    <div>Yhteenveto</div>
                </div>
            </div>
        </nav>
    )
}
