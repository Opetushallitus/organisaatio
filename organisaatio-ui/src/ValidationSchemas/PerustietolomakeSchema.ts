import Joi from 'joi';

const perustietoOptionSchemaRequired = Joi.object({
    label: Joi.string().required(),
    value: Joi.string().required(),
    arvo: Joi.string().required(),
    versio: Joi.number().optional(),
    disabled: Joi.boolean().optional(),
}).required();
const perustietoOptionSchemaOptional = Joi.object({
    label: Joi.string().allow(''),
    value: Joi.string().allow(''),
    arvo: Joi.string().allow(''),
    versio: Joi.number().optional(),
    disabled: Joi.boolean().optional(),
});

export default Joi.object({
    nimi: Joi.object({ fi: Joi.string(), sv: Joi.string(), en: Joi.string() }).required(),
    ytunnus: Joi.string().allow(''),
    alkuPvm: Joi.date().required(),
    organisaatioTyypit: Joi.array().items(Joi.string()).has(Joi.string().not('organisaatiotyyppi_09').required()),
    kotipaikka: perustietoOptionSchemaRequired,
    muutKotipaikat: Joi.array(),
    maa: perustietoOptionSchemaRequired,
    kielet: Joi.array().min(1).required(),
    oppilaitosTyyppiUri: perustietoOptionSchemaOptional,
    oppilaitosKoodi: Joi.string().allow(''),
    muutOppilaitosTyyppiUris: Joi.array().min(0),
    vuosiluokat: Joi.array().min(0),
    lakkautusPvm: Joi.date().allow(''),
});
