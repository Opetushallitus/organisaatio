/*
 Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus

 This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 soon as they will be approved by the European Commission - subsequent versions
 of the EUPL (the "Licence");

 You may not use this work except in compliance with the Licence.
 You may obtain a copy of the Licence at: http://www.osor.eu/eupl/

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 European Union Public Licence for more details.
 */

app.factory('RyhmienHallintaModel', function($log, $injector,
                                             Ryhmat, Organisaatio,
                                             Paivittaja, Henkilo,
                                             UusiOrganisaatio) {

    $log = $log.getInstance("RyhmienHallintaModel");
    var loadingService = $injector.get('LoadingService');

    var model = {
        ryhmat : [],
        groups : [],
        paivitys : {},

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

        clearUpdateInfo : function() {
            $log.debug("clearUpdateInfo()");
            model.paivitys = {};
        },

        loadUpdateInfo : function(oid, callback, virheCallback) {
            $log.debug("loadUpdateInfo() : " + oid);
            model.paivitys = {};
            Paivittaja.get({oid: oid}, function(paivitys) {
                if (paivitys.paivitysPvm) {
                    var pvm = moment(new Date(paivitys.paivitysPvm));
                    model.paivitys.pvm = pvm.format('DD.MM.YYYY h:mm:ss');
                    Henkilo.get({hlooid: paivitys.paivittaja}, function(paivittaja_hlo) {
                        model.paivitys.paivittaja = paivittaja_hlo.etunimet + ' ' + paivittaja_hlo.sukunimi;
                    },
                    // Error case
                    function(response) {
                        $log.warn("Failed to get Henkilo!", response);
                        $log.debug("disable system error dialog.");
                        loadingService.onErrorHandled();
                        model.paivitys.paivittaja = paivitys.paivittaja;
                    });
                }
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
                model.ryhmat.push(result.organisaatio);
                callback(result.organisaatio);
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
