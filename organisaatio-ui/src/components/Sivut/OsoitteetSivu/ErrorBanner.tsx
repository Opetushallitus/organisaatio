import React from 'react';
import styles from './ErrorBanner.module.css';

type BannerProps = React.PropsWithChildren<{
    onClose?(): void;
}>;
export function ErrorBanner({ children, onClose }: BannerProps) {
    return (
        <Banner onClose={onClose} variant={styles.Error}>
            {children}
        </Banner>
    );
}
export function InfoBanner({ children, onClose }: BannerProps) {
    return (
        <Banner onClose={onClose} variant={styles.Partial}>
            {children}
        </Banner>
    );
}

function Banner({ children, onClose, variant }: BannerProps & { variant: string }) {
    return (
        <div className={styles.Banner + ' ' + variant}>
            <ErrorIcon />
            <div>{children}</div>
            {onClose && <CloseIcon onClick={onClose} />}
        </div>
    );
}

function ErrorIcon() {
    return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
                d="M9.325 15H10.825V9H9.325V15ZM9.99955 7.15C10.2332 7.15 10.4292 7.07333 10.5875 6.92C10.7458 6.76667 10.825 6.57667 10.825 6.35C10.825 6.10917 10.746 5.90729 10.5879 5.74438C10.4299 5.58146 10.2341 5.5 10.0004 5.5C9.76682 5.5 9.57083 5.58146 9.4125 5.74438C9.25417 5.90729 9.175 6.10917 9.175 6.35C9.175 6.57667 9.25402 6.76667 9.41205 6.92C9.5701 7.07333 9.76593 7.15 9.99955 7.15ZM10.0066 20C8.62775 20 7.33192 19.7375 6.11915 19.2125C4.90638 18.6875 3.84583 17.9708 2.9375 17.0625C2.02917 16.1542 1.3125 15.093 0.7875 13.879C0.2625 12.665 0 11.3678 0 9.9875C0 8.60718 0.2625 7.31003 0.7875 6.09602C1.3125 4.88201 2.02917 3.825 2.9375 2.925C3.84583 2.025 4.90701 1.3125 6.12103 0.7875C7.33502 0.2625 8.63218 0 10.0125 0C11.3928 0 12.69 0.2625 13.904 0.7875C15.118 1.3125 16.175 2.025 17.075 2.925C17.975 3.825 18.6875 4.88333 19.2125 6.1C19.7375 7.31667 20 8.61445 20 9.99335C20 11.3722 19.7375 12.6681 19.2125 13.8808C18.6875 15.0936 17.975 16.1526 17.075 17.0579C16.175 17.9632 15.1167 18.6798 13.9 19.2079C12.6833 19.736 11.3855 20 10.0066 20ZM10.0125 18.5C12.3708 18.5 14.375 17.6708 16.025 16.0125C17.675 14.3542 18.5 12.3458 18.5 9.9875C18.5 7.62917 17.6766 5.625 16.0297 3.975C14.3828 2.325 12.3729 1.5 10 1.5C7.65 1.5 5.64583 2.32343 3.9875 3.9703C2.32917 5.61718 1.5 7.62708 1.5 10C1.5 12.35 2.32917 14.3542 3.9875 16.0125C5.64583 17.6708 7.65417 18.5 10.0125 18.5Z"
                fill="#666666"
            />
        </svg>
    );
}

function CloseIcon({ onClick }: { onClick(): void }) {
    return (
        <button onClick={onClick} className={styles.CloseButton} aria-label="Sulje banneri">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                    d="M2.35865 14.6666L1.33301 13.641L6.97403 7.99998L1.33301 2.35895L2.35865 1.33331L7.99967 6.97434L13.6407 1.33331L14.6663 2.35895L9.02532 7.99998L14.6663 13.641L13.6407 14.6666L7.99967 9.02562L2.35865 14.6666Z"
                    fill="#2A2A2A"
                />
            </svg>
        </button>
    );
}
