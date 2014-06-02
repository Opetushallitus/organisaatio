// Loads Localisation data from Lokalisaatiopalvelu prior to initializing the Angular App
function organisaatioInitialize() {

  function initOrganisaatioApp() {
    angular.element(document).ready(function() {
      angular.bootstrap(document, ['organisaatio']);
    });
  }

  var localisationUrl = LOKALISAATIO_URL_BASE + 'v1/localisation?category=organisaatio';
  console.log('Loading localisation info from: ', localisationUrl);

  jQuery.ajax(localisationUrl, {
    dataType: 'json',
    crossDomain: true,
    success: function(xhr, status) {
      window.LOCALISATION_DATA = xhr;
      console.log('Localisation info was successfully loaded.');
      initOrganisaatioApp();
    },
    error: function(xhr, status) {
      window.LOCALISATION_DATA = [];
      console.log('There was an error while loading the localisation info: ', status, xhr);
    }
  });
}
