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

// Loads Localisation data from Lokalisaatiopalvelu prior to initializing the Angular App
function organisaatioInitialize() {
    jQuery.support.cors = true;
    var initInjector = angular.injector(['ng']);
    var $http = initInjector.get('$http');
    var $q = initInjector.get('$q');

    function initOrganisaatioApp() {
        angular.element(document).ready(function() {
            angular.bootstrap(document, ['organisaatio']);
        });
    }

    // Set default headers for this context.
    $http.defaults.headers.common['Caller-Id'] = "1.2.246.562.10.00000000001.organisaatio-ui";

    //
    // Ladataan organisaatioiden lokalisoinnit ja käyttäjän kieli
    //
    var localisationUrl = V1_LOKALISAATIO_URL + '?category=organisaatio';
    console.log("** Loading localisation info; from: ", localisationUrl);

    var promiseLocalisationData = $http.get(localisationUrl, {responseType: 'json'}).then(
        function(response) {
            window.APP_LOCALISATION_DATA = response.data;
            console.log('** Localisation info was successfully loaded.');
            console.log("** Localisation request "+response.status+" "+response.statusText, response);
        },
        function(response) {
            window.APP_LOCALISATION_DATA = [];
            console.warn('** There was an error while loading the localisation info: ', response.status, response);
            console.log("** Localisation request "+response.status+" "+response.statusText, response);
        }
    );

    // Make sure this function is not spammed.
    var lang = 'fi';
    var failureHandler = function (response) {
      console.warn(
        "Failed to get: " + CAS_ME_URL + " --> using language: " + lang
      );
      window.APP_CAS_ME = {};
      window.APP_CAS_ME.lang = lang.toLowerCase();
    };
    var promiseCasMe = $http
      .get(CAS_ME_URL)
      .then(function () {
        return $http.get(CAS_ME_URL);
      }, failureHandler)
      .then(function (response) {
        console.log("Success on " + CAS_ME_URL, response);
        lang = response.data.lang || lang;
        if (lang) {
          // Toistaiseksi vain SV on tuettu FI:n lisäksi
          lang = lang === "sv" ? "sv" : "fi";
        } else {
          console.warn("failed parsing result, defaulting to fi");
        }
        window.APP_CAS_ME = response.data;
        window.APP_CAS_ME.lang = lang.toLowerCase();
      }, failureHandler);

    // Start angular app manually when
    $q.all([promiseLocalisationData, promiseCasMe]).then(
        function(response) {
            initOrganisaatioApp();
        }
    );
}
