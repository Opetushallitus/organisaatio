import React from 'react';
import styles from './Fieldset.module.css';
import classNames from 'classnames/bind';

type Props = {
    title?: string,
    description?: string,
    children?: React.ReactNode,
}

export default function Fieldset(props: Props) {
    return (
        <fieldset className={classNames("oph-fieldset", styles.fieldset)}>
            {props.title ? <legend className="oph-label">{props.title}</legend> : null}
            {props.description ? <div>{props.description}</div> : null}
            {props.children}
        </fieldset>
    )
}
