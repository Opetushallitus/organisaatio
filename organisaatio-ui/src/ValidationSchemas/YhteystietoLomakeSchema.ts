import Joi from 'joi';

export const postinumeroSchema = Joi.string().regex(/^\d{5}$/);

const kayntiOsoiteRequiredSchema = Joi.object({
    kayntiOsoite: Joi.string().required(),
    kayntiOsoitePostiNro: postinumeroSchema.required(),
    kayntiOsoiteToimipaikka: Joi.string().allow(''),
});

const allAllowedLanguageObject = {
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
};

const requiredFieldsObject = {
    postiOsoite: Joi.string().required(),
    postiOsoitePostiNro: postinumeroSchema.required(),
    email: Joi.string()
        .email({ tlds: { allow: false } })
        .required(),
};

const fiAltSchema = Joi.object({
    fi: Joi.object({ ...allAllowedLanguageObject, ...requiredFieldsObject }).when('osoitteetOnEri', {
        is: true,
        then: kayntiOsoiteRequiredSchema,
    }),
    sv: Joi.object(allAllowedLanguageObject),
    en: Joi.object(allAllowedLanguageObject),
    osoitteetOnEri: Joi.boolean(),
});

const svAltSchema = Joi.object({
    fi: Joi.object(allAllowedLanguageObject),
    sv: Joi.object({ ...allAllowedLanguageObject, ...requiredFieldsObject }).when('osoitteetOnEri', {
        is: true,
        then: kayntiOsoiteRequiredSchema,
    }),
    en: Joi.object(allAllowedLanguageObject),
    osoitteetOnEri: Joi.boolean(),
});

const enAltSchema = Joi.object({
    fi: Joi.object(allAllowedLanguageObject),
    sv: Joi.object(allAllowedLanguageObject),
    en: Joi.object({
        ...allAllowedLanguageObject,
        postiOsoite: Joi.string().required(),
        email: Joi.string()
            .email({ tlds: { allow: false } })
            .required(),
    }),
});

export default Joi.alternatives().try(fiAltSchema, svAltSchema, enAltSchema);
