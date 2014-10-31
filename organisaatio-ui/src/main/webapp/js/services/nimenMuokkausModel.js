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

app.factory('NimenMuokkausModel', function($log, $location,
                                           NimiHistoriaModel) {
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
        muokattavaNimihistoria : [],
        mode : "new",
        historiaModel : NimiHistoriaModel,
        parentNimi : {},
        uusinNimi : {},

        // Tyhjenneteään mallin tiedot
        clear: function() {
            $log.debug('clear()');
            this.modified = false;
            this.oid = "";
            this.minAlkuPvm = "";
            this.nimi = {};
            this.mode = "new";
            this.parentNimi = {};
            this.uusinNimi = {};
        },

        // Palautetaan muokattu nimihistoria
        getNimiHistoria: function() {
            $log.debug('getNimiHistoria()');
            return this.muokattavaNimihistoria;
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
            $log.debug('getMinAlkuPvm() ' + minAlkuPvm);

            return minAlkuPvm;
        },

        // Laitetaan uusin nimi näkyville / editoitavaksi
        setUusinNimiVisible: function(koulutustoimija, oppilaitos, parentNimi) {
            this.nimi = this.uusinNimi;
            if (!koulutustoimija && !oppilaitos) this.fixParentPrefix(parentNimi, this.nimi);
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

        // Poistetaan parent prefix nimestä
        fixParentPrefix: function(parentNimi, nimi) {
            $log.log('fixParentPrefix()');
            if (parentNimi && nimi) {
                ['fi', 'sv', 'en'].forEach(function(key) {
                    if (nimi.nimi[key] && model.parentNimi[key]) {
                        nimi.nimi[key] = nimi.nimi[key].replace(model.parentNimi[key] + ", ", "");
                    }
                });
            }
        },

        // Ennekuin NimenMuokkausModel:a voidaan käyttää pitää se alustaa
        refresh: function(oid, nimihistoria, organisaatioAlkuPvm,
                          koulutustoimija, oppilaitos, parentNimi,
                          nameFormat) {
            if (this.oid === oid && this.modified) {
                $log.log('refresh() Using old instance');
                this.fixParentPrefix(koulutustoimija || oppilaitos ? null : parentNimi, this.nimi);
                return;
            }
            $log.log('refresh()');

            this.oid = oid;
            this.koulutustoimija = koulutustoimija;
            this.oppilaitos = oppilaitos;
            this.nameFormat = nameFormat;
            this.parentNimi = parentNimi;
            this.muokattavaNimihistoria = angular.copy(nimihistoria);
            this.uusinNimi = this.historiaModel.getUusinNimi(this.muokattavaNimihistoria);

            if (/new$/.test($location.path())) {
                this.uusiOrganisaatio = true;
            }
            else {
                this.uusiOrganisaatio = false;
            }
            this.mode = "new";

            this.ajastettuMuutos = this.historiaModel.isAjastettuMuutos(this.uusinNimi);
            this.minAlkuPvm = this.getMinAlkuPvm(organisaatioAlkuPvm);

            if (this.mode==="update") {
                this.setUusinNimiVisible();
            }

            if (parentNimi && this.uusinNimi) {
                // Poistetaan parent prefix nimestä
                this.fixParentPrefix(parentNimi, this.uusinNimi);
            }
        },

        accept: function() {
            if (model.koulutustoimija || model.oppilaitos) {
                return;
            }

            // Lisätään parentnimi prefix
            ['fi', 'sv', 'en'].forEach(function(key) {
                if (model.nimi.nimi[key] && model.parentNimi[key]) {
                    if (!model.nimi.nimi[key].match("^" + model.parentNimi[key] + ", ") &&
                            !model.nimi.nimi[key].match("^" + model.parentNimi[key] + "$")) {
                        model.nimi.nimi[key] = model.parentNimi[key] + ", " + model.nimi.nimi[key];
                    }
                }
                if (model.uusinNimi && model.uusinNimi.nimi[key] && model.parentNimi[key]) {
                    if (!model.uusinNimi.nimi[key].match("^" + model.parentNimi[key] + ", ") &&
                            !model.uusinNimi.nimi[key].match("^" + model.parentNimi[key] + "$")) {
                        model.uusinNimi.nimi[key] = model.parentNimi[key] + ", " + model.uusinNimi.nimi[key];
                    }
                }
            });
        }
    };

    return model;
});



