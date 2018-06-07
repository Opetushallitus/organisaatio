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
        paivitys : {},

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
                        loadingService.onErrorHandled(response);
                        model.paivitys.paivittaja = paivitys.paivittaja;
                    });
                }
                callback();
            }, virheCallback);
        },

        save : function(ryhma, callback, virheCallback) {
            var fn;
            if (ryhma.oid === null) {
                fn = UusiOrganisaatio.create;
            } else {
                fn = Organisaatio.update;
            }
            fn(ryhma, function(result) {
                callback(result.organisaatio);
            }, virheCallback);
        },

        delete : function(ryhma, callback, virheCallback) {
            Organisaatio.delete(ryhma, function(result) {
                callback();
            }, virheCallback);
        }

    };

    return model;
});
