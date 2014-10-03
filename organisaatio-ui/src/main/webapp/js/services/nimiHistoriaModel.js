app.factory('NimiHistoriaModel', function($log) {
//    emptyNimi = {
//        "nimi" : {
//            "fi" : "",
//            "sv" : "",
//            "en" : ""
//        },
//        "alkuPvm" : ""
//    };

    var model = {
        nimihistoria : [],
        uusinNimi : {},
        ajastettuMuutos : false,
        parentNimi : null,
        currentNimi : {},

        // Nimi rakenne stringiksi (ei alkupäivämäärää)
        nimiToString: function(nimi) {
            var nimiStr = '';
            if (nimi && 'nimi' in nimi) {
                ['fi', 'sv', 'en'].forEach(function(key) {
                    if (key in nimi.nimi)
                        nimiStr += '[' + key + '] ' + nimi.nimi[key];
                });
            }
            return nimiStr;
        },

        // Tyhjenneteään mallin tiedot
        clear: function() {
            $log.debug('NimiHistoriaModel::clear()');

            this.nimihistoria = [];
            this.uusinNimi = {};
            this.currentNimi = {};
            this.ajastettuMuutos = false;
        },

        // Haetaan Nimihistorian uusin nimi
        getUusinNimi: function() {
            var nimi = null;
            if (this.nimihistoria.length > 0) {
                nimi = this.nimihistoria[0];
            }
            for(var i=0; i < this.nimihistoria.length; i++) {
                if (moment(this.nimihistoria[i].alkuPvm).isAfter(moment(nimi.alkuPvm))) {
                    nimi = this.nimihistoria[i];
                }
            }

            $log.debug('NimiHistoriaModel::getUusinNimi() ' + model.nimiToString(nimi));
            return nimi;
        },

        // Haetaan nimihistorian sisältämä nykyinen nimi (ei siis tuleva ajastettu nimi)
        getCurrentNimi: function() {
            var nimi = null;
            if (this.nimihistoria.length > 0) {
                nimi = this.nimihistoria[0];
            }
            for(var i=0; i < this.nimihistoria.length; i++) {
                if (moment(this.nimihistoria[i].alkuPvm).isAfter(moment(nimi.alkuPvm)) &&
                        moment(this.nimihistoria[i].alkuPvm).isBefore(moment())) {
                    nimi = this.nimihistoria[i];
                }
            }

            $log.debug('NimiHistoriaModel::getCurrentNimi() ' + model.nimiToString(nimi));
            return nimi;
        },

        // Tarkastetaan onko annetun nimen muutos ajastus --> siis alkupvm tulevaisuudessa
        isAjastettuMuutos: function(nimi) {
            var ajastettuMuutos = false;
            if (nimi !== null) {
                if('alkuPvm' in nimi &&
                        moment(nimi.alkuPvm).isValid() &&
                        moment(nimi.alkuPvm).isAfter(moment())) {
                    ajastettuMuutos = true;
                }
            }

            $log.debug('NimiHistoriaModel::isAjastettuMuutos() ' + model.nimiToString(nimi) + " = " + ajastettuMuutos);
            return ajastettuMuutos;
        },

        // Init NimiHistoriaModel uudella nimihistorialla
        init: function(nimihistoria, parentNimi) {
            $log.log('NimiHistoriaModel::init()');
            this.parentNimi = parentNimi || null;
            this.nimihistoria = nimihistoria;
            this.uusinNimi = angular.copy(this.getUusinNimi(nimihistoria));
            this.ajastettuMuutos = this.isAjastettuMuutos(this.uusinNimi);
            this.currentNimi = this.getCurrentNimi(nimihistoria);
            if (parentNimi && model.currentNimi) {
                // Poistetaan parent prefix nimestä
                ['fi', 'sv', 'en'].forEach(function(key) {
                    if (model.currentNimi.nimi[key] && model.parentNimi[key]) {
                        model.currentNimi.nimi[key] = model.currentNimi.nimi[key].replace(model.parentNimi[key] + ", ", "");
                    }
                });
            }
            $log.log("NimiHistoriaModel::ini() done");
        },

        accept: function() {
            if (this.parentNimi) {
                // Lisätään parentnimi prefix
                ['fi', 'sv', 'en'].forEach(function(key) {
                    if (model.uusinNimi && model.uusinNimi.nimi[key] && model.parentNimi[key]) {
                        $log.log(model.nimihistoria);
                        if (!model.uusinNimi.nimi[key].match("^" + model.parentNimi[key] + ", ") &&
                            !model.uusinNimi.nimi[key].match("^" + model.parentNimi[key] + "$")) {
                            model.uusinNimi.nimi[key] = model.parentNimi[key] + ", " + model.uusinNimi.nimi[key];
                        }
                    }
                    if (model.currentNimi && model.currentNimi.nimi[key] && model.parentNimi[key]) {
                        if (!model.currentNimi.nimi[key].match("^" + model.parentNimi[key] + ", ") &&
                            !model.currentNimi.nimi[key].match("^" + model.parentNimi[key] + "$")) {
                            model.currentNimi.nimi[key] = model.parentNimi[key] + ", " + model.currentNimi.nimi[key];
                        }
                    }
                });
            }
        }
    };

    return model;
});



