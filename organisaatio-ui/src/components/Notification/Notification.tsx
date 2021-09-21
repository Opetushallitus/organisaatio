import React, { useContext } from 'react';
import ReactNotification, { store } from 'react-notifications-component';
import 'react-notifications-component/dist/theme.css';
import { LanguageContext } from '../../contexts/contexts';

const Notification = () => {
    return <ReactNotification />;
};
const Message = (props: { message: string }) => {
    const { i18n } = useContext(LanguageContext);
    const translated = i18n.translate(props.message);
    return <div>{translated}</div>;
};
export const warning = (
    message: string,
    title?: string,
    timeOut?: number,
    callback?: () => void,
    priority?: boolean
) => {
    store.addNotification({
        title: title,
        message: message,
        type: 'warning',
        insert: 'top',
        container: 'top-right',
        animationIn: ['animate__animated', 'animate__fadeIn'],
        animationOut: ['animate__animated', 'animate__fadeOut'],
        dismiss: {
            duration: timeOut || 5000,
        },
    });
};
export const success = (
    message: string,
    title?: string,
    timeOut?: number,
    callback?: () => void,
    priority?: boolean
) => {
    store.addNotification({
        title: title,
        message: <Message message={message} />,
        type: 'success',
        insert: 'top',
        container: 'top-right',
        animationIn: ['animate__animated', 'animate__fadeIn'],
        animationOut: ['animate__animated', 'animate__fadeOut'],
        dismiss: {
            duration: timeOut || 5000,
        },
    });
};
export const info = (message: string, title?: string, timeOut?: number, callback?: () => void, priority?: boolean) => {
    store.addNotification({
        title: title,
        message: <Message message={message} />,
        type: 'info',
        insert: 'top',
        container: 'top-right',
        animationIn: ['animate__animated', 'animate__fadeIn'],
        animationOut: ['animate__animated', 'animate__fadeOut'],
        dismiss: {
            duration: timeOut || 5000,
        },
    });
};
export default Notification;
