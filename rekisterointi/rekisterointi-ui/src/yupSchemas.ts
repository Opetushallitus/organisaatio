import * as yup from 'yup';

import { Koodi } from './types';

export const KoodiSchema = (koodit: Koodi[]) =>
    yup
        .object()
        .shape({
            label: yup
                .string()
                .required()
                .oneOf(
                    koodit.map((k) => k.nimi.fi),
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

export const PostinumeroSchema = yup.string().matches(/^\d{5}$/, 'Postinumeron muoto on väärä');

const emailRegex = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;

export const EmailSchema = yup.string().email().required();

export const EmailArraySchema = yup
    .array()
    .min(1)
    .of(
        yup.object().shape({
            email: yup.string().test('invalid_email', 'Sähköpostin muoto on väärä', (e) => !e || emailRegex.test(e)),
        })
    );

export const PostiosoiteSchema = yup.string().min(3).max(100);
