import React, { useEffect, useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import axios from 'axios';

import store from './store';
import { JotpaOrganization } from './JotpaOrganization';
import { fetchOrganization } from '../organizationSlice';
import { KoodistoContext, Koodistos } from '../KoodistoContext';
import { Koodi } from '../types';

store.dispatch(fetchOrganization());

export function JotpaRekisterointi() {
    const [koodisto, setKoodisto] = useState<Koodistos>();
    useEffect(() => {
        async function fetchKoodisto() {
            const [{ data: kunnat }, { data: yritysmuodot }, { data: organisaatiotyypit }, { data: maat }] =
                await Promise.all([
                    axios.get<Koodi[]>('/api/koodisto/KUNTA/koodi?onlyValid=true'),
                    axios.get<Koodi[]>('/api/koodisto/YRITYSMUOTO/koodi?onlyValid=true'),
                    axios.get<Koodi[]>('/api/koodisto/ORGANISAATIOTYYPPI/koodi?onlyValid=true'),
                    axios.get<Koodi[]>('/api/koodisto/MAAT_JA_VALTIOT_1/koodi?onlyValid=true'),
                ]);
            setKoodisto({ kunnat, yritysmuodot, organisaatiotyypit, maat });
        }

        void fetchKoodisto();
    }, []);
    if (!koodisto) {
        return <div></div>;
    }

    return (
        <KoodistoContext.Provider value={koodisto}>
            <Provider store={store}>
                <Routes>
                    <Route path="/aloitus" element={<JotpaOrganization />} />
                </Routes>
            </Provider>
        </KoodistoContext.Provider>
    );
}
