app.factory('NimenMuokkausModel', function($filter, $log, Alert, NimiHistoriaModel, Nimet) {
    emptyNimi = {
        "nimi" : {
            "fi" : "",
            "sv" : "",
            "en" : ""
        },
        "alkuPvm" : ""
    };

    var model = {
        oid : "",
        minAlkuPvm : "",
        nimi : emptyNimi,
        mode : "update",
        historiaModel : NimiHistoriaModel,

        // Tyhjenneteään mallin tiedot
        clear: function() {
            this.oid = "";
            this.minAlkuPvm = "";
            this.nimi = emptyNimi;
            this.mode = "update";
            historiaModel.clear();
        },

        // Haetaan uuden nimen minimialkupäivämäärä
        // Viimeisimmän voimassaolevan nimen alkupäivämäärä tai organisaation alkupäiviämäärä.
        getMinAlkuPvm: function(organisaatioAlkuPvm) {
            var voimassaolevaNimi = model.historiaModel.getCurrentNimi();
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

        setUusinNimiVisible: function() {
            this.nimi = this.historiaModel.uusinNimi;
        },

        clearVisibleNimi: function() {
            this.nimi = emptyNimi;
        },

        saveNewNimi: function() {
            Nimet.put({oid: this.oid, alkuPvm: ""}, this.nimi, function(result) {
                $log.log(result);
            },
            // Error case
            function(response) {
                $log.error("Nimet put response: " + response.status);
                Alert.add("error", $filter('i18n')("Nimenmuokkaus.uusinimi.virhe", ""), true);
            });
        },

        saveUpdatedNimi: function() {
            Nimet.post({oid: this.oid, alkuPvm: this.nimi.alkuPvm}, this.nimi, function(result) {
                $log.log(result);
            },
            // Error case
            function(response) {
                $log.error("Nimet post response: " + response.status);
                Alert.add("error", $filter('i18n')("Nimenmuokkaus.updatenimi.virhe", ""), true);
            });
        },

        deletePresetNimi: function() {
            Nimet.delete({oid: this.oid, alkuPvm: this.uusinNimi.alkuPvm}, function(result) {
                $log.log(result);
            },
            // Error case
            function(response) {
                $log.error("Nimet delete response: " + response.status);
                Alert.add("error", $filter('i18n')("Nimenmuokkaus.deletenimi.virhe", ""), true);
            });
        },

        save: function() {
            if (this.mode === "update") {
                this.saveUpdatedNimi();
            }
            else if (this.mode === "new") {
                this.saveNewNimi();
            }
            else if (this.mode === "delete") {
                this.deletePresetNimi();
            }
            else {
                $log.error("Unknown mode: " + this.mode);
            }
        },

        refresh: function(oid, nimihistoria, organisaatioAlkuPvm) {
            $log.log('refresh()');

            // Alustetaan historiamalli
            this.historiaModel.init(nimihistoria);
            this.oid = oid;
            this.ajastettuMuutos = this.historiaModel.ajastettuMuutos;
            this.minAlkuPvm = this.getMinAlkuPvm(organisaatioAlkuPvm);

            this.setUusinNimiVisible();
        }
    };

    return model;
});



