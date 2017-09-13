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

app.factory('RyhmaKoodisto', function($q, $log, $filter, $injector,
                                      KoodistoKoodi, Alert, KoodistoClient) {

    $log = $log.getInstance("RyhmaKoodisto");
    var loadingService = $injector.get('LoadingService');

    var showAndLogError = function(msg, response) {
        loadingService.onErrorHandled(response);
        model.alert = Alert.add("error", $filter('i18n')(response.data.errorKey || msg), false);
        $log.error(msg + " (status: " + response.status + ")");
    };

    /*
     * Palauttaa koodiston arrayna, jonka itemit muotoa { uri: <koodiuri>, nimi: <lokalisoitu nimi> }
     * Parametrit:
     *   uri: koodistoUri
     *   resultArray: Array johon koodi-itemit tallennetaan
     */
    var getKoodistoArray = function(uri, resultArray) {
        KoodistoClient.koodistoArrayByUri.get({uri: uri}, function(result) {
            resultArray.length = 0;
            result.forEach(function(rTyyppiKoodi) {
                resultArray.push({uri: rTyyppiKoodi.koodiUri + "#" + rTyyppiKoodi.versio,
                                  nimi: KoodistoKoodi.getLocalizedName(rTyyppiKoodi)});
            });
        }, function(response) {
            // koodeja ei löytynyt
            showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
        });
    };

    var model = new function() {
        this.ryhmatyypit = [];
        this.kayttoryhmat = [];

        var koodistoArrays = [
            {
                uri: 'ryhmatyypit',
                resultArray: this.ryhmatyypit,
            },
            {
                uri: 'kayttoryhmat',
                resultArray: this.kayttoryhmat,
            }
        ];

        koodistoArrays.forEach(function(koodistoItem) {
            getKoodistoArray(koodistoItem.uri, koodistoItem.resultArray, koodistoItem.defaultArray);
        });

    };
    return model;
});
