import React from 'react';
import {
    NOTIFICATION_CONTAINER,
    NOTIFICATION_INSERTION,
    ReactNotifications,
    Store,
} from 'react-notifications-component';
import 'react-notifications-component/dist/theme.css';
import { useAtom } from 'jotai';
import { languageAtom } from '../../api/lokalisaatio';

const DEFAULT_TIMEOUT = 5000;

const messageInputs = (props: notification) => ({
    insert: 'top' as NOTIFICATION_INSERTION,
    container: 'top-right' as NOTIFICATION_CONTAINER,
    animationIn: ['animate__animated', 'animate__fadeIn'],
    animationOut: ['animate__animated', 'animate__fadeOut'],
    title: props.title,
    message: <Message message={props.message} />,
    dismiss: {
        duration: props.timeOut || DEFAULT_TIMEOUT,
    },
});
const Notification = () => {
    return <ReactNotifications />;
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
    Store.addNotification({
        ...messageInputs(props),
        type: 'warning',
    });
};
export const success = (props: notification) => {
    Store.addNotification({
        ...messageInputs(props),
        type: 'success',
    });
};
export const info = (props: notification) => {
    Store.addNotification({
        ...messageInputs(props),
        type: 'info',
    });
};
export const danger = (props: notification) => {
    Store.addNotification({
        ...messageInputs(props),
        type: 'danger',
    });
};

export default Notification;
