import Joi from 'joi';

export default Joi.object({
    fi: Joi.string(),
    en: Joi.string(),
    sv: Joi.string(),
})
    .when(Joi.object({ fi: Joi.string().required() }).unknown(), {
        then: Joi.object({ sv: Joi.string().allow(''), en: Joi.string().allow('') }),
    })
    .when(Joi.object({ sv: Joi.string().required() }).unknown(), {
        then: Joi.object({ fi: Joi.string().allow(''), en: Joi.string().allow('') }),
    })
    .when(Joi.object({ en: Joi.string().required() }).unknown(), {
        then: Joi.object({ fi: Joi.string().allow(''), sv: Joi.string().allow('') }),
    })
    .or('fi', 'sv', 'en');
