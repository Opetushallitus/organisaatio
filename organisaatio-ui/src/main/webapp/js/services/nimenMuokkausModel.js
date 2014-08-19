app.factory('NimenMuokkausModel', function($filter, $log, Alert, NimenMuokkaus) {
    emptyNimi = {
        "nimi" : {
            "fi" : "",
            "sv" : "",
            "en" : ""
        },
        "alkuPvm" : ""
    };

    var model = {
        uusinNimi : emptyNimi,
        minAlkuPvm : "",
        nimi : emptyNimi,

        // Haetaan Nimihistorian uusin nimi
        getUusinNimi: function(nimihistoria) {
            var nimi = null;
            if (nimihistoria.length > 0) {
                nimi = nimihistoria[0];
            }
            for(var i=0; i < nimihistoria.length; i++) {
                if (moment(nimihistoria[i].alkuPvm).isAfter(moment(nimi.alkuPvm))) {
                    nimi = nimihistoria[i];
                }
            }
            return nimi;
        },

        // Haetaan nimihistorian sisältämä nykyinen nimi (ei siis tuleva ajastettu nimi)
        getCurrentNimi: function(nimihistoria) {
            var nimi = null;
            if (nimihistoria.length > 0) {
                nimi = nimihistoria[0];
            }
            for(var i=0; i < nimihistoria.length; i++) {
                $log.debug(nimihistoria[i]);
                if (moment(nimihistoria[i].alkuPvm).isAfter(moment(nimi.alkuPvm)) &&
                        moment(nimihistoria[i].alkuPvm).isBefore(moment())) {
                    nimi = nimihistoria[i];
                }
            }
            return nimi;
        },

        // Haetaan uuden nimen minimialkupäivämäärä
        // Viimeisimmän voimassaolevan nimen alkupäivämäärä tai organisaation alkupäiviämäärä.
        getMinAlkuPvm: function(nimihistoria, organisaatioAlkuPvm) {
            var voimassaolevaNimi = model.getCurrentNimi(nimihistoria);
            var minAlkuPvm = "";

            if('alkuPvm' in voimassaolevaNimi && moment(voimassaolevaNimi.alkuPvm).isValid()) {
                minAlkuPvm = voimassaolevaNimi.alkuPvm;
            }
            else {
                minAlkuPvm = organisaatioAlkuPvm;
            }
            $log.debug('Minimi alkupvm: ' + minAlkuPvm);

            return minAlkuPvm;
        },

        // Tarkastetaan onko uusin tallennettu muutos ajastus --> siis alkupvm tulevaisuudessa
        isAjastettuMuutos: function(uusinNimi) {
            var ajastettuMuutos = false;
            if('alkuPvm' in uusinNimi &&
                    moment(uusinNimi.alkuPvm).isValid() &&
                    moment(uusinNimi.alkuPvm).isAfter(moment())) {
                ajastettuMuutos = true;
            }
            $log.debug('Ajastettu muutos: ' + $scope.ajastettuMuutos);

            return ajastettuMuutos;
        },

        setUusinNimiVisible: function() {
            this.nimi = this.uusinNimi;
        },

        clearVisibleNimi: function() {
            this.nimi = emptyNimi;
        },


        refresh: function(nimihistoria, organisaatioAlkuPvm) {
            $log.log('refresh()');
            this.uusinNimi = this.getUusinNimi(nimihistoria);

            this.minAlkuPvm = this.getMinAlkuPvm(nimihistoria, organisaatioAlkuPvm);
        }
    };

    return model;
});



