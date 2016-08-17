let argv = require('minimist')(process.argv.slice(2))
const request = require('request')
const conditions = require('./conditions')

require('./validateArgs')(argv)
moveOrgs()

function moveOrgs() {
  getChildOrgs(argv.sourceParentOid)
  .then(childOrgs => {
    if (Array.isArray(childOrgs) && childOrgs.length > 0) {
      console.log('Children:')
      childOrgs.forEach(({ oid, nimi }) => {
        console.log({ oid, nimi })
      })
    }

    return Promise.all(moveOrgsPassingConditions(childOrgs))
  })
  .then(success => {
    console.log('Done')
  })
  .catch(err => {
    console.error(err)
    console.error('Errors found')
  })
}

function getChildOrgs(sourceParentOid) {
  return new Promise((resolve, reject) => {
    request({
      method: 'GET',
      uri: `${argv.baseUri}/${argv.sourceParentOid}/children`,
      headers: {
        Cookie: argv.authCookie,
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
      acc.push(moveOrg(org.oid))
    }

    return acc
  }, [])

  console.log('**', 'About to move', moves.length, 'out of', orgs.length,
    'child organizations from', argv.sourceParentOid, 'to',
    argv.targetParentOid, '**')

  return moves
}

let delay = 0
function moveOrg(orgOid) {
  delay += argv.delayBetweenRequests
  return scheduleMoveRequest(delay, orgOid)
}

function scheduleMoveRequest(delay, orgOid) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      request({
        method: 'POST',
        uri: `${argv.baseUri}/v2/${orgOid}/organisaatiosuhde?merge=false`,
        body: argv.targetParentOid,
        headers: {
          Cookie: argv.authCookie,
          ['Content-Type']: 'application/json',
        },
        followAllRedirects: true,
      }, (error, response, body) => {
        if (error) {
          console.error(`Unable to move ${orgOid}`)
          reject(error)
        } else {
          if (response.statusCode > 199 && response.statusCode < 300) {
            console.log(`${response.statusCode}: successfully moved ${orgOid}`)
            resolve(response)
          } else {
            console.error(`${response.statusCode}: problem moving ${orgOid}`)
            reject(response)
          }
        }
      })
    }, delay)
  })
}
