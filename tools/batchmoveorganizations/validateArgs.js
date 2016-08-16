module.exports = argv => {
  if (!argv.hasOwnProperty('sourceParentOid') ||
      !argv.hasOwnProperty('targetParentOid')) {
    die('Missing source or target parent org oid arg')
  }

  if (!argv.hasOwnProperty('authCookie')) {
    die('Missing authentication cookie arg')
  }

  if (!argv.hasOwnProperty('delayBetweenRequests')) {
    die('Missing delay arg')
  }

  if (argv.hasOwnProperty('env') &&
      ['dev', 'qa', 'prod'].includes(argv.env)) {
    const suffix = 'organisaatio-service/rest/organisaatio'
    switch (argv.env) {
      case 'dev':
        if (process.env.DEV_HOST) {
          argv.baseUri = `https://${process.env.DEV_HOST}/${suffix}`
        } else {
          die('Missing env var DEV_HOST')
        }

        break
      case 'qa':
        if (process.env.QA_HOST) {
          argv.baseUri = `https://${process.env.QA_HOST}/${suffix}`
        } else {
          die('Missing env var QA_HOST')
        }

        break
      case 'prod':
        if (process.env.PROD_HOST) {
          argv.baseUri = `https://${process.env.PROD_HOST}/${suffix}`
        } else {
          die('Missing env var PROD_HOST')
        }

        break
    }
  } else {
    console.error('Incorrect env arg')
    die()
  }
}

function die(error) {
  console.error(error)
  console.log('\nUsage: node move --sourceParentOid=[oid] ' +
    '--targetParentOid=[oid] --env=[dev|qa|prod] --authCookie=[xxx] ' +
    '--delayBetweenRequests=[delayInMillis]')
  console.log('Usage: Set conditions for orgs to move in ./conditions.js')
  process.exit(1)
}
