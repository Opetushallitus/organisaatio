import React from 'react';
import styles from './Fieldset.module.css';
import classNames from 'classnames';

type Props = {
    title?: string;
    description?: string | string[];
    children?: React.ReactNode;
};

function renderDesc(description: string) {
    return <p>{description}</p>;
}

export default function Fieldset(props: Props) {
    return (
        <fieldset className={classNames('oph-fieldset', styles.fieldset)}>
            {props.title ? <legend className="oph-label">{props.title}</legend> : null}
            {props.description
                ? Array.isArray(props.description)
                    ? props.description.map(renderDesc)
                    : renderDesc(props.description)
                : null}
            {props.children}
        </fieldset>
    );
}
