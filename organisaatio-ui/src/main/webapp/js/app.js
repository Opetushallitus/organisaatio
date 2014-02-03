var app = angular.module('organisaatio', ['ngResource', 'loading', 'ngRoute', 'localization', 'ui.bootstrap', 'ngSanitize', 'ui.tinymce']);

angular.module('localization', [])
.filter('i18n', ['$rootScope','$locale', '$window',function ($rootScope, $locale, $window) {
    var language = $window.navigator.userLanguage || $window.navigator.language;
    if (language) {
        language = language.substr(0,2).toUpperCase();
        if (language!=="FI" && language!=="SV") {
            language = "FI";
        }
    } else {
        language = "FI";
    }
	jQuery.i18n.properties({
	    name:'messages',
	    path:'../i18n/',
	    mode:'map',
	    language: language,
	    callback: function() {
	    }
	});

    return function (text) {
        return jQuery.i18n.prop(text);
    };
}]);

app.filter('fixHttpLink',function () {
    return function (text) {
        proto = text.split("://");
        return (proto.length>1 ? proto[0] : "http") + "://" + proto[proto.length-1];
    };
});

////////////
//
// Configuration from config/properties files
//
////////////
var SERVICE_URL_BASE = SERVICE_URL_BASE || "";
var TEMPLATE_URL_BASE = TEMPLATE_URL_BASE || "";
var KOODISTO_URL_BASE = KOODISTO_URL_BASE || "";
var ROOT_ORGANISAATIO_OID = ROOT_ORGANISAATIO_OID || "";

////////////
//
// Route configuration
//
////////////
app.config(function($routeProvider) {
        $routeProvider.

        // front page
        when('/organisaatiot', {controller:OrganisaatioTreeController, templateUrl:TEMPLATE_URL_BASE + 'organisaatiot.html'}).

        // read one
        when('/organisaatiot/:oid', {controller:OrganisaatioController, templateUrl:TEMPLATE_URL_BASE + 'organisaationtarkastelu.html'}).

        // edit one
        when('/organisaatiot/:oid/edit', {controller:OrganisaatioController, templateUrl:TEMPLATE_URL_BASE + 'organisaationmuokkaus.html'}).

        // create new
        when('/organisaatiot/:parentoid/new', {controller:OrganisaatioController, templateUrl:TEMPLATE_URL_BASE + 'organisaationmuokkaus.html'}).

        //else
        otherwise({redirectTo:'/organisaatiot'});
});

////////////
//
// Services
//
////////////
app.service('KoodistoKoodi', function($locale, $window, $http) {
    var language = $window.navigator.userLanguage || $window.navigator.language;
    if (language) {
        language = language.substr(0,2).toUpperCase();
        if (language!=="FI" && language!=="SV") {
            language = "FI";
        }
    } else {
        language = "FI";
    }
    this.getLocalizedName = function(koodi) {
        var nimi = koodi.metadata[0].nimi;
        koodi.metadata.forEach(function(metadata){
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

// Esimerkki: Alert.add("warning", $filter('i18n')("YritysValinta.virheViesti", ""), true);
app.factory('Alert', ['$rootScope', '$timeout', function($rootScope, $timeout) {
        var alertService;
        $rootScope.alerts = [];
        return alertService = {
            add: function(type, msg, usetimeout, hideOnTopLevel) {
                var alert = {
                    type: type,
                    msg: msg,
                    showOnTopLevel: !hideOnTopLevel,
                    close: function() {
                        return alertService.closeAlert(this);
                    }
                };
                // Tarkistetaan onko tälläistä virhettä jo, jos on niin ei luoda uutta
                for (var i = 0; i < $rootScope.alerts.length; i++) {
                    if ($rootScope.alerts[i].type === alert.type &&
                        $rootScope.alerts[i].message === alert.message) {
                       return $rootScope.alerts[i];
                    }
                }
                if (usetimeout) {
                    alert.timeout = $timeout(function() {
                        alertService.closeAlert(this);
                    }, 8000);
                }
                return $rootScope.alerts.push(alert);
            },
            closeAlert: function(alert) {
                return this.closeAlertIdx($rootScope.alerts.indexOf(alert));
            },
            closeAlertIdx: function(index) {
                return $rootScope.alerts.splice(index, 1);
            },
            clear: function(){
                $rootScope.alerts = [];
            }
        };
    }
]);

////////////
//
// REST resources
//
////////////

// Organisaation haku / tallennus organisaatiopalvelulta
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/1.2.246.562.10.23198065932
app.factory('Organisaatio', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/:oid", {oid: "@oid"}, {
        get: {method:   "GET"},
        post:{method:   "POST"},
        delete:{method: "DELETE"}
    });
});

// Organisaation haku / tallennus organisaatiopalvelulta
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/1.2.246.562.10.23198065932
app.factory('UusiOrganisaatio', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio", {}, {
        put: {method:   "PUT"}
    });
});

// Hae aliorganisaatiot organisaatiopalvelulta
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/hae?oidRestrictionList=1.2.246.562.10.59347432821
app.factory('Aliorganisaatiot', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/hae?oidRestrictionList=:oid", {oid: "@oid"}, {
        get: {method: "GET"}
    });
});

// Organisaatioiden haku
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/hae?searchstr=lukio&lakkautetut=true
app.factory('Organisaatiot', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/hae", {}, {
        get: {method: 'GET'}
    });
});

// Kuntien haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/kunta/koodi
app.factory('KoodistoPaikkakunnat', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/kunta/koodi", {}, {
    get: {method: "GET", isArray: true}
  });
});

// Organisaatiotyyppien haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/organisaatiotyyppi/koodi
app.factory('KoodistoOrganisaatiotyypit', function($resource) {
    return $resource(KOODISTO_URL_BASE + "json/organisaatiotyyppi/koodi", {}, {
        get: {method: "GET", isArray: true}
    });
});

// Oppilaitostyyppien haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/oppilaitostyyppi/koodi
app.factory('KoodistoOppilaitostyypit', function($resource) {
    return $resource(KOODISTO_URL_BASE + "json/oppilaitostyyppi/koodi", {}, {
        get: {method: "GET", isArray: true}
    });
});

// Usean koodin haku koodistopalvelulta
// Esim. http://localhost:8081/koodisto-service/rest/json/searchKoodis?koodiUris=posti_52200&koodiUris=maatjavaltiot1_fin
app.factory('KoodistoSearchKoodis', function($resource) {
    return $resource(KOODISTO_URL_BASE + "json/searchKoodis?:uris", {params: "@uris"}, {
        get: {method: "GET", isArray: true}
    });
});

// Maiden haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/maatjavaltiot1/koodi
app.factory('KoodistoMaat', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/maatjavaltiot1/koodi", {}, {
    get: {method: "GET", isArray: true}
  });
});

// Kielten haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/kielivalikoima/koodi
app.factory('KoodistoKielet', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/kielivalikoima/koodi", {}, {
    get: {method: "GET", isArray: true}
  });
});

// YTJ tiedot yhden yrityksen osalta
// Esim: http://localhost:8180/organisaatio-service/rest/ytj/2397998-7
app.factory('YTJYritysTiedot', function($resource) {
    return $resource(SERVICE_URL_BASE + "ytj/:ytunnus", {ytunnus: "@ytunnus"}, {}, {
        get: {method: 'GET'}
    });
});

// YTJ haku nimen perusteella
// Esim: http://localhost:8180/organisaatio-service/rest/ytj/hae?nimi=yliopiston
app.factory('YTJYritystenTiedot', function($resource) {
    return $resource(SERVICE_URL_BASE + "ytj/hae", {}, {
        get: {method: 'GET', isArray: true}
    });
});

// postinumeroiden haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/posti/koodi
app.factory('KoodistoPosti', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/posti/koodi", {}, {
    get: {method: "GET", isArray: true}
  });
});
 
// Vuosiluokkien haku koodistopalvelulta
// Esim: https://localhost:8503/koodisto-service/rest/json/vuosiluokat/koodi
app.factory('KoodistoVuosiluokat', function($resource) {
return $resource(KOODISTO_URL_BASE + "json/vuosiluokat/koodi", {}, {
    get: {method: "GET", isArray: true}
  });
});

// Muokattavien yhteystietojen haku organisaatiopalvelulta
// Esim. https://localhost:8180/organisaatio-service/rest/organisaatio/yhteystietometadata?organisaatioTyyppi=Oppilaitos
app.factory('YhteystietoMetadata', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/yhteystietometadata/:orgTyyppi", { orgTyyppi: "@orgTyyppi"}, {
        get: {method: 'GET', isArray: true}
    });
});
