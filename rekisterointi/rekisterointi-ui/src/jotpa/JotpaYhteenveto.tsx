import React, { useEffect } from 'react';

import { useJotpaRekisterointiSelector } from './store';

import styles from './jotpa.module.css';

export function JotpaYhteenveto() {
    const state = useJotpaRekisterointiSelector((state) => state);

    useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

    return <div>{JSON.stringify(state)}</div>;
}
