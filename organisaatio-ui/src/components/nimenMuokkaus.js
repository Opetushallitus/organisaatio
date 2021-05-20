// TODO ei käytetä??

import moment from 'moment';
import nimiHistoria from './nimiHistoria';

import { deepEquals } from './Ytjmapper';

export default {
    //    emptyNimi = {
    //        "nimi" : {
    //            "fi" : "",
    //            "sv" : "",
    //            "en" : ""
    //        },
    //        "alkuPvm" : ""
    //    };

    model: {
        oid: '',
        minAlkuPvm: '',
        nimi: {},
        muokattavaNimihistoria: [],
        mode: 'new',
        historiaModel: nimiHistoria,
        parentNimi: {},
        uusinNimi: {},
        uusinNimiOrig: {},
        toimipiste: false,
        suunniteltuOrganisaatio: false,
        ajastettuMuutos: false,

        // Tyhjenneteään mallin tiedot
        clear: function () {
            console.debug('clear()');
            this.oid = '';
            this.minAlkuPvm = '';
            this.nimi = {};
            this.mode = 'new';
            this.parentNimi = {};
            this.uusinNimi = {};
            this.uusinNimiOrig = {};
            this.toimipiste = false;
            this.suunniteltuOrganisaatio = false;
            this.ajastettuMuutos = false;
        },

        // Asetetaan nimihistoria, jota tullaan muokkaamaan
        setNimihistoria: function (nimihistoria) {
            console.debug('setNimihistoria()');
            this.muokattavaNimihistoria = Object.assign({}, nimihistoria);
            this.uusinNimi = this.historiaModel.getUusinNimi(this.muokattavaNimihistoria);

            this.ajastettuMuutos = this.historiaModel.isAjastettuMuutos(this.uusinNimi);

            // Tarkistetaan onko kyseessä suunniteltu organisaatio
            // Siis nykyinen nimi tulevaisuudessa.
            var nimi = this.historiaModel.getNimi(this.muokattavaNimihistoria);
            if (this.historiaModel.isAjastettuMuutos(nimi)) {
                this.suunniteltuOrganisaatio = true;
            } else {
                this.suunniteltuOrganisaatio = false;
            }

            if (this.parentNimi && this.uusinNimi) {
                // Poistetaan parent prefix nimestä
                this.removeParentPrefix(this.uusinNimi);
            }
        },

        isSuunniteltuOrganisaatio: function () {
            return this.suunniteltuOrganisaatio;
        },

        // Asetetaan muokattu versio ogranisaation nykyisestä nimestä
        setEditedNimi: function (nimi) {
            console.debug('setEditedNimi()', nimi);
            var emptyNimi = {
                nimi: {},
                alkuPvm: '',
            };

            if (nimi === null) {
                nimi = emptyNimi;
            }

            this.editedNimi = nimi;
            this.originalEditedNimi = Object.assign({}, nimi);

            // Poistetaan parent prefix nimestä
            this.removeParentPrefix(this.editedNimi);
            this.removeParentPrefix(this.originalEditedNimi);
        },

        // Haetaan muokattu versio ogranisaation nykyisestä nimestä
        getEditedNimi: function () {
            return this.editedNimi;
        },

        // Palautetaan muokattu nimihistoria
        getNimihistoria: function () {
            console.debug('getNimiHistoria()');
            return this.muokattavaNimihistoria;
        },

        // Haetaan uuden nimen minimialkupäivämäärä
        // Viimeisimmän voimassaolevan nimen alkupäivämäärä tai organisaation alkupäiviämäärä.
        getMinAlkuPvm: function (organisaatioAlkuPvm) {
            var voimassaolevaNimi = this.historiaModel.getCurrentNimi(this.muokattavaNimihistoria);
            var minAlkuPvm = '';

            // Uuden organisaation tapaus
            if (voimassaolevaNimi === null) {
                return minAlkuPvm;
            }
            if ('alkuPvm' in voimassaolevaNimi && moment(voimassaolevaNimi.alkuPvm, 'YYYY-MM-DD').isValid()) {
                // Uuden nimen alkupäivämäärä ei voi olla sama kuin vanhan
                minAlkuPvm = moment(voimassaolevaNimi.alkuPvm, 'YYYY-MM-DD').add(1, 'd').toDate();
            } else {
                minAlkuPvm = organisaatioAlkuPvm;
            }
            $log.debug('getMinAlkuPvm() ' + minAlkuPvm);

            return minAlkuPvm;
        },

        // Laitetaan uusin nimi näkyville / editoitavaksi
        setEditedNimiVisible: function () {
            this.nimi = this.uusinNimi;
            this.setNimi(this.editedNimi);
        },

        // Laitetaan annettu nimi editoitavaksi
        setNimi: function (nimi) {
            console.debug('setNimi()', nimi);
            for (var i = 0; i < this.muokattavaNimihistoria.length; i++) {
                if (this.nimi === this.muokattavaNimihistoria[i]) {
                    this.muokattavaNimihistoria[i].nimi = nimi.nimi;
                    this.muokattavaNimihistoria[i].alkuPvm = nimi.alkuPvm;
                }
            }

            this.removeParentPrefix(this.nimi);
        },

        // Tarkistetaan onko uusin nimi muuttunut
        isEditedNimiChanged: function () {
            if (deepEquals(this.editedNimi, this.originalEditedNimi)) {
                return false;
            }
            return true;
        },

        // Luodaan organisaatiolle uusi nimi
        createNewNimi: function () {
            var emptyNimi = {
                nimi: {},
                alkuPvm: '',
            };
            this.nimi = emptyNimi;
            this.muokattavaNimihistoria.push(this.nimi);
        },

        // Poistetaan uusin nimi historiasta
        deleteUusinNimi: function () {
            this.nimi = Object.assign({}, this.uusinNimi);

            for (var i = 0; i < this.muokattavaNimihistoria.length; i++) {
                if (this.uusinNimi === this.muokattavaNimihistoria[i]) {
                    this.muokattavaNimihistoria.splice(i, 1);
                }
            }
            this.removeParentPrefix(this.nimi);
        },

        // Tarkastetaan onko annettu nimi ajastettu nimenmuutos
        isAjastettuMuutos: function (nimi) {
            return this.historiaModel.isAjastettuMuutos(nimi);
        },

        // Poistetaan parent prefix nimestä
        removeParentPrefix: function (nimi) {
            if (this.toimipiste) {
                console.log('removeParentPrefix()');

                if (this.parentNimi && nimi) {
                    ['fi', 'sv', 'en'].forEach(function (key) {
                        if (nimi.nimi[key] && this.parentNimi[key]) {
                            nimi.nimi[key] = nimi.nimi[key].replace(this.parentNimi[key] + ', ', '');
                        }
                    });
                }
            }
        },

        // Poistetaan tyhjät nimet
        removeEmptyNimi: function (nimi) {
            if (nimi) {
                ['fi', 'sv', 'en'].forEach(function (key) {
                    if (typeof nimi.nimi[key] === 'undefined') {
                        delete nimi.nimi[key];
                    }
                });
            }
        },

        // Lisätään parent prefix nimeen
        addParentPrefix: function (nimi) {
            this.removeEmptyNimi(nimi);

            if (this.toimipiste) {
                console.log('addParentPrefix()');

                if (this.model.parentNimi && nimi) {
                    ['fi', 'sv', 'en'].forEach(function (key) {
                        if (nimi.nimi[key] && this.model.parentNimi[key]) {
                            if (
                                !nimi.nimi[key].startsWith(this.model.parentNimi[key] + ', ') &&
                                nimi.nimi[key] !== this.model.parentNimi[key]
                            ) {
                                nimi.nimi[key] = this.model.parentNimi[key] + ', ' + nimi.nimi[key];
                            }
                        }
                    });
                }
            }
        },

        // Ennekuin NimenMuokkausModel:a voidaan käyttää pitää se alustaa
        refresh: function (
            oid,
            nimihistoria,
            organisaatioAlkuPvm,
            //koulutustoimija, oppilaitos,
            toimipiste,
            parentNimi,
            nameFormat
        ) {
            console.log('refresh()');

            this.setNimihistoria(nimihistoria);

            this.uusinNimiOrig = Object({}, this.uusinNimi);
            this.removeParentPrefix(this.uusinNimiOrig);

            this.oid = oid;
            this.toimipiste = toimipiste;
            this.nameFormat = nameFormat;
            this.parentNimi = parentNimi;

            /* if (/new$/.test($location.path())) {
                this.uusiOrganisaatio = true;
            }
            else {
                this.uusiOrganisaatio = false;
            }

            */

            this.minAlkuPvm = this.getMinAlkuPvm(organisaatioAlkuPvm);

            if (this.mode === 'update') {
                this.setUusinNimiVisible();
            }
        },

        accept: function () {
            this.addParentPrefix(this.model.nimi);
            this.addParentPrefix(this.model.uusinNimi);
        },
    },
};
