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

app.factory('YritysValintaModel', function($filter, $log, $injector,
                                           Alert,
                                           YTJYritysTiedot,
                                           YTJYritystenTiedot) {

    $log = $log.getInstance("YritysValintaModel");

    var loadingService = $injector.get('LoadingService');

    var model = {
        hakuString: "",
        yritykset: [],

        isYTunnus: function(str) {
            // Y-Tunnus on muotoa NNNNNNN-T
            if (str.length !== 9) {
                return false;
            }
            // Tarkistetaan että numeropositioissa on vain numeroita.
            if (!/\d{7}.\d/.test(str)) {
                return false;
            }
            // Tarkistetaan että välimerkki on väliviiva.
            if (str.charAt(7) !== "-") {
                return false;
            }
            return true;
        },


        refresh: function() {
            $log.log('refresh()');
            var start = +new Date();

            if (this.isYTunnus(model.hakuString)) {
                YTJYritysTiedot.get({ytunnus: model.hakuString}, function(result) {
                    var end = +new Date();  // log end timestamp
                    var diff = end - start;
                    $log.log("Haku kesti: " +diff);
                    model.yritykset.length = 0;
                    model.yritykset.push(result);

                    if (model.yritykset.length === 0) {
                        Alert.add("warnign", $filter('i18n')("YritysValinta.eiHakutuloksia", ""), true, true);
                    }
                },
                // Error case
                function(response) {
                    loadingService.onErrorHandled();
                    $log.error("YTJYritysTiedot response: " + response.status);
                    Alert.add("error", $filter('i18n')("YritysValinta.virheViesti", ""), true, true);
                });
            }
            else {
                YTJYritystenTiedot.get({nimi: model.hakuString}, function(result) {
                    var end = +new Date();  // log end timestamp
                    var diff = end - start;
                    $log.log("Haku kesti: " +diff);
                    model.yritykset = result;

                    if (model.hakuString.length>0 && model.yritykset.length === 0) {
                        Alert.add("warnign", $filter('i18n')("YritysValinta.eiHakutuloksia", ""), true, true);
                    }
                },
                // Error case
                function(response) {
                    loadingService.onErrorHandled();
                    $log.error("YTJYritystenTiedot response: " + response.status);
                    Alert.add("error", $filter('i18n')("YritysValinta.virheViesti", ""), true, true);
                });
            }
        }
    };

    return model;
});



