import * as yup from 'yup';

import { Koodi, Language } from './types';

export const KoodiSchema = (koodit: Koodi[], language: Language) =>
    yup
        .object()
        .shape({
            label: yup
                .string()
                .required()
                .oneOf(
                    koodit.map((k) => k.nimi[language] ?? k.nimi.fi),
                    'Väärä arvo'
                ),
            value: yup
                .string()
                .required()
                .oneOf(
                    koodit.map((k) => k.uri),
                    'Väärä arvo'
                ),
        })
        .required();

export const PuhelinnumeroSchema = yup
    .string()
    .matches(/^(\+|-| |\(|\)|[0-9]){3,100}$/, 'Puhelinnumeron muoto on väärä')
    .required();

export const PostinumeroSchema = (postinumerot: string[]) =>
    yup.string().oneOf(postinumerot, 'Postinumerolle ei löydy postitoimipaikkaa');

export const EmailSchema = yup.string().email('Sähköpostin muoto on väärä').required('Pakollinen tieto');

export const EmailArraySchema = yup
    .array()
    .min(1, 'Syötä vähintään yksi sähköposti')
    .of(
        yup.object().shape({
            email: yup.string().email('Sähköpostin muoto on väärä').required('Pakollinen tieto'),
        })
    );

export const PostiosoiteSchema = yup.string().min(3).max(100);
