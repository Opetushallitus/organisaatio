import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import * as yup from 'yup';

import { Language } from './types';
import { EmailSchema } from './yupSchemas';

export interface UserFormState {
    etunimi: string;
    sukunimi: string;
    email: string;
    asiointikieli: Language;
    info?: string;
}

interface State {
    form?: UserFormState;
}

const initialState: State = {};

const userSlice = createSlice({
    name: 'user',
    initialState,
    reducers: {
        setForm: (state, action: PayloadAction<UserFormState>) => {
            state.form = action.payload;
        },
    },
});

export default userSlice.reducer;

export const { setForm } = userSlice.actions;

export const UserSchema: yup.SchemaOf<UserFormState> = yup.object().shape({
    etunimi: yup.string().required('validaatio_pakollinen'),
    sukunimi: yup.string().required('validaatio_pakollinen'),
    email: EmailSchema,
    asiointikieli: yup.mixed().oneOf<Language>(['fi', 'sv']).required('validaatio_pakollinen'),
    info: yup.string().optional(),
});
