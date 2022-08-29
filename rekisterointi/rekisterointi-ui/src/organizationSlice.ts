import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import axios from 'axios';

import { Organization } from './types';

export const fetchOrganization = createAsyncThunk<Organization, void>(
    'organization/fetchOrganization',
    async (_, thunkAPI) => {
        const resp = await axios.get<Organization>('/hakija/api/organisaatiot');
        return resp.data;
    }
);

interface State {
    loading: boolean;
    organization?: Organization;
}

const initialState: State = {
    loading: true,
    organization: undefined,
};

const organizationSlice = createSlice({
    name: 'organization',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(fetchOrganization.pending, (state) => {
                state.loading = true;
            })
            .addCase(fetchOrganization.fulfilled, (state, action) => {
                state.loading = false;
                state.organization = action.payload;
            });
    },
});

export default organizationSlice.reducer;
