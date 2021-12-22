import React from 'react';
import ReactDOM from 'react-dom';
import App from './frontend/App';
import * as serviceWorker from './serviceWorker';
import axios from 'axios';
import Cookies from 'universal-cookie';

const cookies = new Cookies();

axios.interceptors.request.use(config => {
    const ophHeaders = {
        'Caller-Id': '1.2.246.562.10.00000000001.varda-rekisterointi',
        'CSRF': cookies.get('CSRF')
    };
    config.headers = { ...config.headers, ...ophHeaders };
    return config;
});

ReactDOM.render(<App />, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
