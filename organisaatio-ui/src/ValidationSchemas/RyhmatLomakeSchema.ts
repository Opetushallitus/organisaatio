import Joi from 'joi';

export default Joi.object({
    nimiEn: Joi.string(),
    nimiFi: Joi.string(),
    nimiSv: Joi.string(),
    kuvaus2Fi: Joi.string(),
    kuvaus2Sv: Joi.string(),
    kuvaus2En: Joi.string(),
    ryhmatyypit: Joi.array().min(1).required(),
    kayttoryhmat: Joi.array().min(1).required(),
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
    .when(Joi.object({ kuvaus2Fi: Joi.string().required() }).unknown(), {
        then: Joi.object({ kuvaus2Sv: Joi.string().allow(''), kuvaus2En: Joi.string().allow('') }),
    })
    .when(Joi.object({ kuvaus2Sv: Joi.string().required() }).unknown(), {
        then: Joi.object({ kuvaus2Fi: Joi.string().allow(''), kuvaus2En: Joi.string().allow('') }),
    })
    .when(Joi.object({ kuvaus2En: Joi.string().required() }).unknown(), {
        then: Joi.object({ kuvausSv: Joi.string().allow(''), kuvaus2Fi: Joi.string().allow('') }),
    })
    .or('nimiFi', 'nimiSv', 'nimiEn')
    .or('kuvaus2Fi', 'kuvaus2Sv', 'kuvaus2En')
    .and('ryhmatyypit', 'kayttoryhmat');
