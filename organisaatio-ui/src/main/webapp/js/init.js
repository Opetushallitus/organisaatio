// Loads Localisation data from Lokalisaatiopalvelu prior to initializing the Angular App
function organisaatioInitialize() {

    function initOrganisaatioApp() {
        angular.element(document).ready(function() {
            angular.bootstrap(document, ['organisaatio']);
        });
    }

    var localisationUrl = LOKALISAATIO_URL_BASE + 'v1/localisation?category=organisaatio';
    console.log('Loading localisation info from: ', localisationUrl);

    if (typeof new XMLHttpRequest().responseType !== 'string' && window.XDomainRequest) { //IE8 and IE9
        var xdr = new XDomainRequest();
        xdr.open("get", localisationUrl);
        xdr.onload = function () {
            var data = jQuery.parseJSON(xdr.responseText);
            window.APP_LOCALISATION_DATA = data;
            console.log('Localisation info was successfully loaded.');
            initOrganisaatioApp();
        };
        xdr.onprogress = function () { };
        xdr.ontimeout = function () { };
        xdr.onerror = function () {
            window.LOCALISATION_DATA = [];
            console.log('There was an error while loading the localisation info!');
        };
        setTimeout(function () {
            xdr.send();
        });
    } else {
        jQuery.ajax(localisationUrl, {
            dataType: 'json',
            crossDomain: true,
            success: function(xhr, status) {
                window.APP_LOCALISATION_DATA = xhr;
                console.log('Localisation info was successfully loaded.');
                initOrganisaatioApp();
            },
            error: function(xhr, status) {
                window.LOCALISATION_DATA = [];
                console.log('There was an error while loading the localisation info: ', status, xhr);
            }
        });
    }
}
