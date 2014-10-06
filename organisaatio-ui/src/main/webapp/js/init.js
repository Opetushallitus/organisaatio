// Loads Localisation data from Lokalisaatiopalvelu prior to initializing the Angular App
function organisaatioInitialize() {
    jQuery.support.cors = true;

    function initOrganisaatioApp() {
        angular.element(document).ready(function() {
            angular.bootstrap(document, ['organisaatio']);
        });
    }

    function logRequest(xhr, status) {
        console.log("LOG "+status+": "+xhr.status+" "+xhr.statusText, xhr);
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
            console.log('Localisation info was successfully loaded.');
            initOrganisaatioApp();
        },
        error: function(xhr, status) {
            window.APP_LOCALISATION_DATA = [];
            console.log('There was an error while loading the localisation info: ', status, xhr);
            initOrganisaatioApp();
        }
    });
}
