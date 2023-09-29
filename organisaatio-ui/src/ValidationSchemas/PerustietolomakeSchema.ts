import JoiLess, { Root } from 'joi';
import JoiDate from '@joi/date';
import { ytunnusJoiValidator } from './YtunnusValidator';
import { ORGANIAATIOTYYPPI_KUNTA } from '../api/koodisto';

const Joi = JoiLess.extend(JoiDate) as Root;

const perustietoOptionSchemaRequired = Joi.object({
    label: Joi.string().required(),
    value: Joi.string().required(),
    arvo: Joi.string().required(),
})
    .required()
    .options({ allowUnknown: true });

const perustietoOptionSchemaOptional = Joi.object({
    label: Joi.string().allow(''),
    value: Joi.string().allow(''),
    arvo: Joi.string().allow(''),
}).options({ allowUnknown: true });

const virastoTunnus = Joi.string()
    .pattern(/^\d{6}.*$/)
    .allow('', null);

export default Joi.object({
    nimi: Joi.object({ fi: Joi.string(), sv: Joi.string(), en: Joi.string() }).optional(),
    ytunnus: Joi.custom(ytunnusJoiValidator),
    alkuPvm: Joi.date().format(['D.M.YYYY']).required(),
    organisaatioTyypit: Joi.array().items(Joi.string()).has(Joi.string().not(ORGANIAATIOTYYPPI_KUNTA).required()),
    kotipaikka: perustietoOptionSchemaRequired,
    muutKotipaikat: Joi.array(),
    maa: perustietoOptionSchemaRequired,
    kielet: Joi.array().min(1).required(),
    oppilaitosTyyppiUri: perustietoOptionSchemaOptional,
    oppilaitosKoodi: Joi.string().allow(''),
    muutOppilaitosTyyppiUris: Joi.array().min(0),
    vuosiluokat: Joi.array().min(0),
    lakkautusPvm: Joi.date().format(['D.M.YYYY']).allow(''),
    varhaiskasvatuksenToimipaikkaTiedot: Joi.optional(),
    piilotettu: Joi.optional(),
    yritysmuoto: Joi.optional(),
    virastoTunnus: virastoTunnus,
});
