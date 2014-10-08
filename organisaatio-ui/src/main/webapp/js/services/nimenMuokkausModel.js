app.factory('NimenMuokkausModel', function($q, $filter, $log, $location, Alert, NimiHistoriaModel, Nimet) {
//    emptyNimi = {
//        "nimi" : {
//            "fi" : "",
//            "sv" : "",
//            "en" : ""
//        },
//        "alkuPvm" : ""
//    };

    $log = $log.getInstance("NimenMuokkausModel");

    var model = {
        oid : "",
        minAlkuPvm : "",
        nimi : {},
        mode : "new",
        historiaModel : NimiHistoriaModel,
        parentNimi : {},

        // Tyhjenneteään mallin tiedot
        clear: function() {
            $log.debug('NimenMuokkausModel::clear()');
            this.oid = "";
            this.minAlkuPvm = "";
            this.nimi = {};
            this.mode = "new";
            this.historiaModel.clear();
            this.parentNimi = {};
        },

        // Haetaan uuden nimen minimialkupäivämäärä
        // Viimeisimmän voimassaolevan nimen alkupäivämäärä tai organisaation alkupäiviämäärä.
        getMinAlkuPvm: function(organisaatioAlkuPvm) {
            var voimassaolevaNimi = model.historiaModel.getCurrentNimi();
            var minAlkuPvm = "";

            // Uuden organisaation tapaus
            if (voimassaolevaNimi === null) {
                return minAlkuPvm;
            }

            if('alkuPvm' in voimassaolevaNimi && moment(voimassaolevaNimi.alkuPvm).isValid()) {
                // Uuden nimen alkupäivämäärä ei voi olla sama kuin vanhan
                minAlkuPvm = moment(voimassaolevaNimi.alkuPvm).add('d', 1).toDate();
            }
            else {
                minAlkuPvm = organisaatioAlkuPvm;
            }
            $log.debug('NimenMuokkausModel::getMinAlkuPvm() ' + minAlkuPvm);

            return minAlkuPvm;
        },

        // Laitetaan uusin nimi näkyville / editoitavaksi
        setUusinNimiVisible: function() {
            this.nimi = this.historiaModel.uusinNimi;
        },

        isUusinNimiChanged: function() {
            if (angular.equals(this.historiaModel.uusinNimi, this.historiaModel.getUusinNimi())) {
                return false;
            }
            return true;
        },

        // Tyhjennetään editoitava nimi
        clearVisibleNimi: function() {
            this.nimi = {};
        },

        // Tarkastetaan onko annettu nimi ajastettu nimenmuutos
        isAjastettuMuutos: function(nimi) {
            return this.historiaModel.isAjastettuMuutos(nimi);
        },

        // Uuden nimen tallennus
        saveNewNimi: function(deferred) {
            Nimet.put({oid: this.oid, alkuPvm: ""}, this.nimi, function(result) {
                $log.log(result);
                deferred.resolve();
            },
            // Error case
            function(response) {
                $log.error("NimenMuokkausModel::saveNewNimi() Nimet put response: " + response.status);
                Alert.add("error", $filter('i18n')("Nimenmuokkaus.uusinimi.virhe", ""), true);
                deferred.reject();
            });
        },

        // Nimen päivitys
        saveUpdatedNimi: function(deferred) {
            Nimet.post({oid: this.oid, alkuPvm: this.nimi.alkuPvm}, this.nimi, function(result) {
                $log.log(result);
                deferred.resolve();
            },
            // Error case
            function(response) {
                $log.error("NimenMuokkausModel::saveUpdatedNimi() Nimet post response: " + response.status);
                Alert.add("error", $filter('i18n')("Nimenmuokkaus.updatenimi.virhe", ""), true);
                deferred.reject();
            });
        },

        // Ajastetun nimenmuutoksen poisto / peruminen
        deletePresetNimi: function(deferred) {
            Nimet.delete({oid: this.oid, alkuPvm: this.historiaModel.uusinNimi.alkuPvm}, function(result) {
                $log.log(result);
                deferred.resolve();
            },
            // Error case
            function(response) {
                $log.error("NimenMuokkausModel::deletePresetNimi() Nimet delete response: " + response.status);
                Alert.add("error", $filter('i18n')("Nimenmuokkaus.deletenimi.virhe", ""), true);
                deferred.reject();
            });
        },

        // Tallennus, tilasta riippuen luodaan uusi nimi, päivitetään nimi tai perutaan ajastus
        save: function() {
            var deferred = $q.defer();

            // Uuden organisaation tapauksessa luotetaan siihen, että
            // organisaation tallennus tallentaa myös ensimmäisen nimihistorian
            if (this.uusiOrganisaatio) {
                deferred.resolve();
                return deferred.promise;
            }

            if (this.mode === "update") {
                this.saveUpdatedNimi(deferred);
            }
            else if (this.mode === "new") {
                this.saveNewNimi(deferred);
            }
            else if (this.mode === "delete") {
                this.deletePresetNimi(deferred);
            }
            else {
                $log.error("Unknown mode: " + this.mode);
            }
            return deferred.promise;
        },

        // Ennekuin NimenMuokkausModel:a voidaan käyttää pitää se alustaa
        refresh: function(oid, nimihistoria, organisaatioAlkuPvm,
                          koulutustoimija, oppilaitos, parentNimi,
                          nameFormat) {
            if (this.oid === oid) {
                $log.log('NimenMuokkausModel::refresh() Using old instance');
                return;
            }
            $log.log('NimenMuokkausModel::refresh()');

            // Alustetaan historiamalli
            this.historiaModel.init(nimihistoria, koulutustoimija || oppilaitos ? null : parentNimi);

            this.oid = oid;
            this.koulutustoimija = koulutustoimija;
            this.oppilaitos = oppilaitos;
            this.nameFormat = nameFormat;
            this.parentNimi = parentNimi;

            if (/new$/.test($location.path())) {
                this.uusiOrganisaatio = true;
            }
            else {
                this.uusiOrganisaatio = false;
            }
            this.mode = "new";

            this.ajastettuMuutos = this.historiaModel.ajastettuMuutos;
            this.minAlkuPvm = this.getMinAlkuPvm(organisaatioAlkuPvm);

            if (this.mode==="update") {
                this.setUusinNimiVisible();
            }
        },

        accept: function() {
            this.historiaModel.accept();
            ['fi', 'sv', 'en'].forEach(function(key) {
                if (!model.koulutustoimija && !model.oppilaitos && model.nimi.nimi[key] && model.parentNimi[key]) {
                    if (!model.nimi.nimi[key].match("^" + model.parentNimi[key] + ", ") &&
                        !model.nimi.nimi[key].match("^" + model.parentNimi[key] + "$")) {
                        model.nimi.nimi[key] = model.parentNimi[key] + ", " + model.nimi.nimi[key];
                    }
                }
            });
        }

    };

    return model;
});



