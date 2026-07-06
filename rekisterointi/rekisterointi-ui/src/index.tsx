import React from 'react';
import ReactDOM from 'react-dom/client';
import axios from 'axios';

import App from './App';

function getCookieValue(name: string): string | undefined {
    const cookie = window.document.cookie
        .split(';')
        .map((cookie) => cookie.trim())
        .find((cookie) => cookie.startsWith(`${name}=`));
    if (!cookie) {
        return undefined;
    }
    const value = cookie.substring(name.length + 1);
    try {
        return decodeURIComponent(value);
    } catch {
        return value;
    }
}

axios.interceptors.request.use((config) => {
    config.headers.set('Caller-Id', '1.2.246.562.10.00000000001.varda-rekisterointi', true);
    const csrf = getCookieValue('CSRF');
    if (csrf !== undefined) {
        config.headers.set('CSRF', csrf, true);
    }
    return config;
});

const root = ReactDOM.createRoot(document.getElementById('root')!);
root.render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);
