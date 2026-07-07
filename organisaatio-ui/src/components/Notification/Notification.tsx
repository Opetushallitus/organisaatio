import React from 'react';
import { atom, useAtom } from 'jotai';
import { languageAtom } from '../../api/lokalisaatio';
import { jotaiStore } from '../../jotaiStore';
import styles from './Notification.module.css';

const DEFAULT_TIMEOUT = 5000;

type NotificationType = 'success' | 'warning' | 'info' | 'danger';

type NotificationPayload = {
    message: string;
    title?: string;
    timeOut?: number;
};

type Toast = NotificationPayload & {
    id: string;
    type: NotificationType;
};

const toastsAtom = atom<Toast[]>([]);
let toastId = 0;

const removeToast = (id: string) => {
    jotaiStore.set(toastsAtom, (toasts) => toasts.filter((toast) => toast.id !== id));
};

const addToast = (type: NotificationType, props: NotificationPayload) => {
    const id = `${Date.now()}-${toastId++}`;
    jotaiStore.set(toastsAtom, (toasts) => [...toasts, { ...props, id, type }]);
    globalThis.setTimeout(() => removeToast(id), props.timeOut || DEFAULT_TIMEOUT);
};

const ToastIcon = ({ type }: { type: NotificationType }) => {
    if (type === 'success') {
        return (
            <svg aria-hidden="true" viewBox="0 0 24 24" className={styles.icon}>
                <path
                    d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 20C7.59 20 4 16.41 4 12C4 7.59 7.59 4 12 4C16.41 4 20 7.59 20 12C20 16.41 16.41 20 12 20ZM16.59 7.58L10 14.17L7.41 11.59L6 13L10 17L18 9L16.59 7.58Z"
                    fill="currentColor"
                />
            </svg>
        );
    }

    if (type === 'info') {
        return (
            <svg aria-hidden="true" viewBox="0 0 24 24" className={styles.icon}>
                <path
                    d="M11 17H13V11H11V17ZM12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 20C7.59 20 4 16.41 4 12C4 7.59 7.59 4 12 4C16.41 4 20 7.59 20 12C20 16.41 16.41 20 12 20ZM11 9H13V7H11V9Z"
                    fill="currentColor"
                />
            </svg>
        );
    }

    return (
        <svg aria-hidden="true" viewBox="0 0 24 24" className={styles.icon}>
            <path
                d="M11 15H13V17H11V15ZM11 7H13V13H11V7ZM11.99 2C6.47 2 2 6.48 2 12C2 17.52 6.47 22 11.99 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 11.99 2ZM12 20C7.58 20 4 16.42 4 12C4 7.58 7.58 4 12 4C16.42 4 20 7.58 20 12C20 16.42 16.42 20 12 20Z"
                fill="currentColor"
            />
        </svg>
    );
};

const CloseIcon = () => (
    <svg aria-hidden="true" viewBox="0 0 24 24" className={styles.closeIcon}>
        <path
            d="M19 6.41L17.59 5L12 10.59L6.41 5L5 6.41L10.59 12L5 17.59L6.41 19L12 13.41L17.59 19L19 17.59L13.41 12L19 6.41Z"
            fill="currentColor"
        />
    </svg>
);

const Toast = ({ toast, onClose }: { toast: Toast; onClose: (id: string) => void }) => {
    const [i18n] = useAtom(languageAtom);
    const closeLabel = i18n.translate('BUTTON_SULJE', false) || 'Sulje';
    const toastClassName = `${styles.toast} ${styles[toast.type]} ${toast.title ? '' : styles.withoutTitle}`;

    return (
        <div className={toastClassName} role={toast.type === 'danger' || toast.type === 'warning' ? 'alert' : 'status'}>
            <ToastIcon type={toast.type} />
            {toast.title && <div className={styles.title}>{toast.title}</div>}
            <div className={styles.body}>{i18n.translate(toast.message)}</div>
            <button
                className={styles.closeButton}
                type="button"
                aria-label={closeLabel}
                onClick={() => onClose(toast.id)}
            >
                <CloseIcon />
            </button>
        </div>
    );
};

const Notification = () => {
    const [toasts, setToasts] = useAtom(toastsAtom);
    const handleClose = React.useCallback(
        (id: string) => setToasts((currentToasts) => currentToasts.filter((toast) => toast.id !== id)),
        [setToasts]
    );

    if (toasts.length === 0) {
        return null;
    }

    return (
        <div className={styles.toasts} aria-live="polite">
            {toasts.map((toast) => (
                <Toast key={toast.id} toast={toast} onClose={handleClose} />
            ))}
        </div>
    );
};

export const warning = (props: NotificationPayload) => {
    addToast('warning', props);
};

export const success = (props: NotificationPayload) => {
    addToast('success', props);
};

export const info = (props: NotificationPayload) => {
    addToast('info', props);
};

export const danger = (props: NotificationPayload) => {
    addToast('danger', props);
};

export default Notification;
