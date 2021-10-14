import Joi from 'joi';

export default Joi.object({
    nimi: Joi.object({ fi: Joi.string(), sv: Joi.string(), en: Joi.string() }).required(),
    ytunnus: Joi.string(),
    alkuPvm: Joi.date().required(),
    tyypit: Joi.array().min(1).required(),
    kotipaikkaUri: Joi.object({ label: Joi.string().required(), value: Joi.string().required() }).required(),
    muutKotipaikatUris: Joi.array(),
    maaUri: Joi.object({ label: Joi.string().required(), value: Joi.string().required() }).required(),
    kieletUris: Joi.array().min(1).required(),
});
