import { configureStore } from '@reduxjs/toolkit';
import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux';

import organisationReducer from '../organisationSlice';
import userReducer from '../userSlice';
import { rekisterointiApi } from '../rekisterointiApi';

const store = configureStore({
    reducer: {
        [rekisterointiApi.reducerPath]: rekisterointiApi.reducer,
        organisation: organisationReducer,
        user: userReducer,
    },
    middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(rekisterointiApi.middleware),
});

export default store;

export type JotpaRekisterointiState = ReturnType<typeof store.getState>;
export type JotpaRekisterointiDispatch = typeof store.dispatch;

export const useJotpaRekisterointiDispatch: () => JotpaRekisterointiDispatch = useDispatch;
export const useJotpaRekisterointiSelector: TypedUseSelectorHook<JotpaRekisterointiState> = useSelector;
