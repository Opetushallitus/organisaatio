import { configureStore } from '@reduxjs/toolkit';
import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux';

import organisationReducer from '../organisationSlice';
import userReducer from '../userSlice';

const store = configureStore({
    reducer: {
        organisation: organisationReducer,
        user: userReducer,
    },
});

export default store;

export type JotpaRekisterointiState = ReturnType<typeof store.getState>;
export type JotpaRekisterointiDispatch = typeof store.dispatch;

export const useJotpaRekisterointiDispatch: () => JotpaRekisterointiDispatch = useDispatch;
export const useJotpaRekisterointiSelector: TypedUseSelectorHook<JotpaRekisterointiState> = useSelector;
