import { configureStore } from '@reduxjs/toolkit';
import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux';

import organizationReducer from '../organizationSlice';

const store = configureStore({
    reducer: {
        organization: organizationReducer,
    },
});

export default store;

export type JotpaRekisterointiState = ReturnType<typeof store.getState>;
export type JotpaRekisterointiDispatch = typeof store.dispatch;

export const useJotpaRekisterointiDispatch: () => JotpaRekisterointiDispatch = useDispatch;
export const useJotpaRekisterointiSelector: TypedUseSelectorHook<JotpaRekisterointiState> = useSelector;
