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

    function initOrganisaatioApp() {
        angular.element(document).ready(function() {
            angular.bootstrap(document, ['organisaatio']);
        });
    }

    function logRequest(xhr, status) {
        console.log("** Localisation request "+status+": "+xhr.status+" "+xhr.statusText, xhr);
    }

    //
    // Ladataan organisaatioiden lokalisoinnit
    //
    var localisationUrl = LOKALISAATIO_URL_BASE + 'v1/localisation?category=organisaatio';
    console.log("** Loading localisation info; from: ", localisationUrl);
    jQuery.ajax(localisationUrl, {
        dataType: 'json',
        crossDomain: true,
        complete: logRequest,
        success: function(xhr, status) {
            window.APP_LOCALISATION_DATA = xhr;
            console.log('** Localisation info was successfully loaded.');
            initOrganisaatioApp();
        },
        error: function(xhr, status) {
            window.APP_LOCALISATION_DATA = [];
            console.log('** There was an error while loading the localisation info: ', status, xhr);
            initOrganisaatioApp();
        }
    });
}
