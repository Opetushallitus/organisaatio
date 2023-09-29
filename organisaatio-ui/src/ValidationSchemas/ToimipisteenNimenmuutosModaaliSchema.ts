import JoiLess, { Root } from 'joi';
import JoiDate from '@joi/date';

const Joi = JoiLess.extend(JoiDate) as Root;

export default Joi.object({
    nimi: Joi.object({ fi: Joi.string(), sv: Joi.string(), en: Joi.string() }).required(),
    alkuPvm: Joi.when('muutostyyppi', {
        is: 'CREATE',
        then: Joi.date().format(['D.M.YYYY']).required(),
        otherwise: Joi.optional(),
    }),
    muutostyyppi: Joi.string().allow('CREATE', 'EDIT').required(),
    oid: Joi.string().required(),
    foundAmatch: Joi.boolean().optional(),
});
