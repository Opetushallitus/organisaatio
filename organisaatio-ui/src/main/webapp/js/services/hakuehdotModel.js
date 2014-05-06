app.factory('HakuehdotModel', function($q, $filter, $log, AuthService, Alert,
                                       KoodistoPaikkakunnat,
                                       KoodistoOrganisaatiotyypit,
                                       KoodistoOppilaitostyypit,
                                       KoodistoKoodi) {
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
        tila: "kaikki",
        kaikki: "kaikki",
        vainAktiiviset: "vainAkviiviset",
        vainLakkautetut: "vainLakkautetut",
        paikkakunnat: [],
        organisaatiotyypit: [],
        oppilaitostyypit: [],

        refresh: function() {
            $log.log('refresh()');
            model.refreshed = false;
            model.refreshIfNeeded();
        },

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
        
        refreshIfNeeded: function() {
            $log.log('refreshIfNeeded()');
            if (model.refreshed === false) {
                model.refreshed = true;
                KoodistoPaikkakunnat.get({onlyValidKoodis:true}, function(result) {
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
                    $log.error("KoodistoPaikkakunnat response: " + response.status);
                    Alert.add("error", $filter('i18n')("Organisaatiot.koodistoVirhe", ""), true);
                    model.refreshed = false;
                });
                
                KoodistoOrganisaatiotyypit.get({onlyValidKoodis:true}, function(result) {
                    result.forEach(function(orgTyyppiKoodi) {
                        var organisaatioTyyppi = {"uri": orgTyyppiKoodi.koodiUri,
                            "arvo":orgTyyppiKoodi.koodiArvo};

                        organisaatioTyyppi.nimi = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                        model.organisaatiotyypit.push(organisaatioTyyppi);
                    });
                    $log.log('organisaatiotyypit: ' +  model.organisaatiotyypit.length);
                }, 
                // Error case
                function(response) {
                    $log.error("KoodistoPaikkakunnat response: " + response.status);
                    Alert.add("error", $filter('i18n')("Organisaatiot.koodistoVirhe", ""), true);
                    model.refreshed = false;
                });
                
                KoodistoOppilaitostyypit.get({onlyValidKoodis:true}, function(result) {
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
            model.tila = "kaikki";
        },

        resetAll: function () {
            $log.log('resetAll()');
            model.nimiTaiTunnus="";
            model.kunta= "";
            model.organisaatiotyyppi= "";
            model.oppilaitostyyppi= "";
            model.tila = "kaikki";
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
