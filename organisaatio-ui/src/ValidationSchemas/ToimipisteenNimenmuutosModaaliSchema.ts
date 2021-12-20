import Joi from 'joi';

export default Joi.object({
    nimi: Joi.object({ fi: Joi.string(), sv: Joi.string(), en: Joi.string() }).required(),
    alkuPvm: Joi.when('muutostyyppi', {
        is: 'CREATE',
        then: Joi.date().required(),
        otherwise: Joi.optional(),
    }),
    muutostyyppi: Joi.string().allow('CREATE', 'EDIT').required(),
    oid: Joi.string().required(),
    editDisabled: Joi.boolean().optional(),
});
