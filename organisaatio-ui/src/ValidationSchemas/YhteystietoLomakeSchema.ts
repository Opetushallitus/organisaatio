import Joi from 'joi';

export const postinumeroSchema = Joi.string().regex(/^\d{5}$/);

export default Joi.alternatives().try(
    Joi.object({
        fi: Joi.object({
            postiOsoite: Joi.string().required(),
            postiOsoitePostiNro: postinumeroSchema.required(),
            postiOsoiteToimipaikka: Joi.string().allow(''),
            kayntiOsoite: Joi.string().allow(''),
            kayntiOsoitePostiNro: postinumeroSchema.allow(''),
            kayntiOsoiteToimipaikka: Joi.string().allow(''),
            puhelinnumero: Joi.string().allow(''),
            email: Joi.string()
                .email({ tlds: { allow: false } })
                .required(),
            www: Joi.string().allow(''),
        }).when('osoitteetOnEri', {
            is: true,
            then: Joi.object({
                kayntiOsoite: Joi.string().required(),
                kayntiOsoitePostiNro: postinumeroSchema.required(),
                kayntiOsoiteToimipaikka: Joi.string().allow(''),
            }),
        }),
        sv: Joi.object({
            postiOsoite: Joi.string().allow(''),
            postiOsoitePostiNro: postinumeroSchema.allow(''),
            postiOsoiteToimipaikka: Joi.string().allow(''),
            kayntiOsoite: Joi.string().allow(''),
            kayntiOsoitePostiNro: postinumeroSchema.allow(''),
            kayntiOsoiteToimipaikka: Joi.string().allow(''),
            puhelinnumero: Joi.string().allow(''),
            email: Joi.string()
                .email({ tlds: { allow: false } })
                .allow(''),
            www: Joi.string().allow(''),
        }),
        en: Joi.object({
            postiOsoite: Joi.string().allow(''),
            postiOsoitePostiNro: postinumeroSchema.allow(''),
            postiOsoiteToimipaikka: Joi.string().allow(''),
            kayntiOsoite: Joi.string().allow(''),
            kayntiOsoitePostiNro: postinumeroSchema.allow(''),
            kayntiOsoiteToimipaikka: Joi.string().allow(''),
            puhelinnumero: Joi.string().allow(''),
            email: Joi.string()
                .email({ tlds: { allow: false } })
                .allow(''),
            www: Joi.string().allow(''),
        }),
        osoitteetOnEri: Joi.boolean(),
    }),
    Joi.object({
        fi: Joi.object({
            postiOsoite: Joi.string().allow(''),
            postiOsoitePostiNro: postinumeroSchema.allow(''),
            postiOsoiteToimipaikka: Joi.string().allow(''),
            kayntiOsoite: Joi.string().allow(''),
            kayntiOsoitePostiNro: postinumeroSchema.allow(''),
            kayntiOsoiteToimipaikka: Joi.string().allow(''),
            puhelinnumero: Joi.string().allow(''),
            email: Joi.string()
                .email({ tlds: { allow: false } })
                .allow(''),
            www: Joi.string().allow(''),
        }),
        sv: Joi.object({
            postiOsoite: Joi.string().required(),
            postiOsoitePostiNro: postinumeroSchema.required(),
            postiOsoiteToimipaikka: Joi.string().allow(''),
            kayntiOsoite: Joi.string().allow(''),
            kayntiOsoitePostiNro: postinumeroSchema.allow(''),
            kayntiOsoiteToimipaikka: Joi.string().allow(''),
            puhelinnumero: Joi.string().allow(''),
            email: Joi.string()
                .email({ tlds: { allow: false } })
                .required(),
            www: Joi.string().allow(''),
        }).when('osoitteetOnEri', {
            is: true,
            then: Joi.object({
                kayntiOsoite: Joi.string().required(),
                kayntiOsoitePostiNro: postinumeroSchema.required(),
                kayntiOsoiteToimipaikka: Joi.string().allow(''),
            }),
        }),
        en: Joi.object({
            postiOsoite: Joi.string().allow(''),
            postiOsoitePostiNro: postinumeroSchema.allow(''),
            postiOsoiteToimipaikka: Joi.string().allow(''),
            kayntiOsoite: Joi.string().allow(''),
            kayntiOsoitePostiNro: postinumeroSchema.allow(''),
            kayntiOsoiteToimipaikka: Joi.string().allow(''),
            puhelinnumero: Joi.string().allow(''),
            email: Joi.string()
                .email({ tlds: { allow: false } })
                .allow(''),
            www: Joi.string().allow(''),
        }),
        osoitteetOnEri: Joi.boolean(),
    }),
    Joi.object({
        fi: Joi.object({
            postiOsoite: Joi.string().allow(''),
            postiOsoitePostiNro: postinumeroSchema.allow(''),
            postiOsoiteToimipaikka: Joi.string().allow(''),
            kayntiOsoite: Joi.string().allow(''),
            kayntiOsoitePostiNro: postinumeroSchema.allow(''),
            kayntiOsoiteToimipaikka: Joi.string().allow(''),
            puhelinnumero: Joi.string().allow(''),
            email: Joi.string()
                .email({ tlds: { allow: false } })
                .allow(''),
            www: Joi.string().allow(''),
        }),
        sv: Joi.object({
            postiOsoite: Joi.string().allow(''),
            postiOsoitePostiNro: postinumeroSchema.allow(''),
            postiOsoiteToimipaikka: Joi.string().allow(''),
            kayntiOsoite: Joi.string().allow(''),
            kayntiOsoitePostiNro: postinumeroSchema.allow(''),
            kayntiOsoiteToimipaikka: Joi.string().allow(''),
            puhelinnumero: Joi.string().allow(''),
            email: Joi.string()
                .email({ tlds: { allow: false } })
                .allow(''),
            www: Joi.string().allow(''),
        }),
        en: Joi.object({
            postiOsoite: Joi.string().required(),
            postiOsoitePostiNro: Joi.string().allow(''),
            postiOsoiteToimipaikka: Joi.string().allow(''),
            kayntiOsoite: Joi.string().allow(''),
            kayntiOsoitePostiNro: postinumeroSchema.allow(''),
            kayntiOsoiteToimipaikka: Joi.string().allow(''),
            puhelinnumero: Joi.string().allow(''),
            email: Joi.string()
                .email({ tlds: { allow: false } })
                .required(),
            www: Joi.string().allow(''),
        }),
        osoitteetOnEri: Joi.boolean(),
    })
);
