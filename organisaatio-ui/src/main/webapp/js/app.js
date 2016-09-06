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

var app = angular.module('organisaatio',
['ngResource',
    'Loading',
    'Koodisto',
    'ngRoute',
    'Logging',
    'Localisation',
    'ui.bootstrap',
    'ngSanitize',
    'ui.tinymce',
    'ui.select',
    'ngCookies',
    'ngIdle']);

app.config(function() {
    tinyMCE.baseURL = '../jslib';
});

app.filter('fixHttpLink',function () {
    return function (text) {
        var proto = text.split("://");
        return (proto.length>1 ? proto[0] : "http") + "://" + proto[proto.length-1];
    };
});

app.filter('decodeAmp',function () {
    return function (text) {
        if (text===null) {
            return null;
        }
        return text.replace(/&amp;/g, '&');
    };
});

////////////
//
// Configuration from config/properties files
//
////////////
var UI_URL_BASE = UI_URL_BASE || "http://localhost:8180/organisaatio-ui/";
var SERVICE_URL_BASE = SERVICE_URL_BASE || "";
var TEMPLATE_URL_BASE = TEMPLATE_URL_BASE || "";
var KOODISTO_URL_BASE = KOODISTO_URL_BASE || "";
var LOKALISAATIO_URL_BASE = LOKALISAATIO_URL_BASE || "";
var AUTHENTICATION_URL_BASE = AUTHENTICATION_URL_BASE || "";
var ROOT_ORGANISAATIO_OID = ROOT_ORGANISAATIO_OID || "";
var CAS_ME_URL = CAS_ME_URL || "/cas/me";
var SESSION_KEEPALIVE_INTERVAL_IN_SECONDS = SESSION_KEEPALIVE_INTERVAL_IN_SECONDS || 30;
var MAX_SESSION_IDLE_TIME_IN_SECONDS = MAX_SESSION_IDLE_TIME_IN_SECONDS || 1800;

////////////
//
// Route configuration
//
////////////
app.config(function($routeProvider, $httpProvider) {
    $httpProvider.interceptors.push('NoCacheInterceptor');

    $routeProvider.

    // front page
    when('/organisaatiot', {controller: 'OrganisaatioTreeController', templateUrl:TEMPLATE_URL_BASE + 'organisaatiot.html'}).

    // view notifications
    when('/organisaatiot/ilmoitukset', {controller: 'YtjIlmoituksetController', templateUrl:TEMPLATE_URL_BASE + 'ytjilmoitukset.html'}).

    // read one
    when('/organisaatiot/:oid', {controller: 'OrganisaatioController', templateUrl:TEMPLATE_URL_BASE + 'organisaationtarkastelu.html'}).

    // edit one
    when('/organisaatiot/:oid/edit', {controller: 'OrganisaatioController', templateUrl:TEMPLATE_URL_BASE + 'organisaationmuokkaus.html'}).

    // create new
    when('/organisaatiot/:parentoid/new', {controller: 'OrganisaatioController', templateUrl:TEMPLATE_URL_BASE + 'organisaationmuokkaus.html'}).

    // yhteystietojen tyypit
    when('/yhteystietotyypit', {controller: 'YhteystietojentyyppiController', templateUrl:TEMPLATE_URL_BASE + 'yhteystietojentyyppi.html'}).

    // manage groups
    when('/organisaatiot/:parentoid/groups', {controller: 'RyhmienHallintaController', templateUrl:TEMPLATE_URL_BASE + 'ryhmienhallinta.html'}).

        //else
    otherwise({redirectTo:'/organisaatiot'});
});

// Konfiguroidaan DatePicker alkamaan viikon maanantaista (defaul = sunnuntai)
app.config(function(uibDatepickerConfig) {
    uibDatepickerConfig.startingDay = 1;
});

app.run(function($http, $cookies, OrganisaatioInitAuth, $routeParams) {
    // Set headers. NOTE: init() sends auth messages so this needs to be done before that.
    $http.defaults.headers.common['clientSubSystemCode'] = "organisaatio.organisaatio-ui.frontend";
    if($cookies.get('CSRF')) {
        $http.defaults.headers.common['CSRF'] = $cookies.get('CSRF');
    }

    // Tehdään autentikoitu get servicelle
    // Näin kierretään ongelma: "CAS + ensimmäinen autentikoitia vaativa POST kutsu"
    OrganisaatioInitAuth.init();

    console.log($("input", "urlHash").val());
});

////////////
//
// Services
//
////////////

// Esimerkki: Alert.add("warning", $filter('i18n')("YritysValinta.virheViesti", ""), true);
app.factory('Alert', ['$rootScope', '$timeout', function($rootScope, $timeout) {
        var alertService;
        $rootScope.alerts = [];
        alertService = {
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
        $rootScope.$on('$locationChangeStart', function() {
            alertService.clear();
        });
        return alertService;
    }
]);


app.factory('OrganisaatioInitAuth', ['$log', '$timeout', '$filter', '$injector',
                                     'Alert', 'OrganisaatioAuthGET',
    function($log, $timeout, $filter, $injector, Alert, OrganisaatioAuthGET) {
        $log = $log.getInstance("OrganisaatioInitAuth");

        var loadingService = $injector.get('LoadingService');

        return  {
            init: function() {
                OrganisaatioAuthGET.get({}, function(result) {
                    $log.log("Organisaatio Auth Init.");
                },
                // Error case, ensimmäinen yritys
                function(response) {
                    loadingService.onErrorHandled(response);
                    $timeout(function() {
                        OrganisaatioAuthGET.get({}, function(result) {
                            $log.log("Organisaatio Auth Init, second try.");
                        },
                        // Error case, toinen yritys
                        function(response) {
                            loadingService.onErrorHandled(response);
                            Alert.add("error", $filter('i18n')("Organisaatiot.yleinenVirhe", ""), true);
                            $log.error("Organisaatio Auth Init failed, response: " + response.status);
                        });
                    }, 1000);
                });
            }
        };
    }
]);

// http://stackoverflow.com/questions/16098430/angular-ie-caching-issue-for-http#19771501
// Cachen voi sallia yksittäisille URLeille parametrilla '?allowCache=true'
app.factory('NoCacheInterceptor', function() {
    return {
        request: function(config) {
            if (config.method && config.method === 'GET' &&
                    config.url.indexOf('html') === -1 &&
                    config.url.indexOf("?allowCache=true") === -1 &&
                    (config.url.indexOf(SERVICE_URL_BASE) !== -1 ||
                            (config.url.indexOf(KOODISTO_URL_BASE) !== -1))) {
                var separator = config.url.indexOf('?') === -1 ? '?' : '&';
                config.url = config.url + separator + 'noCache=' + new Date().getTime();
            }
            return config;
        }
    };
});

////////////
//
// REST resources
//
////////////

// Organisaation haku / päivitys organisaatiopalveluun
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/1.2.246.562.10.23198065932
app.factory('Organisaatio', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/:oid?includeImage=true", {oid: "@oid"}, {
        get: {method:   "GET"},
        post:{method:   "POST"},
        delete:{method: "DELETE"}
    });
});

// Organisaation historian haku
// Esim: https://localhost:8180/organisaatio-service/rest/organisaatio/v2/1.2.246.562.10.68986346941/historia
app.factory('OrganisaatioHistoria', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/v2/:oid/historia", {oid: "@oid"}, {
        get: {method:   "GET"}
    });
});


// Organisaation siiro puussa
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/1.2.246.562.10.23198065932/organisaatiosuhde
app.factory('OrganisaatioSiirto', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/v2/:oid/organisaatiosuhde", {oid: "@oid"}, {
        post:{method:   "POST"}
    });
});

// Organisaation luonti organisaatiopalveluun
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/1.2.246.562.10.23198065932
app.factory('UusiOrganisaatio', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio", {}, {
        put: {method:   "PUT"}
    });
});

// Aliorganisaatioiden haku organisaatiopalvelulta
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/hae?oidRestrictionList=1.2.246.562.10.59347432821
app.factory('Aliorganisaatiot', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/hae?oidRestrictionList=:oid", {oid: "@oid"}, {
        get: {method: "GET"}
    });
});

// Organisaatioiden haku puunäkymää varten organisaatiopalvelulta
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/hae?searchstr=lukio&lakkautetut=true
app.factory('Organisaatiot', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/v2/hierarkia/hae", {}, {
        get: {method: 'GET'}
    });
});

// Organisaatioiden haku ilman hierrarkiaa
app.factory('OrganisaatiotFlat', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/v2/hae", {}, {
        get: {method: 'GET'}
    });
});

// Autentikoitu get kutsu organisaatiopalveluun
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/auth
app.factory('OrganisaatioAuthGET', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/auth", {}, {
        get: {method: "GET"}
    });
});


// YTJ tiedot yhden yrityksen osalta organisaatiopalvelun kautta
// Esim: http://localhost:8180/organisaatio-service/rest/ytj/2397998-7
app.factory('YTJYritysTiedot', function($resource) {
    return $resource(SERVICE_URL_BASE + "ytj/:ytunnus", {ytunnus: "@ytunnus"}, {}, {
        get: {method: 'GET', withCredentials : true}
    });
});

// YTJ tietojen haku nimen perusteella
// Esim: http://localhost:8180/organisaatio-service/rest/ytj/hae?nimi=yliopiston
app.factory('YTJYritystenTiedot', function($resource) {
    return $resource(SERVICE_URL_BASE + "ytj/hae", {}, {
        get: {method: 'GET', withCredentials : true, isArray: true}
    });
});


// Muokattavien yhteystietojen haku organisaatiopalvelulta
// Esim. https://localhost:8180/organisaatio-service/rest/yhteystietojentyyppi
app.factory('Yhteystietojentyyppi', function($resource) {
    return $resource(SERVICE_URL_BASE + "yhteystietojentyyppi", {}, {
        get: {method: 'GET',isArray: true},
        post: {method: 'POST'},
        put: {method: 'PUT'}
    });
});

// Yhteystietotyypin poisto organisaatiopalvelulta
app.factory('YhteystietojentyypinPoisto', function($resource) {
    return $resource(SERVICE_URL_BASE + "yhteystietojentyyppi/:oid?force=:force", { oid: "@oid", force: "@force" }, {
        delete: {method: 'DELETE'}
    });
});

// Virkailijoiden haku organisaatiolle käyttäjähallinnasta
// Esim. https://localhost:8508/authentication-service/resources/henkilo?count=200&ht=VIRKAILIJA&index=0&org=1.2.246.562.10.67019405611
app.factory('HenkiloVirkailijat', function($resource) {
    return $resource(AUTHENTICATION_URL_BASE + "henkilo?count=200&ht=VIRKAILIJA&index=0&org=:oid", { oid: "@oid"}, {
        get: {method: 'GET', withCredentials : true}
    });
});

// Henkilön haku käyttäjähallinnasta
// Esim. https://localhost:8508/authentication-service/resources/henkilo/1.2.246.562.24.91121139885
app.factory('Henkilo', function($resource) {
    return $resource(AUTHENTICATION_URL_BASE + "henkilo/:hlooid", { hlooid: "@hlooid"}, {
        get: {method: 'GET', withCredentials : true}
    });
});

// Käyttöoikeuden haku henkilölle organisaatiossa
// Esim. https://localhost:8508/authentication-service/resources/kayttooikeusryhma/henkilo/1.2.246.562.24.91121139885?ooid=1.2.246.562.10.82388989657
app.factory('HenkiloKayttooikeus', function($resource) {
    return $resource(AUTHENTICATION_URL_BASE + "kayttooikeusryhma/henkilo/:hlooid?ooid=:orgoid", { hlooid: "@hlooid", orgoid: "@orgoid"}, {
        get: {method: 'GET', withCredentials : true, isArray: true}
    });
});

// Ryhmien haku organisaatioplavelulta
// Esim. https://itest-virkailija.oph.ware.fi/organisaatio-service/rest/organisaatio/1.2.246.562.10.00000000001/ryhmat
app.factory('Ryhmat', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/v2/ryhmat", {}, {
        get: {method: 'GET', isArray: true}
    });
});

// Viimeisimman päivityksen tietojen haku organisaatioplavelulta
// Esim. https://itest-virkailija.oph.ware.fi/organisaatio-service/rest/organisaatio/v2/1.2.246.562.10.00000000001/paivittaja
app.factory('Paivittaja', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/v2/:oid/paivittaja", {oid: "@oid"}, {
        get: {method: 'GET'}
    });
});

// Nimihistorian haku organisaatioplavelulta
// Lisäksi operaatiot: uuden nimen luonti, vanhan päivitys ja ajastetun nimen poistaminen
// Esim. http://localhost:8180/organisaatio-service/rest/organisaatio/v2/1.2.246.562.10.00000000001/nimet
app.factory('Nimet', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/v2/:oid/nimet/:alkuPvm", {oid: "@oid", alkuPvm: "@alkuPvm"}, {
        get: {method: 'GET', isArray: true},
        post: {method: 'POST'},
        put: {method: 'PUT'},
        delete: {method: 'DELETE'}
    });
});

// Usean organisaation voimassaolon muokkaus yhdellä kertaa
app.factory('Muokkaamonta', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/v2/muokkaamonta", {}, {
        put: {method: 'PUT'}
    });
});

// Organisaation historiatietojen haku
app.factory('Historia', function($resource) {
    return $resource(SERVICE_URL_BASE + "organisaatio/v2/:oid/historia", {oid: "@oid"}, {
        get: {method: 'GET', isArray: true}
    });
});

app.factory('YtjLoki', function($resource) {
    return $resource(SERVICE_URL_BASE + "ytjpaivitysloki/aikavali", {alkupvm: "@alkupvm", loppupvm: "@loppupvm"}, {
        get: {method: 'GET', withCredentials: true, isArray: true}
    });
});
