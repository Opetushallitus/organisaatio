import Joi from 'joi';

export default Joi.object({
    nimi: Joi.object({ fi: Joi.string(), sv: Joi.string(), en: Joi.string() }).required(),
    ytunnus: Joi.string().allow(''),
    alkuPvm: Joi.date().required(),
    organisaatioTyypit: Joi.array().min(1).required(),
    kotipaikka: Joi.object({ label: Joi.string().required(), value: Joi.string().required() }).required(),
    muutKotipaikat: Joi.array(),
    maa: Joi.object({ label: Joi.string().required(), value: Joi.string().required() }).required(),
    kielet: Joi.array().min(1).required(),
});
