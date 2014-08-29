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

        // Tyhjenneteään mallin tiedot
        clear: function() {
            this.nimihistoria = [];
            this.uusinNimi = {};
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
            $log.debug('Ajastettu muutos: ' + ajastettuMuutos);

            return ajastettuMuutos;
        },

        // Init NimiHistoriaModel uudella nimihistorialla
        init: function(nimihistoria) {
            $log.log('init()');

            this.nimihistoria = nimihistoria;
            this.uusinNimi = this.getUusinNimi(nimihistoria);
            this.ajastettuMuutos = this.isAjastettuMuutos(this.uusinNimi);
        }
    };

    return model;
});



