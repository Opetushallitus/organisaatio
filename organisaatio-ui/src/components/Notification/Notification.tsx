import React, { useContext } from 'react';
import ReactNotification, { store } from 'react-notifications-component';
import 'react-notifications-component/dist/theme.css';
import { LanguageContext } from '../../contexts/contexts';
const DEFAULT_TIMEOUT = 5000;
const MESSAGE_DEFAULTS = {
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
    const { i18n } = useContext(LanguageContext);
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
