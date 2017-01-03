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
        toimipiste : false,
        suunniteltuOrganisaatio : false,
        ajastettuMuutos : false,

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
            this.toimipiste = false;
            this.suunniteltuOrganisaatio = false;
            this.ajastettuMuutos = false;
        },

        // Asetetaan nimihistoria, jota tullaan muokkaamaan
        setNimihistoria: function(nimihistoria) {
            $log.debug('setNimihistoria()');
            this.muokattavaNimihistoria = angular.copy(nimihistoria);
            this.uusinNimi = this.historiaModel.getUusinNimi(this.muokattavaNimihistoria);

            this.ajastettuMuutos = this.historiaModel.isAjastettuMuutos(this.uusinNimi);

            // Tarkistetaan onko kyseessä suunniteltu organisaatio
            // Siis nykyinen nimi tulevaisuudessa.
            var nimi = this.historiaModel.getNimi(this.muokattavaNimihistoria);
            if (this.historiaModel.isAjastettuMuutos(nimi)) {
                this.suunniteltuOrganisaatio = true;
            }
            else {
                this.suunniteltuOrganisaatio = false;
            }

            if (this.parentNimi && this.uusinNimi) {
                // Poistetaan parent prefix nimestä
                this.removeParentPrefix(this.uusinNimi);
            }
        },

        isSuunniteltuOrganisaatio: function() {
            return this.suunniteltuOrganisaatio;
        },

        // Asetetaan muokattu versio ogranisaation nykyisestä nimestä
        setEditedNimi: function(nimi) {
            $log.debug('setEditedNimi()', nimi);
            var emptyNimi = {
                "nimi" : {
                },
                "alkuPvm" : ""
            };

            if (nimi === null) {
                nimi = emptyNimi;
            }

            this.editedNimi = nimi;
            this.originalEditedNimi = angular.copy(nimi);

            // Poistetaan parent prefix nimestä
            this.removeParentPrefix(this.editedNimi);
            this.removeParentPrefix(this.originalEditedNimi);
        },

        // Haetaan muokattu versio ogranisaation nykyisestä nimestä
        getEditedNimi: function() {
            return this.editedNimi;
        },

        // Palautetaan muokattu nimihistoria
        getNimihistoria: function() {
            $log.debug('getNimiHistoria()');
            return this.muokattavaNimihistoria;
        },

        // Haetaan uuden nimen minimialkupäivämäärä
        // Viimeisimmän voimassaolevan nimen alkupäivämäärä tai organisaation alkupäiviämäärä.
        getMinAlkuPvm: function(organisaatioAlkuPvm) {
            var voimassaolevaNimi = model.historiaModel.getCurrentNimi(this.muokattavaNimihistoria);
            var minAlkuPvm = "";

            // Uuden organisaation tapaus
            if (voimassaolevaNimi === null) {
                return minAlkuPvm;
            }
            if('alkuPvm' in voimassaolevaNimi && moment(voimassaolevaNimi.alkuPvm, 'YYYY-MM-DD').isValid()) {
                // Uuden nimen alkupäivämäärä ei voi olla sama kuin vanhan
                minAlkuPvm = moment(voimassaolevaNimi.alkuPvm, 'YYYY-MM-DD').add(1,'d').toDate();
            }
            else {
                minAlkuPvm = organisaatioAlkuPvm;
            }
            $log.debug('getMinAlkuPvm() ' + minAlkuPvm);

            return minAlkuPvm;
        },

        // Laitetaan uusin nimi näkyville / editoitavaksi
        setEditedNimiVisible: function() {
            this.nimi = this.uusinNimi;
            this.setNimi(this.editedNimi);
        },

        // Laitetaan annettu nimi editoitavaksi
        setNimi: function(nimi) {
            $log.debug('setNimi()', nimi);
            for(var i=0; i < this.muokattavaNimihistoria.length; i++) {
                if (this.nimi === this.muokattavaNimihistoria[i]) {
                    this.muokattavaNimihistoria[i].nimi = nimi.nimi;
                    this.muokattavaNimihistoria[i].alkuPvm = nimi.alkuPvm;
                }
            }

            this.removeParentPrefix(this.nimi);
        },

        // Tarkistetaan onko uusin nimi muuttunut
        isEditedNimiChanged: function() {
            if (angular.equals(this.editedNimi, this.originalEditedNimi)) {
                return false;
            }
            return true;
        },

        // Luodaan organisaatiolle uusi nimi
        createNewNimi: function() {
            var emptyNimi = {
                "nimi" : {
                },
                "alkuPvm" : ""
            };
            this.nimi = emptyNimi;
            this.muokattavaNimihistoria.push(this.nimi);
        },

        // Poistetaan uusin nimi historiasta
        deleteUusinNimi: function() {
            this.nimi = angular.copy(this.uusinNimi)

            for(var i=0; i < this.muokattavaNimihistoria.length; i++) {
                if (this.uusinNimi === this.muokattavaNimihistoria[i]) {
                    this.muokattavaNimihistoria.splice(i, 1);
                }
            }
            this.removeParentPrefix(this.nimi);
        },

        // Tarkastetaan onko annettu nimi ajastettu nimenmuutos
        isAjastettuMuutos: function(nimi) {
            return this.historiaModel.isAjastettuMuutos(nimi);
        },

        // Poistetaan parent prefix nimestä
        removeParentPrefix: function(nimi) {
            if (this.toimipiste) {
                $log.log('removeParentPrefix()');

                if (model.parentNimi && nimi) {
                    ['fi', 'sv', 'en'].forEach(function(key) {
                        if (nimi.nimi[key] && model.parentNimi[key]) {
                            nimi.nimi[key] = nimi.nimi[key].replace(model.parentNimi[key] + ", ", "");
                        }
                    });
                }
            }
        },

        // Poistetaan tyhjät nimet
        removeEmptyNimi: function(nimi) {
            if (nimi) {
                ['fi', 'sv', 'en'].forEach(function(key) {
                    if (typeof nimi.nimi[key] === 'undefined') {
                        delete nimi.nimi[key];
                    }
                });
            }
        },

        // Lisätään parent prefix nimeen
        addParentPrefix: function(nimi) {
            this.removeEmptyNimi(nimi);

            if (this.toimipiste) {
                $log.log('addParentPrefix()');

                if (model.parentNimi && nimi) {
                    ['fi', 'sv', 'en'].forEach(function(key) {
                        if (nimi.nimi[key] && model.parentNimi[key]) {
                            if (!nimi.nimi[key].startsWith(model.parentNimi[key] + ", ") &&
                                    nimi.nimi[key] !== model.parentNimi[key]) {
                                nimi.nimi[key] = model.parentNimi[key] + ", " + nimi.nimi[key];
                            }
                        }
                    });
                }
            }
        },


        // Ennekuin NimenMuokkausModel:a voidaan käyttää pitää se alustaa
        refresh: function(oid, nimihistoria, organisaatioAlkuPvm,
                          //koulutustoimija, oppilaitos,
                          toimipiste, parentNimi,
                          nameFormat) {
            $log.log('refresh()');

            this.setNimihistoria(nimihistoria);

            this.uusinNimiOrig = angular.copy(this.uusinNimi);
            this.removeParentPrefix(this.uusinNimiOrig);

            this.oid = oid;
            this.toimipiste = toimipiste;
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
            this.addParentPrefix(model.nimi);
            this.addParentPrefix(model.uusinNimi);
        }
    };

    return model;
});



