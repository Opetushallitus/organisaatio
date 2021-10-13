import Joi from 'joi';

export default Joi.object({
    nimiEn: Joi.string(),
    nimiFi: Joi.string(),
    nimiSv: Joi.string(),
})
    .when(Joi.object({ nimiFi: Joi.string().required() }).unknown(), {
        then: Joi.object({ nimiSv: Joi.string().allow(''), nimiEn: Joi.string().allow('') }),
    })
    .when(Joi.object({ nimiSv: Joi.string().required() }).unknown(), {
        then: Joi.object({ nimiFi: Joi.string().allow(''), nimiEn: Joi.string().allow('') }),
    })
    .when(Joi.object({ nimiEn: Joi.string().required() }).unknown(), {
        then: Joi.object({ nimiFi: Joi.string().allow(''), nimiSv: Joi.string().allow('') }),
    })
    .or('nimiFi', 'nimiSv', 'nimiEn');
