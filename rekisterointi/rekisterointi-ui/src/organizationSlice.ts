import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import axios from 'axios';

import { Organization, SelectOption } from './types';

export const fetchOrganization = createAsyncThunk<Organization, void>(
    'organization/fetchOrganization',
    async (_, thunkAPI) => {
        const resp = await axios.get<Organization>('/hakija/api/organisaatiot');
        return resp.data;
    }
);

export interface FormState {
    yritysmuoto: SelectOption;
    kotipaikka: SelectOption;
    alkamisaika: Date;
    puhelinnumero: string;
    email: string;
    postiosoite: string;
    postinumero: string;
    postitoimipaikka: string;
    kayntiosoite: string;
    kayntipostinumero: string;
    kayntipostitoimipaikka: string;
    emails: { email: string }[];
}

interface State {
    loading: boolean;
    initialOrganization?: Organization;
    form?: FormState;
}

const initialState: State = {
    loading: true,
};

const organizationSlice = createSlice({
    name: 'organization',
    initialState,
    reducers: {
        setForm: (state, action: PayloadAction<FormState>) => {
            state.form = action.payload;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchOrganization.pending, (state) => {
                state.loading = true;
            })
            .addCase(fetchOrganization.fulfilled, (state, action) => {
                state.loading = false;
                state.initialOrganization = action.payload;
            });
    },
});

export default organizationSlice.reducer;

export const { setForm } = organizationSlice.actions;
