import React from 'react';
import styles from './LinklikeButton.module.css';

export type LinklikeButtonProps = React.PropsWithChildren<{
    disabled?: boolean;
    onClick?: () => void;
}>;

function noop() {}

export function LinklikeButton({ onClick = noop, disabled = false, children }: LinklikeButtonProps) {
    const classes = [styles.LinklikeButton];
    if (disabled) classes.push(styles.LinklikeButtonDisabled);
    return (
        <div className={classes.join(' ')} role="button" onClick={onClick} aria-disabled={disabled}>
            {children}
        </div>
    );
}
