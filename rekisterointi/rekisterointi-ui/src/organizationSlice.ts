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

interface State {
    loading: boolean;
    initialOrganization?: Organization;
    yritysmuoto?: SelectOption;
    kotipaikka?: SelectOption;
    alkamisaika?: Date;
}

const initialState: State = {
    loading: true,
    initialOrganization: undefined,
    yritysmuoto: undefined,
    kotipaikka: undefined,
    alkamisaika: undefined,
};

const organizationSlice = createSlice({
    name: 'organization',
    initialState,
    reducers: {
        setYritysmuoto: (state, action: PayloadAction<SelectOption>) => {
            state.yritysmuoto = action.payload;
        },
        setKotipaikka: (state, action: PayloadAction<SelectOption>) => {
            state.kotipaikka = action.payload;
        },
        setAlkamisaika: (state, action: PayloadAction<Date>) => {
            state.alkamisaika = action.payload;
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

export const { setYritysmuoto, setKotipaikka, setAlkamisaika } = organizationSlice.actions;
