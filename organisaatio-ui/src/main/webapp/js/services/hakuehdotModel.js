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

app.factory('HakuehdotModel', function($q, $filter, $log, $injector,
                                       AuthService, Alert,
                                       KoodistoClient, KoodistoKoodi) {

    $log = $log.getInstance("HakuehdotModel");
    var loadingService = $injector.get('LoadingService');

    var model = {
        organisaatioRajausVisible: false,
        organisaatioRajaus: false,
        rajatutOrganisaatiot: [],
        rajatutOrganisaatiotStr: "",
        refreshed: false,
        nimiTaiTunnus: "",
        kunta: "",
        organisaatiotyyppi: "",
        oppilaitostyyppi: "",
        aktiiviset: true,
        suunnitellut: true,
        lakkautetut: false,
        paikkakunnat: [],
        organisaatiotyypit: [],
        oppilaitostyypit: [],

        /**
         * Pakotetaan hakuehtoihin tarvittavien koodistotietojen päivitys.
         */
        refresh: function() {
            $log.log('refresh()');
            model.refreshed = false;
            model.refreshIfNeeded();
        },

        /**
         * Tarkistetaan onko hakuehdot tyhjät.
         *
         * @returns {Boolean} true, jos hakuehdot tyhjät
         */
        isEmpty: function() {
            if (model.nimiTaiTunnus ||
                    model.kunta ||
                    model.organisaatiotyyppi ||
                    model.oppilaitostyyppi ||
                    model.organisaatioRajaus) {
                return false;
            }
            return true;
        },

        /**
         * Tarkistetaan onko hakuehdoissa olevat organisaation tilat valideja.
         * Jotta haun tuloksena saadaan organisaatioita, pitää haussa olla joku
         * organisaation tiloista mukana.
         *
         * @returns {Boolean} true, jos validi
         */
        isTilaValid: function() {
            if (!model.aktiiviset && !model.suunnitellut && !model.lakkautetut) {
                return false;
            }
            return true;
        },

        refreshIfNeeded: function() {
            $log.log('refreshIfNeeded()');
            if (model.refreshed === false) {
                model.refreshed = true;
                KoodistoClient.koodistoPaikkakunnat.get({onlyValidKoodis:true}, function(result) {
                    result.forEach(function(kuntaKoodi) {
                        var paikkakunta = {"uri": kuntaKoodi.koodiUri,
                            "arvo":kuntaKoodi.koodiArvo};

                        paikkakunta.nimi = KoodistoKoodi.getLocalizedName(kuntaKoodi);
                        model.paikkakunnat.push(paikkakunta);
                    });
                    $log.log('paikkakunnat: ' +  model.paikkakunnat.length);
                },
                // Error case
                function(response) {
                    loadingService.onErrorHandled(response);
                    $log.error("KoodistoPaikkakunnat response: " + response.status);
                    Alert.add("error", $filter('i18n')("Organisaatiot.koodistoVirhe", ""), true);
                    model.refreshed = false;
                });

                KoodistoClient.koodistoOrganisaatiotyypit.get({onlyValidKoodis:true}, function(result) {
                    result.forEach(function(orgTyyppiKoodi) {
                        var organisaatioTyyppi = {"uri": orgTyyppiKoodi.koodiUri,
                            "arvo":orgTyyppiKoodi.koodiArvo};

                        organisaatioTyyppi.nimi   = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                        organisaatioTyyppi.nimifi = KoodistoKoodi.getLangName(orgTyyppiKoodi, 'FI')
                        model.organisaatiotyypit.push(organisaatioTyyppi);
                    });
                    $log.log('organisaatiotyypit: ' +  model.organisaatiotyypit.length);
                },
                // Error case
                function(response) {
                    loadingService.onErrorHandled(response);
                    $log.error("KoodistoPaikkakunnat response: " + response.status);
                    Alert.add("error", $filter('i18n')("Organisaatiot.koodistoVirhe", ""), true);
                    model.refreshed = false;
                });

                KoodistoClient.koodistoOppilaitostyypit.get({onlyValidKoodis:true}, function(result) {
                    result.forEach(function(oplTyyppiKoodi) {
                        var oppilaitosTyyppi = {"uri": oplTyyppiKoodi.koodiUri,
                            "arvo":oplTyyppiKoodi.koodiArvo};

                        oppilaitosTyyppi.nimi = KoodistoKoodi.getLocalizedName(oplTyyppiKoodi);
                        model.oppilaitostyypit.push(oppilaitosTyyppi);
                    });
                    $log.log('oppilaitostyypit: ' +  model.oppilaitostyypit.length);
                },
                // Error case
                function(response) {
                    loadingService.onErrorHandled(response);
                    $log.error("KoodistoPaikkakunnat response: " + response.status);
                    Alert.add("error", $filter('i18n')("Organisaatiot.koodistoVirhe", ""), true);
                    model.refreshed = false;
                });
            }
        },

        resetTarkemmatEhdot: function () {
            $log.log('resetTarkemmatEhdot()');
            model.kunta= "";
            model.organisaatiotyyppi= "";
            model.oppilaitostyyppi= "";
            model.aktiiviset= true;
            model.suunnitellut= true;
            model.lakkautetut= false;
        },

        resetAll: function () {
            $log.log('resetAll()');
            model.nimiTaiTunnus="";
            model.kunta= "";
            model.organisaatiotyyppi= "";
            model.oppilaitostyyppi= "";
            model.aktiiviset= true;
            model.suunnitellut= true;
            model.lakkautetut= false;
        },

        init: function () {
            var deferred = $q.defer();
            AuthService.getOrganizations("APP_ORGANISAATIOHALLINTA").then(function(organisations){
                "use strict";
                $log.debug("Käyttäjän organisaatiot:" + organisations);

                // Jos OPH käyttäjä, niin ei näytetä organisaatiorajausta
                if(!organisations || organisations.indexOf(ROOT_ORGANISAATIO_OID) > -1) {
                    model.organisaatioRajausVisible = false;
                    model.organisaatioRajaus = false;
                    model.rajatutOrganisaatiot = [];
                    model.rajatutOrganisaatiotStr = "";
                }
                else if (angular.equals(organisations,model.rajatutOrganisaatiot)) {
                    // Sivulle tultiin takaisin ja vanhat rajatut organisaatio käytössä
                }
                else {
                    model.organisaatioRajausVisible = true;
                    model.organisaatioRajaus = true;
                    model.rajatutOrganisaatiot = organisations;
                    model.rajatutOrganisaatiotStr = "";
                }
                deferred.resolve();
            });
            return deferred.promise;
        }

        };

    return model;
});
