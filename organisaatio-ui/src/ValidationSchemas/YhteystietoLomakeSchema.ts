import Joi from 'joi';

export default Joi.object({
    'kieli_fi#1': Joi.object({
        postiOsoite: Joi.string().required(),
        postiOsoitePostiNro: Joi.string().regex(/^\d+$/).required(),
        postiOsoiteToimipaikka: Joi.string().allow(''),
        kayntiOsoite: Joi.string().allow(''),
        kayntiOsoitePostiNro: Joi.string().regex(/^\d+$/).allow(''),
        kayntiOsoiteToimipaikka: Joi.string().allow(''),
        puhelinnumero: Joi.string().allow(''),
        email: Joi.string()
            .email({ tlds: { allow: false } })
            .required(),
        www: Joi.string().allow(''),
    }),
    'kieli_sv#1': Joi.object({
        postiOsoite: Joi.string().allow(''),
        postiOsoitePostiNro: Joi.string().regex(/^\d+$/).allow(''),
        postiOsoiteToimipaikka: Joi.string().allow(''),
        kayntiOsoite: Joi.string().allow(''),
        kayntiOsoitePostiNro: Joi.string().regex(/^\d+$/).allow(''),
        kayntiOsoiteToimipaikka: Joi.string().allow(''),
        puhelinnumero: Joi.string().allow(''),
        email: Joi.string()
            .email({ tlds: { allow: false } })
            .allow(''),
        www: Joi.string().allow(''),
    }),
    'kieli_en#1': Joi.object({
        postiOsoite: Joi.string().allow(''),
        postiOsoitePostiNro: Joi.string().regex(/^\d+$/).allow(''),
        postiOsoiteToimipaikka: Joi.string().allow(''),
        kayntiOsoite: Joi.string().allow(''),
        puhelinnumero: Joi.string().allow(''),
        kayntiOsoitePostiNro: Joi.string().regex(/^\d+$/).allow(''),
        kayntiOsoiteToimipaikka: Joi.string().allow(''),
        email: Joi.string()
            .email({ tlds: { allow: false } })
            .allow(''),
        www: Joi.string().allow(''),
    }),
    osoitteetOnEri: Joi.boolean(),
});
