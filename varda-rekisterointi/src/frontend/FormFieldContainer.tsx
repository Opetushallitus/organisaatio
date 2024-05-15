import React from 'react';
import classNames from 'classnames';

type Props = {
    readOnly?: boolean;
    label: string;
    labelFor?: string;
    labelBy?: string;
    required?: boolean;
    errorText?: string;
    helpText?: string;
    children: React.ReactNode;
    ariaLisatietoId?: string;
    ariaLisatietoLokalisaatio?: string;
    ariaErrorKoosteId?: string;
};

export default function FormFieldContainer(props: Props) {
    const classnames = classNames({
        'oph-field': true,
        'oph-field-inline': true,
        'oph-field-is-required': props.required,
    });
    return (
        <div className={classnames}>
            <label id={props.labelBy} className="oph-label" htmlFor={props.labelFor}>
                {props.label}
            </label>
            {props.errorText || props.helpText ? (
                <div className="oph-input-container">
                    {props.children}
                    {props.errorText ? (
                        <div aria-describedby={props.ariaErrorKoosteId || ''} className="oph-field-text oph-error">
                            {props.errorText}
                        </div>
                    ) : null}
                    {!props.readOnly && props.helpText ? <div className="oph-field-text">{props.helpText}</div> : null}
                </div>
            ) : (
                props.children
            )}
            {props.ariaLisatietoId && (
                <p style={{ display: 'none' }} id={props.ariaLisatietoId}>
                    {props.ariaLisatietoLokalisaatio || 'Tähän lisätietoja ruudunlukijalle.'}
                </p>
            )}
        </div>
    );
}
