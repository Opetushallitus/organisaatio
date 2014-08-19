app.factory('NimenMuokkausModel', function($filter, $log, Alert, Nimet) {
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
        mode : "update",

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

        saveNewNimi: function(oid) {
            Nimet.put({oid: oid, alkuPvm: ""}, this.nimi, function(result) {
                $log.log(result);
            },
            // Error case
            function(response) {
                $log.error("Nimet put response: " + response.status);
                Alert.add("error", $filter('i18n')("Nimenmuokkaus.uusinimi.virhe", ""), true);
            });
        },

        saveUpdatedNimi: function(oid) {
            Nimet.post({oid: oid, alkuPvm: this.nimi.alkuPvm}, this.nimi, function(result) {
                $log.log(result);
            },
            // Error case
            function(response) {
                $log.error("Nimet post response: " + response.status);
                Alert.add("error", $filter('i18n')("Nimenmuokkaus.uusinimi.virhe", ""), true);
            });
        },

        refresh: function(nimihistoria, organisaatioAlkuPvm) {
            $log.log('refresh()');
            this.uusinNimi = this.getUusinNimi(nimihistoria);

            this.minAlkuPvm = this.getMinAlkuPvm(nimihistoria, organisaatioAlkuPvm);

            this.setUusinNimiVisible();
        }
    };

    return model;
});



