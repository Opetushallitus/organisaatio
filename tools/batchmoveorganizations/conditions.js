const moment = require('moment')

module.exports = [
  org => org.lakkautusPvm &&
    moment(org.lakkautusPvm).isBetween('1990-01-01', '2005-12-31', null, '[]'),
  org => Array.isArray(org.tyypit) && org.tyypit.includes('Oppilaitos'),
]
