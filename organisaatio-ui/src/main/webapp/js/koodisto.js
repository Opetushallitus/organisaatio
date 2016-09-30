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

var koodisto = angular.module('Koodisto', ['Loading', 'Logging', 'ngCookies', 'ngResource']);

koodisto.factory('KoodistoClient', function ($resource) {
    return {
        // Kuntien haku koodistopalvelulta
        // Esim: https://localhost:8503/koodisto-service/rest/json/kunta/koodi
        koodistoPaikkakunnat: $resource(KOODISTO_KUNTA_KOODI + "?onlyValidKoodis=true", {}, {
            get: {method: "GET", withCredentials : true, isArray: true}
        }),
        // Kuntien haku koodistopalvelulta
        // Esim: https://localhost:8503/koodisto-service/rest/json/kunta/koodi
        koodistoPaikkakunta: $resource(KOODISTO_KUNTA_KOODI + "/:uri", {uri: "@uri"}, {
            get: {method: "GET", withCredentials : true}
        }),
        // Organisaatiotyyppien haku koodistopalvelulta
        // Esim: https://localhost:8503/koodisto-service/rest/json/organisaatiotyyppi/koodi
        koodistoOrganisaatiotyypit: $resource(KOODISTO_ORGANISAATIOTYYPPI_KOODI, {}, {
            get: {method: "GET", withCredentials : true, isArray: true}
        }),
        // Oppilaitostyyppien haku koodistopalvelulta
        // Esim: https://localhost:8503/koodisto-service/rest/json/oppilaitostyyppi/koodi
        koodistoOppilaitostyypit: $resource(KOODISTO_OPPILAITOSTYYPPI_KOODI, {}, {
            get: {method: "GET", withCredentials : true, isArray: true}
        }),
        // Usean koodin haku koodistopalvelulta
        // Esim. http://localhost:8081/koodisto-service/rest/json/searchKoodis?koodiUris=posti_52200&koodiUris=maatjavaltiot1_fin
        koodistoSearchKoodis: $resource(KOODISTO_KOODI_HAE + "?:uris", {params: "@uris"}, {
            get: {method: "GET", withCredentials : true, isArray: true}
        }),
        // Maiden haku koodistopalvelulta
        // Esim: https://localhost:8503/koodisto-service/rest/json/maatjavaltiot1/koodi
        koodistoMaat: $resource(KOODISTO_MAAT_JA_VALTIOT + "?onlyValidKoodis=true", {}, {
            get: {method: "GET", withCredentials : true, isArray: true}
        }),
        // ISO-kielilistan haku koodistopalvelulta
        // Esim: https://localhost:8503/koodisto-service/rest/json/kieli/koodi
        koodistoKieli: $resource(KOODISTO_KIELI_KOODI + "?onlyValidKoodis=true", {}, {
            get: {method: "GET", withCredentials : true, isArray: true}
        }),
        // Opetuskielten haku koodistopalvelulta
        // Esim: https://localhost:8503/koodisto-service/rest/json/oppilaitoksenopetuskieli/koodi
        koodistoOpetuskielet: $resource(KOODISTO_OPPILAITOKSENOPETUSKIELI + "?onlyValidKoodis=true", {}, {
            get: {method: "GET", withCredentials : true, isArray: true}
        }),
        // Postinumerokoodiston version haku koodistopalvelulta
        // Esim: https://localhost:8503/koodisto-service/rest/json/posti
        koodistoPostiVersio: $resource(KOODISTO_POSTI, {}, {
            get: {method: "GET", withCredentials : true}
        }),
        // Postinumeroiden haku koodistopalvelulta
        // Esim: https://localhost:8503/koodisto-service/rest/json/posti/koodi
        koodistoPosti: $resource(KOODISTO_POSTI_KOODI, {}, {
            get: {method: "GET", withCredentials : true, isArray: true}
        }),
        // Postinumeroiden haku koodistopalvelulta tai selaimen cachesta
        // Esim: https://localhost:8503/koodisto-service/rest/json/posti/koodi
        koodistoPostiCached: $resource(KOODISTO_POSTI_KOODI + "?allowCache=true", {}, {
            get: {method: "GET", withCredentials : true, isArray: true}
        }),
        // Vuosiluokkien haku koodistopalvelulta
        // Esim: https://localhost:8503/koodisto-service/rest/json/vuosiluokat/koodi
        koodistoVuosiluokat: $resource(KOODISTO_VUOSILUOKAT, {}, {
            get: {method: "GET", withCredentials : true, isArray: true}
        }),
        // Koodiston haku koodistopalvelulta koodistoUrin perusteella
        koodistoArrayByUri: $resource(KOODISTO_URI_KOODI, {params: "@uri"}, {
            get: {method: "GET", withCredentials : true, isArray: true}
        })
    }
});

koodisto.service('KoodistoKoodi', function($locale, $window, $http, LocalisationService, $log) {
    $log = $log.getInstance('KoodistoKoodi');
    var language = LocalisationService.getLocale().toUpperCase();

    this.getLocalizedName = function(koodi) {
        var nimi = koodi.metadata[0].nimi;
        koodi.metadata.forEach(function(metadata){
            if(angular.isUndefined(language))
                $log.warn('this.language called before defined.');
            if(metadata.kieli === language) {
                nimi = metadata.nimi;
            }
        });
        return nimi;
    };

    // lang = FI tai SV
    this.getLangName = function(koodi, lang) {
        var nimi = koodi.metadata[0].nimi;
        koodi.metadata.forEach(function(metadata){
            if(metadata.kieli === lang) {
                nimi = metadata.nimi;
            }
        });
        return nimi;
    };

    this.getLanguage = function() {
        if(angular.isUndefined(language))
            $log.warn('this.language is undefined');
        return language;
    };

    this.isValid = function(koodi) {
        if (koodi.voimassaAlkuPvm) {
            if (new Date() < new Date(koodi.voimassaAlkuPvm)) {
                // Ei vielä voimassa
                return false;
            }
        }
        if (koodi.voimassaLoppuPvm) {
            if (new Date() > new Date(koodi.voimassaLoppuPvm)) {
                // Ei enää voimassa
                return false;
            }
        }
        return true;
    };

});
