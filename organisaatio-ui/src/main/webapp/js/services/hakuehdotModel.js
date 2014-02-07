app.factory('HakuehdotModel', function($filter, $log, Alert,
                                       KoodistoPaikkakunnat,
                                       KoodistoOrganisaatiotyypit,
                                       KoodistoOppilaitostyypit,
                                       KoodistoKoodi) {
    var model = {
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
                    model.oppilaitostyyppi) {
                return false;
            }   
            return true;
        },
        
        refreshIfNeeded: function() {
            $log.log('refreshIfNeeded()');
            if (model.refreshed === false) {
                model.refreshed = true;
                KoodistoPaikkakunnat.get(function(result) {
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
                
                KoodistoOrganisaatiotyypit.get(function(result) {
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
                
                KoodistoOppilaitostyypit.get(function(result) {
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
        }
    };

    return model;
});
