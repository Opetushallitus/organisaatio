import * as yup from 'yup';

import { Koodi } from './types';

export const KoodiSchema = (koodit: Koodi[]) =>
    yup
        .object()
        .shape({
            label: yup.string().required('validaatio_pakollinen'),
            value: yup
                .string()
                .required('validaatio_pakollinen')
                .oneOf(
                    koodit.map((k) => k.uri),
                    'validaatio_geneerinen'
                ),
        })
        .required('validaatio_pakollinen');

export const PuhelinnumeroSchema = yup
    .string()
    .required('validaatio_pakollinen')
    .matches(/^(\+|-| |\(|\)|[0-9]){3,100}$/, 'validaatio_geneerinen');

export const PostinumeroSchema = (postinumerot: string[]) => yup.string().oneOf(postinumerot, 'validaatio_postinumero');

export const EmailSchema = yup.string().email('validaatio_email').required('validaatio_pakollinen');

export const EmailArraySchema = yup
    .array()
    .required()
    .min(1, 'validaatio_email')
    .of(
        yup.object().shape({
            email: yup.string().email('validaatio_email').required('validaatio_pakollinen'),
        })
    );

export const PostiosoiteSchema = yup.string();

export const DateStringSchema = yup
    .string()
    .nullable()
    .required('validaatio_pakollinen')
    .matches(/^[0-9]{1,2}\.[0-9]{1,2}\.[0-9]{4}/, 'validaatio_geneerinen');
