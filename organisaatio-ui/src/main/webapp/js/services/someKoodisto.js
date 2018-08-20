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

var koodisto = angular.module('Koodisto');

koodisto.factory('SomeKoodisto', function($q, $log, $filter, $injector,
                                     KoodistoKoodi, Alert, KoodistoClient) {

    $log = $log.getInstance("SomeKoodisto");
    var loadingService = $injector.get('LoadingService');

    var showAndLogError = function(msg, response) {
        loadingService.onErrorHandled(response);
        model.alert = Alert.add("error", $filter('i18n')(response.data.errorKey || msg), false);
        $log.error(msg + " (status: " + response.status + ")");
    };

    /**
     * Palauttaa koodiston arrayna, jonka itemit muotoa { uri: <koodiuri>, nimi: <lokalisoitu nimi> }
     *
     * @param uri {String} koodistoUri
     * @param resultArray {Array} Array johon koodi-itemit tallennetaan
     * @param defaultArray {Array} Jos tyyppiä Array, palautetaan virhetilanteessa eikä näytetä virheilmoitusta.
     *                 Jos muu kuin Array, näytetään virheilmoitus.
     */
    var getKoodistoArray = function(uri, resultArray, defaultArray) {
        $log.info("getKoodistoArray(): " + uri);

        var deferred = $q.defer();
        KoodistoClient.koodistoArrayByUri.get({uri: uri}, function(result) {
            resultArray.length = 0;
            result.forEach(function(rTyyppiKoodi) {
                resultArray.push({type: rTyyppiKoodi.koodiUri + "#" + rTyyppiKoodi.versio,
                    nimi: KoodistoKoodi.getLocalizedName(rTyyppiKoodi)});
            });
            deferred.resolve();
        }, function(response) {
            // koodeja ei löytynyt
            if (defaultArray instanceof Array) {
                defaultArray.forEach(function(rTyyppiKoodi) {
                    resultArray.push(rTyyppiKoodi);
                });
            } else {
                showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
            }
            deferred.resolve();
        });
        return deferred.promise;
    };

    var getAutofill = function(type) {
        if (type === 'FACEBOOK' || type === 'sosiaalinenmedia_1#1') {
            return 'https://www.facebook.com/';
        }
        if (type === 'GOOGLE_PLUS' || type === "sosiaalinenmedia_2#1") {
            return 'https://plus.google.com/';
        }
        if (type === 'LINKED_IN' || type === 'sosiaalinenmedia_3#1') {
            return 'https://linkedin.com/';
        }
        if (type === 'TWITTER' || type === 'sosiaalinenmedia_4#1') {
            return 'https://twitter.com/';
        }
        if (type === 'INSTAGRAM' || type === 'sosiaalinenmedia_6#1') {
            return 'https://instagram.com/'
        }
        if (type === 'YOUTUBE' || type === 'sosiaalinenmedia_7#1') {
            return 'https://youtube.com/'
        }

        // Default / MUU
        return 'https://';
    };

    var getValidator = function(type) {
        if (type === 'FACEBOOK' || type === 'sosiaalinenmedia_1#1') {
            return '^https{0,1}://(?:www\.){0,1}facebook.com/.+';
        }
        if (type === 'GOOGLE_PLUS' || type === "sosiaalinenmedia_2#1") {
            return '^https{0,1}://plus.google.com/.+';
        }
        if (type === 'LINKED_IN' || type === 'sosiaalinenmedia_3#1') {
            return '^https{0,1}://(?:www\.){0,1}linkedin.com/.+';
        }
        if (type === 'TWITTER' || type === 'sosiaalinenmedia_4#1') {
            return '^https{0,1}://(?:www\.){0,1}twitter.com/.+';
        }
        if (type === 'INSTAGRAM' || type === 'sosiaalinenmedia_6#1') {
            return '^https{0,1}://(?:www\.){0,1}instagram.com/.+';
        }
        if (type === 'YOUTUBE' || type === 'sosiaalinenmedia_7#1') {
            return '^https{0,1}://(?:www\.){0,1}youtube.com/.+';
        }

        // Default / MUU
        return '^https{0,1}://.+';
    };

    var model = new function() {
        this.some = [];
        this.sometyypit = [];
        this.someurls = {};

        someKoodisto = {
                uri: 'sosiaalinenmedia',
                resultArray: this.some,
                defaultArray: [
                    // Vanhoja default-arvoja käytetään kunnes otetaan käyttöön 'sosiaalinenmedia'-koodisto
                    {type: 'FACEBOOK',    nimi: $filter('i18n')('Organisaationtarkastelu.FACEBOOK')},
                    {type: 'GOOGLE_PLUS', nimi: $filter('i18n')('Organisaationtarkastelu.GOOGLE_PLUS')},
                    {type: 'LINKED_IN',   nimi: $filter('i18n')('Organisaationtarkastelu.LINKED_IN')},
                    {type: 'TWITTER',     nimi: $filter('i18n')('Organisaationtarkastelu.TWITTER')},
                    {type: 'MUU',         nimi: $filter('i18n')('Organisaationtarkastelu.MUU')},
                    {type: 'YOUTUBE',     nimi: $filter('i18n')('Organisaationtarkastelu.YOUTUBE')},
                    {type: 'INSTAGRAM',   nimi: $filter('i18n')('Organisaationtarkastelu.INSTAGRAM')}
                ]
            };

        getKoodistoArray(someKoodisto.uri,
                         someKoodisto.resultArray,
                         someKoodisto.defaultArray).then(function() {

            // Kopioidaan tyypit omaan listaan
            model.sometyypit.length = 0;
            model.sometyypit.push.apply(model.sometyypit, $.map(model.some, function(v, i){
                return v.type;
            }));

            // Laitetaan sometyypeille autofill ja validaattori
            model.some.forEach(function(some) {
                model.someurls[some.type] = {
                    autofill: getAutofill(some.type),
                    validator: getValidator(some.type),
                    nimi: some.nimi
                };
            });

        });
    };

    return model;
});
