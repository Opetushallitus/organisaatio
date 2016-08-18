const argv = require('minimist')(process.argv.slice(2))
const request = require('request')
const conditions = require('./conditions')

require('./validateArgs')(argv)
moveOrgs()

function moveOrgs() {
  getChildOrgs()
  .then(childOrgs => {
    if (Array.isArray(childOrgs) && childOrgs.length > 0) {
      console.log('Found children:')
      childOrgs.forEach(({ oid, nimi }) => { console.log({ oid, nimi }) })
    } else {
      console.log('No children found')
    }

    return Promise.all(moveOrgsPassingConditions(childOrgs))
  })
  .then(success => {
    console.log(success)
  })
  .catch(err => {
    console.error(err)
    console.error('Errors found')
  })
}

function getChildOrgs() {
  return new Promise((resolve, reject) => {
    request({
      method: 'GET',
      uri: `${argv.baseUri}/${argv.source}/children`,
      headers: {
        Cookie: argv.auth,
      },
      json: true,
    }, (error, response, body) => {
      if (error) {
        reject(error)
      } else {
        resolve(body)
      }
    })
  })
}

function moveOrgsPassingConditions(orgs) {
  let moves = orgs.reduce((acc, org) => {
    if (conditions.length === 0 || conditions.every(fn => fn(org))) {
      acc.push(argv.dry ? null : moveOrg(org.oid))
    }

    return acc
  }, [])

  console.log('**', 'About to move', moves.length, 'out of', orgs.length,
    'child organizations from', argv.source, 'to',
    argv.target, '**')

  return argv.dry ? [Promise.resolve('dry run, stopping')] : moves
}

let delay = 0
function moveOrg(orgOid) {
  const scheduledMove = scheduleMoveRequest(delay, orgOid)
  delay += argv.delay
  return scheduledMove
}

function scheduleMoveRequest(delay, orgOid) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      request({
        method: 'POST',
        uri: `${argv.baseUri}/v2/${orgOid}/organisaatiosuhde?merge=false`,
        body: argv.target,
        headers: {
          Cookie: argv.auth,
          ['Content-Type']: 'application/json',
        },
        followAllRedirects: true,
      }, (error, response, body) => {
        if (error) {
          console.error(`Unable to move ${orgOid}`)
          reject(error)
        } else {
          if (response.statusCode == 204) {
            resolve(`${response.statusCode}: successfully moved ${orgOid}`)
          } else {
            reject(`${response.statusCode}: problem moving ${orgOid}`)
          }
        }
      })
    }, delay)
  })
}
