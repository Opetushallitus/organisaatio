import styles from './ViestiStatusView.module.css';
import React from 'react';

type ModalishBoxProps = React.PropsWithChildren<{
    className: string;
}>;

export function ModalishBox({ children, className }: ModalishBoxProps) {
    const classList = [styles.ModalishBox, className];
    return (
        <div className={classList.join(' ')}>
            <div className={styles.Content}>{children}</div>
        </div>
    );
}
