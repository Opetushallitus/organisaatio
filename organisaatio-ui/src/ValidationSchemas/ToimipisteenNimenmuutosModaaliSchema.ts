import Joi from 'joi';

export default Joi.object({
    fi: Joi.string().required(),
    en: Joi.string().required(),
    sv: Joi.string().required(),
    muutostyyppi: Joi.string().allow('CREATE', 'EDIT', 'CANCEL'),
});
