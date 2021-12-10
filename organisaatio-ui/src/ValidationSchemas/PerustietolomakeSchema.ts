import Joi from 'joi';

const perustietoOptionSchema = Joi.object({
    label: Joi.string().required(),
    value: Joi.string().required(),
    disabled: Joi.boolean().optional(),
}).required();

export default Joi.object({
    nimi: Joi.object({ fi: Joi.string(), sv: Joi.string(), en: Joi.string() }).required(),
    ytunnus: Joi.string().allow(''),
    alkuPvm: Joi.date().required(),
    organisaatioTyypit: Joi.array().items(Joi.string()).has(Joi.string().not('organisaatiotyyppi_09').required()),
    kotipaikka: perustietoOptionSchema,
    muutKotipaikat: Joi.array(),
    maa: perustietoOptionSchema,
    kielet: Joi.array().min(1).required(),
    oppilaitosTyyppiUri: Joi.object({
        label: Joi.string().allow(''),
        value: Joi.string().allow(''),
        disabled: Joi.boolean().optional(),
    }),
    oppilaitosKoodi: Joi.string().allow(''),
    muutOppilaitosTyyppiUris: Joi.array().min(0),
    vuosiluokat: Joi.array().min(0),
    lakkautusPvm: Joi.date().allow(''),
});
