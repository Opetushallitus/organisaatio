import React from 'react';
import ReactNotification, { ReactNotificationOptions, store } from 'react-notifications-component';
import 'react-notifications-component/dist/theme.css';
import { useAtom } from 'jotai';
import { languageAtom } from '../../api/lokalisaatio';

const DEFAULT_TIMEOUT = 5000;
const MESSAGE_DEFAULTS: ReactNotificationOptions = {
    insert: 'top',
    container: 'top-right',
    animationIn: ['animate__animated', 'animate__fadeIn'],
    animationOut: ['animate__animated', 'animate__fadeOut'],
};
const messageInputs = (props: notification) => ({
    ...MESSAGE_DEFAULTS,
    title: props.title,
    message: <Message message={props.message} />,
    dismiss: {
        duration: props.timeOut || DEFAULT_TIMEOUT,
    },
});
const Notification = () => {
    return <ReactNotification />;
};
type notification = {
    message: string;
    title?: string;
    timeOut?: number;
};

const Message = (props: { message: string }) => {
    const [i18n] = useAtom(languageAtom);
    const translated = i18n.translate(props.message);
    return <div>{translated}</div>;
};

export const warning = (props: notification) => {
    store.addNotification({
        ...messageInputs(props),
        type: 'warning',
    });
};
export const success = (props: notification) => {
    store.addNotification({
        ...messageInputs(props),
        type: 'success',
    });
};
export const info = (props: notification) => {
    store.addNotification({
        ...messageInputs(props),
        type: 'info',
    });
};
export const danger = (props: notification) => {
    store.addNotification({
        ...messageInputs(props),
        type: 'danger',
    });
};

export default Notification;
