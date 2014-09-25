app.factory('RyhmienHallintaModel', function(Ryhmat, Organisaatio, UusiOrganisaatio) {

    var model = {
        ryhmat : [],
        groups : [],

        reload : function(parentOid, callback, virheCallback) {
            model.ryhmat.length = 0;
            Ryhmat.get({oid: parentOid}, function(result) {
                result.forEach(function(ryhma) {
                    if (!ryhma.kuvaus2) {
                        ryhma.kuvaus2 = {};
                    }
                    ryhma.tyypit = ['Ryhma'];
                    if (ryhma.ryhmatyypit.length===0) {
                        ryhma.ryhmatyypit = [''];
                    }
                    if (ryhma.kayttoryhmat.length===0) {
                        ryhma.kayttoryhmat = [''];
                    }
                    model.ryhmat.push(ryhma);
                });
                model.groups = model.ryhmat;
                callback();
            }, virheCallback);
        },

        create : function(parentOid) {
            var ryhma = {
                version: 0,
                parentOid: parentOid,
                oid: null,
                tyypit: ['Ryhma'],
                ryhmatyypit: [''],
                kayttoryhmat: [''],
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
            model.ryhmat.push(ryhma);
            return ryhma;
        },

        save : function(ryhma, callback, virheCallback) {
            var fn;
            if (ryhma.oid === null) {
                fn = UusiOrganisaatio.put;
            } else {
                fn = Organisaatio.post;
            }
            fn(ryhma, function(result) {
                var ind = model.ryhmat.indexOf(ryhma);
                if (ind !== -1) {
                    model.ryhmat.splice(ind, 1);
                }
                model.ryhmat.push(result);
                callback(result);
            }, virheCallback);
        },

        delete : function(ryhma, callback, virheCallback) {
            var ind = model.ryhmat.indexOf(ryhma);
            if (ryhma.oid !== null) {
                Organisaatio.delete(ryhma, function(result) {
                    if (ind !== -1) {
                        model.ryhmat.splice(ind, 1);
                    }
                    callback();
                }, virheCallback);
            } else {
                if (ind !== -1) {
                    model.ryhmat.splice(ind, 1);
                }
                callback();
            }
        }

    };

    return model;
});
