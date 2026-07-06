import { createSlice, PayloadAction } from '@reduxjs/toolkit';
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
    initialOrganisation?: Organisation;
    form?: OrganisationFormState;
}

const initialState: State = {};

const organisationSlice = createSlice({
    name: 'organisation',
    initialState,
    reducers: {
        setForm: (state, action: PayloadAction<OrganisationFormState>) => {
            state.form = action.payload;
        },
        setInitialOrganisation: (state, action: PayloadAction<Organisation>) => {
            state.initialOrganisation = action.payload;
        },
    },
});

export default organisationSlice.reducer;

export const { setForm, setInitialOrganisation } = organisationSlice.actions;

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
