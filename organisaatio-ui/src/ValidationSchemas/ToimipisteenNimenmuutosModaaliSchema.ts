import JoiLess from 'joi';
import JoiDate from '@joi/date';
import moment from 'moment';

const Joi = JoiLess.extend(JoiDate);

export default Joi.object({
    nimi: Joi.object({ fi: Joi.string(), sv: Joi.string(), en: Joi.string() }).required(),
    alkuPvm: Joi.when('muutostyyppi', {
        is: 'CREATE',
        then: Joi.date().greater(moment().subtract(1, 'days')).format(['D.M.YYYY']).required(),
        otherwise: Joi.optional(),
    }),
    muutostyyppi: Joi.string().allow('CREATE', 'EDIT').required(),
    oid: Joi.string().required(),
    editDisabled: Joi.boolean().optional(),
    createDisabled: Joi.boolean().optional(),
});
