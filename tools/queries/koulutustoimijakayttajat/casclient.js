const unirest = require('unirest')
const deferred = require('deferred')

function authenticate(props, service) {
    const def = deferred();

    unirest.post(props.tgt)
        .send({
            service: props.env + '/' + service + '/j_spring_cas_security_check'
        })
        .end(response => {
            const serviceGrantingTicket = response.body;
            def.resolve(serviceGrantingTicket);
        })
    return def.promise;
}

function get(props, service, parampath, success) {
    const def = deferred();

    authenticate(props, service).then(casTicket => {
        unirest.get(props.env+'/'+service+parampath).header('CasSecurityTicket', casTicket).end(response => {
            success(def, response);
        });
    });

    return def.promise;
}

function getWithoutAuth(props, service, parampath, success) {
    const def = deferred();
    unirest.get(props.env+'/'+service+parampath).end(response => {
        success(def, response);
    });
    return def.promise;
}

module.exports = {get: get, getWithoutAuth: getWithoutAuth};
