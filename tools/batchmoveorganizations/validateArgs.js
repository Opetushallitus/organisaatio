module.exports = argv => {
  if (!argv.hasOwnProperty('source') ||
      !argv.hasOwnProperty('target')) {
    die('Missing source or target parent org oid arg')
  }

  if (!argv.hasOwnProperty('host')) {
    die('Missing host arg')
  } else {
    const suffix = 'organisaatio-service/rest/organisaatio'
    argv.baseUri = `https://${argv.host}/${suffix}`
  }

  if (!argv.hasOwnProperty('auth')) {
    die('Missing authentication cookie arg')
  }

  if (!argv.hasOwnProperty('delay')) {
    die('Missing delay arg')
  }
}

function die(error) {
  console.error(error)
  console.log(`
Usage : 
  node move 
    --source=[oid of source parent]
    --target=[oid of target parent] 
    --host=[host of dev/qa/demo/prod env]
    --auth=[authentication cookie]
    --delay=[delayBetweenRequestsInMillis]
    --dry (dry run, do not move orgs)
  Set conditions for orgs to move in ./conditions.js.`)
  process.exit(1)
}
