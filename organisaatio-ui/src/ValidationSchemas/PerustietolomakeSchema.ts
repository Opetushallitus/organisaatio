import Joi from 'joi';
import JoiDate from '@joi/date';

const JoiExtended = Joi.extend(JoiDate);

const perustietoOptionSchemaRequired = JoiExtended.object({
    label: JoiExtended.string().required(),
    value: JoiExtended.string().required(),
    arvo: JoiExtended.string().required(),
    versio: JoiExtended.number().optional(),
    disabled: JoiExtended.boolean().optional(),
}).required();
const perustietoOptionSchemaOptional = Joi.object({
    label: JoiExtended.string().allow(''),
    value: JoiExtended.string().allow(''),
    arvo: JoiExtended.string().allow(''),
    versio: JoiExtended.number().optional(),
    disabled: JoiExtended.boolean().optional(),
});

export default Joi.object({
    nimi: JoiExtended.object({
        fi: JoiExtended.string(),
        sv: JoiExtended.string(),
        en: JoiExtended.string(),
    }).required(),
    ytunnus: JoiExtended.string().allow(''),
    alkuPvm: JoiExtended.date().format(['D.M.YYYY']).required(),
    organisaatioTyypit: JoiExtended.array()
        .items(JoiExtended.string())
        .has(JoiExtended.string().not('organisaatiotyyppi_09').required()),
    kotipaikka: perustietoOptionSchemaRequired,
    muutKotipaikat: JoiExtended.array(),
    maa: perustietoOptionSchemaRequired,
    kielet: JoiExtended.array().min(1).required(),
    oppilaitosTyyppiUri: perustietoOptionSchemaOptional,
    oppilaitosKoodi: JoiExtended.string().allow(''),
    muutOppilaitosTyyppiUris: JoiExtended.array().min(0),
    vuosiluokat: JoiExtended.array().min(0),
    lakkautusPvm: JoiExtended.date().format(['D.M.YYYY']).allow(''),
    varhaiskasvatuksenToimipaikkaTiedot: JoiExtended.optional(),
    piilotettu: JoiExtended.optional(),
});
