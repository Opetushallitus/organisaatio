import Joi from 'joi';

export const postinumeroSchema = Joi.string().regex(/^\d{5}$/);

const kayntiOsoiteRequiredSchema = Joi.object({
    kayntiOsoite: Joi.string().required(),
    kayntiOsoitePostiNro: postinumeroSchema.required(),
    kayntiOsoiteToimipaikka: Joi.string().allow(''),
});

const allowedLanguageSchema = Joi.object({
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
});

const fiAltSchema = Joi.object({
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
        then: kayntiOsoiteRequiredSchema,
    }),
    sv: allowedLanguageSchema,
    en: allowedLanguageSchema,
    osoitteetOnEri: Joi.boolean(),
});

const svAltSchema = Joi.object({
    fi: allowedLanguageSchema,
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
        then: kayntiOsoiteRequiredSchema,
    }),
    en: allowedLanguageSchema,
    osoitteetOnEri: Joi.boolean(),
});

const enAltSchema = Joi.object({
    fi: allowedLanguageSchema,
    sv: allowedLanguageSchema,
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
});

export default Joi.alternatives().try(fiAltSchema, svAltSchema, enAltSchema);
