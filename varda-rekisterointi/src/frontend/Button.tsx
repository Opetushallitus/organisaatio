import React from 'react';
import SpinnerInButton from './SpinnerInButton';

type Props = {
    tabIndex?: number,
    type?: 'submit' | 'reset' | 'button',
    disabled?: boolean,
    loading?: boolean,
    styling?: 'primary' | 'confirm' | 'cancel' | 'ghost';
    className?: string,
    onClick?: () => void,
    children: React.ReactNode,
}

export default function Button(props: Props) {
    return (
        <button tabIndex={props.tabIndex || 0}
                className={`oph-button oph-button-${props.styling} ${props.className ? props.className : ''}`}
                type={props.type}
                disabled={props.disabled}
                onClick={props.onClick}>
            {props.loading ? <SpinnerInButton /> : null}
            {props.children}
        </button>
    )
}
