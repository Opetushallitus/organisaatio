import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import axios from 'axios';
import * as yup from 'yup';

import {
    DateStringSchema,
    EmailArraySchema,
    EmailSchema,
    KoodiSchema,
    PostinumeroSchema,
    PostiosoiteSchema,
    PuhelinnumeroSchema,
} from './yupSchemas';
import { Koodi, Organisation, SelectOption } from './types';

export const fetchOrganisation = createAsyncThunk<Organisation, void>(
    'organisation/fetchOrganisation',
    async () => {
        const resp = await axios.get<Organisation>('/hakija/api/organisaatiot');
        return resp.data;
    }
);

export interface OrganisationFormState {
    yritysmuoto: SelectOption;
    kotipaikka: SelectOption;
    alkamisaika: string;
    puhelinnumero: string;
    email: string;
    postiosoite: string;
    postinumero: string;
    copyKayntiosoite: boolean;
    kayntiosoite?: string;
    kayntipostinumero?: string;
    emails: { email?: string }[];
}

interface State {
    loading: boolean;
    initialOrganisation?: Organisation;
    form?: OrganisationFormState;
}

const initialState: State = {
    loading: true,
};

const organisationSlice = createSlice({
    name: 'organisation',
    initialState,
    reducers: {
        setForm: (state, action: PayloadAction<OrganisationFormState>) => {
            state.form = action.payload;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchOrganisation.pending, (state) => {
                state.loading = true;
            })
            .addCase(fetchOrganisation.fulfilled, (state, action) => {
                state.loading = false;
                state.initialOrganisation = action.payload;
            })
            .addCase(fetchOrganisation.rejected, () => {
                window.location.href = '/hakija/logout?redirect=/jotpa'
            });
    },
});

export default organisationSlice.reducer;

export const { setForm } = organisationSlice.actions;

export const OrganisationSchema = (
    yritysmuodot: Koodi[],
    kunnat: Koodi[],
    postinumerot: string[]
): yup.ObjectSchema<OrganisationFormState> =>
    yup.object().shape({
        yritysmuoto: KoodiSchema(yritysmuodot),
        kotipaikka: KoodiSchema(kunnat),
        alkamisaika: DateStringSchema,
        puhelinnumero: PuhelinnumeroSchema,
        email: EmailSchema,
        emails: EmailArraySchema,
        postiosoite: PostiosoiteSchema.required(),
        postinumero: PostinumeroSchema(postinumerot).required(),
        copyKayntiosoite: yup.bool().required(),
        kayntiosoite: yup
            .string()
            .when('copyKayntiosoite', ([copyKayntiosoite], schema) =>
                copyKayntiosoite ? schema.optional() : PostiosoiteSchema.required()
            ),
        kayntipostinumero: yup
            .string()
            .when('copyKayntiosoite', ([copyKayntiosoite], schema) =>
                copyKayntiosoite ? schema.optional() : PostinumeroSchema(postinumerot).required()
            ),
    });
