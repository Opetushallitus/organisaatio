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
var SERVICE_URL_BASE = SERVICE_URL_BASE || "";
var TEMPLATE_URL_BASE = TEMPLATE_URL_BASE || "";

var KOODISTO_URL_BASE = KOODISTO_URL_BASE || "";
var KOODISTO_ORGANISAATIOTYYPPI_KOODI = KOODISTO_ORGANISAATIOTYYPPI_KOODI  || "";
var KOODISTO_OPPILAITOSTYYPPI_KOODI = KOODISTO_OPPILAITOSTYYPPI_KOODI  || "";
var KOODISTO_MAAT_JA_VALTIOT = KOODISTO_MAAT_JA_VALTIOT  || "";
var KOODISTO_KIELI_KOODI = KOODISTO_KIELI_KOODI  || "";
var KOODISTO_VUOSILUOKAT = KOODISTO_VUOSILUOKAT  || "";
var KOODISTO_URI_KOODI = KOODISTO_URI_KOODI  || "";
var KOODISTO_POSTI_KOODI = KOODISTO_POSTI_KOODI  || "";
var KOODISTO_KUNTA_KOODI = KOODISTO_KUNTA_KOODI || "";
var KOODISTO_KOODI_HAE = KOODISTO_KOODI_HAE || "";
var KOODISTO_POSTI = KOODISTO_POSTI || "";
var KOODISTO_OPPILAITOKSENOPETUSKIELI = KOODISTO_OPPILAITOKSENOPETUSKIELI || "";
var KOODISTO_JARJESTAMISMUOTO = KOODISTO_JARJESTAMISMUOTO || "";
var KOODISTO_KASVATUSOPILLINEN_JARJESTELMA = KOODISTO_KASVATUSOPILLINEN_JARJESTELMA || "";
var KOODISTO_TOIMINNALLINEN_PAINOTUS = KOODISTO_TOIMINNALLINEN_PAINOTUS || "";
var KOODISTO_MAAT_JA_VALTIOT2 = KOODISTO_MAAT_JA_VALTIOT2 ||"";
var KOODISTO_VARHAISKASVATUKSEN_TOIMINTAMUODOT = KOODISTO_VARHAISKASVATUKSEN_TOIMINTAMUODOT || "";

var SESSION_KEEPALIVE_INTERVAL_IN_SECONDS = SESSION_KEEPALIVE_INTERVAL_IN_SECONDS || 30;
var MAX_SESSION_IDLE_TIME_IN_SECONDS = MAX_SESSION_IDLE_TIME_IN_SECONDS || 1800;
var ORGANISAATIO_REST_ORGAISAATIO_MAXINACTIVEINTERVAL = ORGANISAATIO_REST_ORGAISAATIO_MAXINACTIVEINTERVAL || "";

var ORGANISAATIO_REST_V3 = ORGANISAATIO_REST_V3 || "";
var ORGANISAATIO_REST_V4 = ORGANISAATIO_REST_V4 || "";
var ORGANISAATIO_REST_HAE = ORGANISAATIO_REST_HAE || "";
var ORGANISAATIO_REST_V3_BY_OID = ORGANISAATIO_REST_V3_BY_OID || "";
var ORGANISAATIO_REST_V4_BY_OID = ORGANISAATIO_REST_V4_BY_OID || "";

var ORGANISAATIO_REST_V2_HAE = ORGANISAATIO_REST_V2_HAE || "";
var ORGANISAATIO_REST_V4_HAE = ORGANISAATIO_REST_V4_HAE || "";
var ORGANISAATIO_REST_V3_RYHMAT = ORGANISAATIO_REST_V3_RYHMAT || "";
var ORGANISAATIO_REST_V2_MUOKKAAMONTA = ORGANISAATIO_REST_V2_MUOKKAAMONTA || "";
var ORGANISAATIO_REST_V2_OID_HISTORIA = ORGANISAATIO_REST_V2_OID_HISTORIA || "";
var ORGANISAATIO_REST_V4_OID_HISTORIA = ORGANISAATIO_REST_V4_OID_HISTORIA || "";
var ORGANISAATIO_REST_V2_OID_ORGANISAATIOSUHDE = ORGANISAATIO_REST_V2_OID_ORGANISAATIOSUHDE || "";
var ORGANISAATIO_REST_V2_HIERARKIA_HAE = ORGANISAATIO_REST_V2_HIERARKIA_HAE || "";
var ORGANISAATIO_REST_V4_HIERARKIA_HAE = ORGANISAATIO_REST_V4_HIERARKIA_HAE || "";
var ORGANISAATIO_REST_V2_PAIVITTAJA_HAE = ORGANISAATIO_REST_V2_PAIVITTAJA_HAE || "";
var ORGANISAATIO_REST_V2_NIMIHISTORIA_HAE = ORGANISAATIO_REST_V2_NIMIHISTORIA_HAE || "";

var ORGANISAATIO_REST_AUTH = ORGANISAATIO_REST_AUTH || "http://localhost:8180/organisaatio-service/rest/organisaatio/auth";
var ORGANISAATIO_REST_YTJ_YTUNNUS = ORGANISAATIO_REST_YTJ_YTUNNUS || "";
var ORGANISAATIO_REST_YTJ_HAE = ORGANISAATIO_REST_YTJ_HAE || "";
var ORGANISAATIO_REST_YHTEYSTIETOJENTYYPPI = ORGANISAATIO_REST_YHTEYSTIETOJENTYYPPI || "";
var ORGANISAATIO_REST_YHTEYSTIETOJENTYYPPI_BY_OID = ORGANISAATIO_REST_YHTEYSTIETOJENTYYPPI_BY_OID || "";
var ORGANISAATIO_REST_YTJ_LOKI = ORGANISAATIO_REST_YTJ_LOKI || "";
var ORGANISAATIO_REST_LISATIEDOT_BY_OID = ORGANISAATIO_REST_LISATIEDOT_BY_OID || "";
var ORGANISAATIO_REST_LISATIETOTYYPIT = ORGANISAATIO_REST_LISATIETOTYYPIT || "";
var ORGANISAATIO_REST_LISATIETOTYYPPI_NIMI = ORGANISAATIO_REST_LISATIETOTYYPPI_NIMI || "";
var ORGANISAATIO_REST_LISATIETOTYYPPI = ORGANISAATIO_REST_LISATIETOTYYPPI || "";

var OPPIJANUMEROREKISTERI_HENKILO_BY_OID = OPPIJANUMEROREKISTERI_HENKILO_BY_OID || "";
var KAYTTOOIKEUS_REST_RYHMA_BY_HENKILO_OID = KAYTTOOIKEUS_REST_RYHMA_BY_HENKILO_OID || "";

var OPPIJANUMEROREKISTERI_REST_HENKILO = OPPIJANUMEROREKISTERI_REST_HENKILO || "";

////////////
//
// Route configuration
//
////////////
app.config(function($routeProvider, $httpProvider, $locationProvider) {
    $httpProvider.interceptors.push('NoCacheInterceptor');

    $locationProvider.html5Mode({
        enabled: true
    });

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

    // lisatietojen tyypit ja yhteystietojen tyypit
    when('/yhteystietotyypit', {templateUrl: TEMPLATE_URL_BASE + 'tyyppienhallinta.html'}).

    // manage groups
    when('/organisaatiot/:parentoid/groups', {controller: 'RyhmatController', controllerAs: 'vm', templateUrl:TEMPLATE_URL_BASE + 'ryhmat.html'}).

    // add group
    when('/ryhmat/uusi', {controller: 'RyhmienHallintaController', templateUrl:TEMPLATE_URL_BASE + 'ryhmienhallinta.html', resolve: {
        ryhma: function ($route) {
            return {oid: null, parentOid: $route.current.params.parentOid, tyypit: ['Ryhma'], ryhmatyypit: [''], kayttoryhmat: ['']};
        }
    }}).

    // edit group
    when('/ryhmat/:oid', {controller: 'RyhmienHallintaController', templateUrl:TEMPLATE_URL_BASE + 'ryhmienhallinta.html', resolve: {
        ryhma: function (Organisaatio, $route) {
            return Organisaatio.get({oid: $route.current.params.oid}).$promise;
        }
    }}).

    //else
    otherwise({redirectTo:'/organisaatiot'});
});

// Konfiguroidaan DatePicker alkamaan viikon maanantaista (defaul = sunnuntai)
app.config(function(uibDatepickerConfig) {
    uibDatepickerConfig.startingDay = 1;
});

app.run(function($http, $cookies, OrganisaatioInitAuth, $location) {
    // Set headers. NOTE: init() sends auth messages so this needs to be done before that.
    $http.defaults.headers.common['clientSubSystemCode'] = "organisaatio.organisaatio-ui.frontend";
    if($cookies.get('CSRF')) {
        $http.defaults.headers.common['CSRF'] = $cookies.get('CSRF');
    }

    // Tehdään autentikoitu get servicelle
    // Näin kierretään ongelma: "CAS + ensimmäinen autentikoitia vaativa POST kutsu"
    OrganisaatioInitAuth.init();
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
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/v3/1.2.246.562.10.23198065932
app.factory('Organisaatio', function ($resource) {
    return $resource(ORGANISAATIO_REST_V4_BY_OID + "?includeImage=true", {oid: "@oid"}, {
        get: {method: "GET"},
        update: {method: "PUT"},
        delete: {method: "DELETE"}
    });
});

// Organisaation historian haku
// Esim: https://localhost:8180/organisaatio-service/rest/organisaatio/v2/1.2.246.562.10.68986346941/historia
app.factory('OrganisaatioHistoria', function ($resource) {
    return $resource(ORGANISAATIO_REST_V4_OID_HISTORIA, {oid: "@oid"}, {
        get: {method: "GET"}
    });
});


// Organisaation siiro puussa
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/v2/1.2.246.562.10.23198065932/organisaatiosuhde
app.factory('OrganisaatioSiirto', function ($resource) {
    return $resource(ORGANISAATIO_REST_V2_OID_ORGANISAATIOSUHDE, {oid: "@oid"}, {
        post: {method: "POST"}
    });
});

// Organisaation luonti organisaatiopalveluun
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/v3
app.factory('UusiOrganisaatio', function ($resource) {
    return $resource(ORGANISAATIO_REST_V4, {}, {
        create: {method: "POST"}
    });
});

// Organisaatioiden haku puunäkymää varten organisaatiopalvelulta
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/hae?searchstr=lukio&lakkautetut=true
app.factory('Organisaatiot', function($resource) {
    return $resource(ORGANISAATIO_REST_V4_HIERARKIA_HAE, {}, {
        get: {method: 'GET'}
    });
});

// Organisaatioiden haku ilman hierrarkiaa
app.factory('OrganisaatiotFlat', function ($resource) {
    return $resource(ORGANISAATIO_REST_V4_HAE, {}, {
        get: {method: 'GET'}
    });
});

// Autentikoitu get kutsu organisaatiopalveluun
// Esim: http://localhost:8180/organisaatio-service/rest/organisaatio/auth
app.factory('OrganisaatioAuthGET', function ($resource) {
    return $resource(ORGANISAATIO_REST_AUTH, {}, {
        get: {method: "GET"}
    });
});


// YTJ tiedot yhden yrityksen osalta organisaatiopalvelun kautta
// Esim: http://localhost:8180/organisaatio-service/rest/ytj/2397998-7
app.factory('YTJYritysTiedot', function ($resource) {
    return $resource(ORGANISAATIO_REST_YTJ_YTUNNUS, {ytunnus: "@ytunnus"}, {}, {
        get: {method: 'GET', withCredentials: true}
    });
});

// YTJ tietojen haku nimen perusteella
// Esim: http://localhost:8180/organisaatio-service/rest/ytj/hae?nimi=yliopiston
app.factory('YTJYritystenTiedot', function ($resource) {
    return $resource(ORGANISAATIO_REST_YTJ_HAE, {}, {
        get: {method: 'GET', withCredentials: true, isArray: true}
    });
});


// Muokattavien yhteystietojen haku organisaatiopalvelulta
// Esim. https://localhost:8180/organisaatio-service/rest/yhteystietojentyyppi
app.factory('Yhteystietojentyyppi', function ($resource) {
    return $resource(ORGANISAATIO_REST_YHTEYSTIETOJENTYYPPI, {}, {
        get: {method: 'GET', isArray: true},
        post: {method: 'POST'},
        put: {method: 'PUT'}
    });
});

// Yhteystietotyypin poisto organisaatiopalvelulta
app.factory('YhteystietojentyypinPoisto', function ($resource) {
    return $resource(ORGANISAATIO_REST_YHTEYSTIETOJENTYYPPI_BY_OID + "?force=:force", {
        oid: "@oid",
        force: "@force"
    }, {
        delete: {method: 'DELETE'}
    });
});

// Virkailijoiden haku organisaatiolle oppijanumerorekisteristä
// Esim. https://localhost:8509/oppijanumerorekisteri-service/henkilo?tyyppi=VIRKAILIJA&passivoitu=false&duplikaatti=false&count=200&organisaatioOids=1.2.246.562.10.67019405611
app.factory('HenkiloVirkailijat', function ($resource) {
    return $resource(OPPIJANUMEROREKISTERI_REST_HENKILO + "?tyyppi=VIRKAILIJA&passivoitu=false&duplikaatti=false&count=200&organisaatioOids=:oid", {oid: "@oid"}, {
        get: {method: 'GET', withCredentials: true}
    });
});

// Henkilön haku oppijanumerorekisteristä
// Esim. https://localhost:8509/oppijanumerorekisteri-service/henkilo/1.2.246.562.24.91121139885
app.factory('Henkilo', function ($resource) {
    return $resource(OPPIJANUMEROREKISTERI_HENKILO_BY_OID, {hlooid: "@hlooid"}, {
        get: {method: 'GET', withCredentials: true}
    });
});

// Käyttöoikeuden haku henkilölle organisaatiossa
// Esim. https://localhost:8510/kayttooikeus-service/kayttooikeusryhma/henkilo/1.2.246.562.24.91121139885?ooid=1.2.246.562.10.82388989657
app.factory('HenkiloKayttooikeus', function ($resource) {
    return $resource(KAYTTOOIKEUS_REST_RYHMA_BY_HENKILO_OID + "?ooid=:orgoid", {
        hlooid: "@hlooid",
        orgoid: "@orgoid"
    }, {
        get: {method: 'GET', withCredentials: true, isArray: true}
    });
});

// Ryhmien haku organisaatioplavelulta
// Esim. https://itest-virkailija.oph.ware.fi/organisaatio-service/rest/organisaatio/v3/ryhmat
app.factory('Ryhmat', function($resource) {
    return $resource(ORGANISAATIO_REST_V3_RYHMAT, {}, {
        get: {method: 'GET', isArray: true}
    });
});

// Viimeisimman päivityksen tietojen haku organisaatioplavelulta
// Esim. https://itest-virkailija.oph.ware.fi/organisaatio-service/rest/organisaatio/v2/1.2.246.562.10.00000000001/paivittaja
app.factory('Paivittaja', function ($resource) {
    return $resource(ORGANISAATIO_REST_V2_PAIVITTAJA_HAE, {oid: "@oid"}, {
        get: {method: 'GET'}
    });
});

// Usean organisaation voimassaolon muokkaus yhdellä kertaa
app.factory('Muokkaamonta', function ($resource) {
    return $resource(ORGANISAATIO_REST_V2_MUOKKAAMONTA, {}, {
        put: {method: 'PUT'}
    });
});

// Organisaation historiatietojen haku
app.factory('Historia', function ($resource) {
    return $resource(ORGANISAATIO_REST_V4_OID_HISTORIA, {oid: "@oid"}, {
        get: {method: 'GET', isArray: true}
    });
});

app.factory('YtjLoki', function ($resource) {
    return $resource(ORGANISAATIO_REST_YTJ_LOKI, {alkupvm: "@alkupvm", loppupvm: "@loppupvm"}, {
        get: {method: 'GET', withCredentials: true, isArray: true}
    });
});

app.factory('MahdollisetLisatiedot', function ($resource) {
    return $resource(ORGANISAATIO_REST_LISATIEDOT_BY_OID, {oid: "@oid"}, {
        get: {method: 'GET', isArray: true}
    });
});

app.factory('Lisatietotyypit', function ($resource) {
   return $resource(ORGANISAATIO_REST_LISATIETOTYYPIT, {}, {
       get: {method: 'GET', isArray: true}
   });
});

app.factory('LisatietotyyppiNimi', function ($resource) {
    return $resource(ORGANISAATIO_REST_LISATIETOTYYPPI_NIMI, {nimi: '@nimi'}, {
        delete: {method: 'DELETE', withCredentials: true},
        get: {method: 'GET'}
    });
});

app.factory('Lisatietotyyppi', function ($resource) {
    return $resource(ORGANISAATIO_REST_LISATIETOTYYPPI, {}, {
        post: {method: 'POST', withCredentials: true}
    });
});
