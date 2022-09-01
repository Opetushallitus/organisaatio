import React, { useEffect, useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import axios from 'axios';

import store from './store';
import { JotpaOrganization } from './JotpaOrganization';
import { fetchOrganization, OrganizationSchema } from '../organizationSlice';
import { KoodistoContext, Koodistos } from '../KoodistoContext';
import { Koodi } from '../types';
import { JotpaUser } from './JotpaUser';
import { JotpaWizardValidator } from './JotpaWizardValidator';

store.dispatch(fetchOrganization());

const koodistoNimiComparator = (a: Koodi, b: Koodi) => ((a.nimi.fi ?? 'xxx') > (b.nimi.fi ?? 'xxx') ? 1 : -1);

export function JotpaRekisterointi() {
    const [koodisto, setKoodisto] = useState<Koodistos>();
    useEffect(() => {
        async function fetchKoodisto() {
            const [
                { data: kunnat },
                { data: yritysmuodot },
                { data: organisaatiotyypit },
                { data: maat },
                { data: posti },
            ] = await Promise.all([
                axios.get<Koodi[]>('/api/koodisto/KUNTA/koodi?onlyValid=true'),
                axios.get<Koodi[]>('/api/koodisto/YRITYSMUOTO/koodi?onlyValid=true'),
                axios.get<Koodi[]>('/api/koodisto/ORGANISAATIOTYYPPI/koodi?onlyValid=true'),
                axios.get<Koodi[]>('/api/koodisto/MAAT_JA_VALTIOT_1/koodi?onlyValid=true'),
                axios.get<Koodi[]>('/api/koodisto/POSTI/koodi?onlyValid=true'),
            ]);
            kunnat.sort(koodistoNimiComparator);
            yritysmuodot.sort(koodistoNimiComparator);
            organisaatiotyypit.sort(koodistoNimiComparator);
            maat.sort(koodistoNimiComparator);
            const postinumerot = posti.map((p) => p.arvo);
            setKoodisto({
                kunnat,
                yritysmuodot,
                organisaatiotyypit,
                maat,
                posti,
                postinumerot,
            });
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
                    <Route
                        path="/paakayttaja"
                        element={
                            <JotpaWizardValidator
                                validate={[
                                    {
                                        slice: 'organization',
                                        schema: OrganizationSchema(
                                            koodisto.yritysmuodot,
                                            koodisto.kunnat,
                                            koodisto.postinumerot
                                        ),
                                        redirectPath: '/hakija/jotpa/aloitus',
                                    },
                                ]}
                            >
                                <JotpaUser />
                            </JotpaWizardValidator>
                        }
                    />
                </Routes>
            </Provider>
        </KoodistoContext.Provider>
    );
}
