import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

import { Koodi, Language, Lokalisointi, Organisation, RekisterointiRequest } from './types';

type Koodisto = 'KUNTA' | 'YRITYSMUOTO' | 'ORGANISAATIOTYYPPI' | 'POSTI';

const languages: Language[] = ['fi', 'sv', 'en'];

function getCookieValue(name: string): string | undefined {
    const cookie = window.document.cookie
        .split(';')
        .map((cookie) => cookie.trim())
        .find((cookie) => cookie.startsWith(`${name}=`));
    if (!cookie) {
        return undefined;
    }
    const value = cookie.substring(name.length + 1);
    try {
        return decodeURIComponent(value);
    } catch {
        return value;
    }
}

export const rekisterointiApi = createApi({
    reducerPath: 'rekisterointiApi',
    tagTypes: ['Language'],
    baseQuery: fetchBaseQuery({
        prepareHeaders: (headers) => {
            headers.set('Caller-Id', '1.2.246.562.10.00000000001.varda-rekisterointi');
            const csrf = getCookieValue('CSRF');
            if (csrf !== undefined) {
                headers.set('CSRF', csrf);
            }
            return headers;
        },
    }),
    endpoints: (builder) => ({
        getLanguage: builder.query<Language, void>({
            query: () => ({
                url: '/api/lokalisointi/kieli',
                responseHandler: 'text',
            }),
            transformResponse: (response: string) => {
                const language = response.trim();
                return languages.includes(language as Language) ? (language as Language) : 'fi';
            },
            providesTags: ['Language'],
        }),
        getLocalization: builder.query<Lokalisointi, void>({
            query: () => ({
                url: '/api/lokalisointi',
                params: { category: 'jotpa-rekisterointi' },
            }),
        }),
        setLanguage: builder.mutation<void, Language>({
            query: (locale) => ({
                url: '/api/lokalisointi/kieli',
                method: 'PUT',
                params: { locale },
                responseHandler: 'text',
            }),
            transformResponse: () => undefined,
            invalidatesTags: ['Language'],
        }),
        getOrganisation: builder.query<Organisation, void>({
            query: () => '/hakija/api/organisaatiot',
        }),
        getKoodisto: builder.query<Koodi[], Koodisto>({
            query: (koodisto) => ({
                url: `/api/koodisto/${koodisto}/koodi`,
                params: { onlyValid: true },
            }),
        }),
        submitRegistration: builder.mutation<string, RekisterointiRequest>({
            query: (body) => ({
                url: '/hakija/api/rekisterointi',
                method: 'POST',
                body,
                responseHandler: 'text',
            }),
        }),
    }),
});

export const {
    useGetLanguageQuery,
    useGetLocalizationQuery,
    useSetLanguageMutation,
    useGetOrganisationQuery,
    useGetKoodistoQuery,
    useSubmitRegistrationMutation,
} = rekisterointiApi;
