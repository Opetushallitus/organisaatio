/*
Scripti joka toteuttaa seuraavan haun: https://jira.oph.ware.fi/jira/browse/KJHH-1006
 */

const unirest = require('unirest');
const client = require("./casclient");
const prompt = require('prompt');
const deferred = require('deferred')
const fs = require('fs');

//deferred.monitor();

//const ryhmaIdt = [{id:"1216", name:"Koulutustoimijan vastuukäyttäjä"}, {id:"4825743",name:"Organisaatiopalvelun pääkäyttäjä"}, {id:"345926", name:"Organisaatiotietojen ylläpitäjä"}]; //QA
const ryhmaIdt = [{id:"1216", name:"Koulutustoimijan vastuukäyttäjä"}, {id:"5101119",name:"Organisaatiopalvelun pääkäyttäjä"}, {id:"345926", name:"Organisaatiotietojen ylläpitäjä"}]; //prod

var stream = fs.createWriteStream('export.csv');

var schema = {
    properties: {
        env: {
            description: 'Ympäristö',
            required: true,
            default: 'https://virkailija.opintopolku.fi'
//            default: 'https://testi.virkailija.opintopolku.fi'
        },
        username: {
            description: 'Käyttäjätunnus',
            required: true,
            default: 'x'
        },
        password: {
            hidden: true,
            description: 'Salasana',
            required: true,
              default: 'xx'
        }
    }
};

prompt.start();
prompt.get(schema, function (err, input) {
    unirest.post(input.env + '/cas/v1/tickets')
        .send({
            username: input.username,
            password: input.password
        })
        .end(response => {
            const ticketGrantingTicket = response.headers.location;
            haekayttajat({env: input.env, tgt: ticketGrantingTicket});
        });
});

var haekayttajat = (props) => {
    stream.write("\uFEFF");
    stream.write("virkailija\tkoulutustoimija\tsahkoposti\tryhma\n");
    deferred(getRyhmanHenkilot(ryhmaIdt[0], props), getRyhmanHenkilot(ryhmaIdt[1], props), getRyhmanHenkilot(ryhmaIdt[2], props)).then(resp => {

        for(var i=0; i<3; i++) {
            console.log("ryhma " + resp[i].ryhma.name + ": " + resp[i].data.personOids.length);
        resp[i].data.personOids.map(oid => {
            getHenkiloEmail(oid, resp[i].ryhma, props).then(henkilo => {
                getHenkilonOrgByRyhma(oid, henkilo.ryhma.id, props).then(orgId => {
                    getKoulutusToimijaForOrg(orgId, props).then(nimi => {
                        stream.write(henkilo.nimi + "\t"+ nimi + "\t" + henkilo.email +"\t" + henkilo.ryhma.name + "\n");
                    })
                });
            });
        });
        }


    });
}

var getRyhmanHenkilot = (ryhma, props) => {
    return client.get(props, 'kayttooikeus-service', '/kayttooikeusryhma/' + ryhma.id + '/henkilot', (promise, response) => {
            promise.resolve({ryhma:ryhma, data:response.body});
    })
};

var getHenkiloEmail = deferred.gate((henkilooid, ryhma, props) => {
    return client.get(props, 'oppijanumerorekisteri-service', '/henkilo/' + henkilooid, (promise, response) => {
        var person = {
            oid: henkilooid,
            nimi: response.body.kutsumanimi + " " + response.body.sukunimi,
            ryhma: ryhma,
            email: ""
        };

        for(var r = 0; r < response.body.yhteystiedotRyhma.length; r++) {
        var yhteystiedotRyhma = response.body.yhteystiedotRyhma[r];
        if(yhteystiedotRyhma.ryhmaKuvaus == 'yhteystietotyyppi2') {
            for (var yt = 0; yt < yhteystiedotRyhma.yhteystieto.length; yt++) {
                if (yhteystiedotRyhma.yhteystieto[yt].yhteystietoTyyppi == 'YHTEYSTIETO_SAHKOPOSTI') {
                    person.email = yhteystiedotRyhma.yhteystieto[yt].yhteystietoArvo;
                    break;
                }
            }
        }
    }
    promise.resolve(person);
    });
}, 1);

var getHenkilonOrgByRyhma = (henkilooid, ryhmaId, props) => {
    return client.get(props, 'kayttooikeus-service', '/kayttooikeusryhma/henkilo/' + henkilooid, (promise, response) => {
        promise.resolve(response.body.find(ryhma => { return ryhmaId == ryhma.ryhmaId; }).organisaatioOid);
    })
};

var getKoulutusToimijaForOrg = (orgOid, props) => {
    const def = deferred();
    orgRecursive(orgOid, props, def);
    return def.promise();
};

var orgRecursive = (orgOid, props, def) => {
    console.log("processing organisation:" + orgOid);
    client.getWithoutAuth(props, 'organisaatio-service', '/rest/organisaatio/' + orgOid + '?includeImage=false', (promise, response) => {
        promise.resolve(response.body);
    }).then(data => {
        if(data.tyypit != undefined && data.tyypit.some(str => { return str == "Koulutustoimija"})) {
            def.resolve(data.nimi.fi == undefined ? data.nimi.sv : data.nimi.fi);
        } else {
            if(data.parentOid == undefined || orgOid == '1.2.246.562.10.00000000001') {
                def.resolve(data.nimi.fi == undefined ? data.nimi.sv : data.nimi.fi);
            } else {
                orgRecursive(data.parentOid, props, def);
            }
        }
    })
}
