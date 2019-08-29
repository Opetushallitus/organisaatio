import React, { useState } from 'react';
import Button from './Button';

type Props = {
    getNavigation: (currentStep: number) => React.ReactNode,
    children: React.ReactNodeArray,
    disabled: boolean,
    changed: () => void,
    submit: () => Promise<void>,
    loading: boolean,
    error?: string,
}

export default function Wizard(props: Props) {
    const [currentStep, setCurrentStep] = useState(1);

    const steps = props.children.length;
    const child = props.children[currentStep - 1];

    function isPrev() {
        return currentStep > 1 && currentStep < steps;
    }

    function isNext() {
        return currentStep < steps - 1;
    }

    function isLast() {
        return currentStep >= steps - 1;
    }

    function isSubmit() {
        return currentStep === steps - 1;
    }

    function prev() {
        setCurrentStep(currentStep - 1);
        props.changed();
    }

    function next() {
        setCurrentStep(currentStep + 1);
        props.changed();
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
        <>
            {isLast() ? null : props.getNavigation(currentStep)}
            <section>
                {child}
                <div className="buttons">
                    {props.error
                        ? <span className="oph-error">{props.error}</span>
                        : null}
                    {isPrev()
                        ? <Button disabled={props.disabled || props.loading}
                                  styling="ghost"
                                  onClick={prev}>Edellinen vaihe</Button>
                        : null}
                    {isNext()
                        ? <Button type="submit"
                                  disabled={props.disabled || props.loading}
                                  styling="primary"
                                  onClick={next}>Seuraava vaihe</Button>
                        : null}
                    {isSubmit()
                        ? <Button type="submit"
                                  disabled={props.disabled || props.loading}
                                  loading={props.loading}
                                  styling="primary"
                                  onClick={submit}>
                              Lähetä hyväksyttäväksi
                          </Button>
                        : null}
                </div>
            </section>
        </>
    )
}
