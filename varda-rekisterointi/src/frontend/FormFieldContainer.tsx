import React from 'react';
import classNames from 'classnames/bind';

type Props = {
    label: string,
    labelFor?: string,
    required?: boolean,
    helpText?: string,
    children: React.ReactNode,
}

export default function FormFieldContainer(props: Props) {
    const classnames = classNames({
        'oph-field': true,
        'oph-field-inline': true,
        'oph-field-is-required': props.required,
    });
    return (
        <div className={classnames}>
            <label className="oph-label" htmlFor={props.labelFor}>{props.label}</label>
            {props.helpText
                ? <div className="oph-input-container">{props.children}<div className="oph-field-text">{props.helpText}</div></div>
                : props.children}
        </div>
    )
}
