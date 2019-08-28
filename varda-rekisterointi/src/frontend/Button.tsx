import React from 'react';
import SpinnerInButton from './SpinnerInButton';

type Props = {
    type?: 'submit' | 'reset' | 'button',
    disabled?: boolean,
    loading?: boolean,
    styling?: 'primary' | 'confirm' | 'cancel' | 'ghost';
    onClick?: () => void,
    children: React.ReactNode,
}

export default function Button(props: Props) {
    return (
        <button className={`oph-button oph-button-${props.styling}`}
                type={props.type}
                disabled={props.disabled}
                onClick={props.onClick}>
            {props.loading ? <SpinnerInButton /> : null}
            {props.children}
        </button>
    )
}
