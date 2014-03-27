app.factory('RyhmienHallintaModel', function(Ryhmat) {

    var model = new function() {
        var ryhmat = [];
        this.groups = ryhmat;
        function loadRyhmat() {
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

            Ryhmat.get({}, function(result) {
                result.forEach(function(ryhma) {
                    ryhmat.push(ryhma);
                });
            }, function(error) {
                // Ei ryhmiä?
            });
        };

        this.create = function(parentOid) {
            var ryhma = {
                version: 0,
                parentOid: parentOid,
                oid: null,
                tyypit: ['Ryhmä'],
                "nimi": {
                    "fi": null,
                    "sv": null,
                    "en": null
                }
            };
            ryhmat.push(ryhma);
            return ryhma;
        };
        this.save = function(ryhma, callback, virheCallback) {
            var fn;
            if (ryhma.oid === null) {
                fn = Ryhmat.put;
            } else {
                fn = Ryhmat.post;
            }
            fn(ryhma, function(result) {
                var ind = ryhmat.indexOf(ytt);
                if (ind !== -1) {
                    ryhmat.splice(ind, 1);
                }
                ryhmat.push(ryhma);
                callback(ryhma);
            }, virheCallback);
        };
        this.delete = function(ryhma, callback, virheCallback) {
            var ind = ryhmat.indexOf(ryhma);
            if (ryhma.oid !== null) {
                RyhmanPoisto.delete({oid: ryhma.oid}, function(result) {
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
        this.reload = function() {
            ryhmat.length = 0;
            loadRyhmat();
        };
        loadRyhmat();
    };
    return model;
});
