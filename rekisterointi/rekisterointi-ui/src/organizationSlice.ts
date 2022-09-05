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
import { Koodi, Language, Organization, SelectOption } from './types';

export const fetchOrganization = createAsyncThunk<Organization, void>(
    'organization/fetchOrganization',
    async (_, thunkAPI) => {
        const resp = await axios.get<Organization>('/hakija/api/organisaatiot');
        return resp.data;
    }
);

export interface OrganizationFormState {
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
    initialOrganization?: Organization;
    form?: OrganizationFormState;
}

const initialState: State = {
    loading: true,
};

const organizationSlice = createSlice({
    name: 'organization',
    initialState,
    reducers: {
        setForm: (state, action: PayloadAction<OrganizationFormState>) => {
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

export const OrganizationSchema = (
    yritysmuodot: Koodi[],
    kunnat: Koodi[],
    postinumerot: string[],
    language: Language
): yup.SchemaOf<OrganizationFormState> =>
    yup.object().shape({
        yritysmuoto: KoodiSchema(yritysmuodot, language),
        kotipaikka: KoodiSchema(kunnat, language),
        alkamisaika: DateStringSchema,
        puhelinnumero: PuhelinnumeroSchema,
        email: EmailSchema,
        emails: EmailArraySchema,
        postiosoite: PostiosoiteSchema.required(),
        postinumero: PostinumeroSchema(postinumerot).required(),
        copyKayntiosoite: yup.bool().required(),
        kayntiosoite: yup
            .string()
            .when(['copyKayntiosoite'], (copyKayntiosoite, schema) =>
                copyKayntiosoite ? schema.optional() : PostiosoiteSchema.required()
            ),
        kayntipostinumero: yup
            .string()
            .when(['copyKayntiosoite'], (copyKayntiosoite, schema) =>
                copyKayntiosoite ? schema.optional() : PostinumeroSchema(postinumerot).required()
            ),
    });
