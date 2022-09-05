import React from 'react';

import { useJotpaRekisterointiSelector } from './store';

import styles from './jotpa.module.css';

export function JotpaYhteenveto() {
    const state = useJotpaRekisterointiSelector((state) => state);

    return <div>{JSON.stringify(state)}</div>;
}
