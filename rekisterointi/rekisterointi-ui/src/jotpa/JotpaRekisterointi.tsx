import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';

import store from './store';
import { JotpaOrganization } from './JotpaOrganization';
import { fetchOrganization } from '../organizationSlice';

export function JotpaRekisterointi() {
    store.dispatch(fetchOrganization());
    return (
        <Provider store={store}>
            <Routes>
                <Route path="/aloitus" element={<JotpaOrganization />} />
            </Routes>
        </Provider>
    );
}
