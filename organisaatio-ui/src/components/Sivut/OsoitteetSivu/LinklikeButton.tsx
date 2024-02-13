import React from 'react';
import styles from './LinklikeButton.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';

export type LinklikeButtonProps = React.PropsWithChildren<{
    disabled?: boolean;
    onClick: () => void;
}>;

export function LinklikeButton({ onClick, disabled = false, children }: LinklikeButtonProps) {
    const classes = [styles.LinklikeButton];
    if (disabled) classes.push(styles.LinklikeButtonDisabled);
    return (
        <Button variant={'text'} disabled={disabled} className={classes.join(' ')} onClick={onClick}>
            {children}
        </Button>
    );
}
