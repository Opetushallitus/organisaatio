app.factory('RyhmienHallintaModel', function(Ryhmat, Organisaatio, UusiOrganisaatio) {

    var model = new function() {
        var ryhmat = [];
        this.groups = ryhmat;

        this.reload = function(parentOid, callback, virheCallback) {
            ryhmat.length = 0;
            // TODO: Remove dummy implemetation when api is available
            /*[
             {version: 0,
             parentOid: '1.2.246.562.10.86037067971',
             oid: '1.2.246.562.28.00000000001',
             tyypit: ['Ryhmä'],
             "nimi": {
             "fi": "Ryhmä ykkönen",
             "sv": null,
             "en": null
             }},
             {version: 0,
             parentOid: '1.2.246.562.10.86037067971',
             oid: '1.2.246.562.28.00000000002',
             tyypit: ['Ryhmä'],
             "nimi": {
             "fi": "Ryhmä kakkonen",
             "sv": null,
             "en": null
             }}].forEach(function(ryhma) {
             ryhmat.push(ryhma);
             });*/

            Ryhmat.get({oid: parentOid}, function(result) {
                result.forEach(function(ryhma) {
                    if (!ryhma.kuvaus2) {
                        ryhma.kuvaus2 = {};
                    }
                    ryhmat.push(ryhma);
                });
                callback();
            }, virheCallback);
        };

        this.create = function(parentOid) {
            var ryhma = {
                version: 0,
                parentOid: parentOid,
                oid: null,
                tyypit: ['Ryhma'],
                "nimi": {
                    "fi": null,
                    "sv": null,
                    "en": null
                },
                "kuvaus2": {
                    "kieli_fi#1": null,
                    "kieli_sv#1": null,
                    "kieli_en#1": null
                }
            };
            ryhmat.push(ryhma);
            return ryhma;
        };

        this.save = function(ryhma, callback, virheCallback) {
            var fn;
            if (ryhma.oid === null) {
                fn = UusiOrganisaatio.put;
            } else {
                fn = Organisaatio.post;
            }
            fn(ryhma, function(result) {
                var ind = ryhmat.indexOf(ryhma);
                if (ind !== -1) {
                    ryhmat.splice(ind, 1);
                }
                ryhmat.push(result);
                callback(result);
            }, virheCallback);
        };

        this.delete = function(ryhma, callback, virheCallback) {
            var ind = ryhmat.indexOf(ryhma);
            if (ryhma.oid !== null) {
                Organisaatio.delete(ryhma, function(result) {
                    if (ind !== -1) {
                        ryhmat.splice(ind, 1);
                    }
                    callback();
                }, virheCallback);
            } else {
                if (ind !== -1) {
                    ryhmat.splice(ind, 1);
                }
                callback();
            }
        };

    };

    return model;
});
