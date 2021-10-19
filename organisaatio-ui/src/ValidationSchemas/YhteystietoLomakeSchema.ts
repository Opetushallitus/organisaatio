import Joi from 'joi';

export const postinumeroSchema = Joi.string().regex(/^\d{5}$/);

export default Joi.object({
    'kieli_fi#1': Joi.object({
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
    }),
    'kieli_sv#1': Joi.object({
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
    'kieli_en#1': Joi.object({
        postiOsoite: Joi.string().allow(''),
        postiOsoitePostiNro: postinumeroSchema.allow(''),
        postiOsoiteToimipaikka: Joi.string().allow(''),
        kayntiOsoite: Joi.string().allow(''),
        puhelinnumero: Joi.string().allow(''),
        kayntiOsoitePostiNro: postinumeroSchema.allow(''),
        kayntiOsoiteToimipaikka: Joi.string().allow(''),
        email: Joi.string()
            .email({ tlds: { allow: false } })
            .allow(''),
        www: Joi.string().allow(''),
    }),
    osoitteetOnEri: Joi.boolean(),
});
