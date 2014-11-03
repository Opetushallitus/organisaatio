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
        uusinNimiOrig : {},
        koulutustoimija : false,
        oppilaitos : false,

        // Tyhjenneteään mallin tiedot
        clear: function() {
            $log.debug('clear()');
            this.oid = "";
            this.minAlkuPvm = "";
            this.nimi = {};
            this.mode = "new";
            this.parentNimi = {};
            this.uusinNimi = {};
            this.uusinNimiOrig = {};
            this.koulutustoimija = false;
            this.oppilaitos = false;
        },

        // Asetetaan nimihistoria, jota tullaan muokkaamaan
        setNimihistoria: function(nimihistoria) {
            $log.debug('setNimihistoria()');
            this.muokattavaNimihistoria = angular.copy(nimihistoria);
            this.uusinNimi = this.historiaModel.getUusinNimi(this.muokattavaNimihistoria);

            this.ajastettuMuutos = this.historiaModel.isAjastettuMuutos(this.uusinNimi);

            if (this.parentNimi && this.uusinNimi) {
                // Poistetaan parent prefix nimestä
                this.removeParentPrefix(this.uusinNimi);
            }
        },

        // Palautetaan muokattu nimihistoria
        getNimihistoria: function() {
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
        setUusinNimiVisible: function() {
            this.nimi = this.uusinNimi;
            if (!this.koulutustoimija && !this.oppilaitos) this.removeParentPrefix(this.nimi);
        },

        // Tarkistetaan onko uusin nimi muuttunut
        isUusinNimiChanged: function() {
            if (angular.equals(this.uusinNimi, this.uusinNimiOrig)) {
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
        removeParentPrefix: function(nimi) {
            $log.log('removeParentPrefix()');
            if (model.parentNimi && nimi) {
                ['fi', 'sv', 'en'].forEach(function(key) {
                    if (nimi.nimi[key] && model.parentNimi[key]) {
                        nimi.nimi[key] = nimi.nimi[key].replace(model.parentNimi[key] + ", ", "");
                    }
                });
            }
        },

        // Lisätään parent prefix nimeen
        addParentPrefix: function(nimi) {
            if (model.parentNimi && nimi) {
                ['fi', 'sv', 'en'].forEach(function(key) {
                    if (nimi.nimi[key] && model.parentNimi[key]) {
                        if (!nimi.nimi[key].match("^" + model.parentNimi[key] + ", ") &&
                                !nimi.nimi[key].match("^" + model.parentNimi[key] + "$")) {
                            nimi.nimi[key] = model.parentNimi[key] + ", " + nimi.nimi[key];
                        }
                    }
                });
            }
        },


        // Ennekuin NimenMuokkausModel:a voidaan käyttää pitää se alustaa
        refresh: function(oid, nimihistoria, organisaatioAlkuPvm,
                          koulutustoimija, oppilaitos, parentNimi,
                          nameFormat) {
            $log.log('refresh()');

            this.setNimihistoria(nimihistoria);

            this.uusinNimiOrig = angular.copy(this.uusinNimi);
            if (!this.koulutustoimija && !this.oppilaitos) {
                this.removeParentPrefix(this.uusinNimiOrig);
            }

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

            this.minAlkuPvm = this.getMinAlkuPvm(organisaatioAlkuPvm);

            if (this.mode==="update") {
                this.setUusinNimiVisible();
            }
        },

        accept: function() {
            if (model.koulutustoimija || model.oppilaitos) {
                return;
            }
            this.addParentPrefix(model.nimi);
            this.addParentPrefix(model.uusinNimi);
        }
    };

    return model;
});



