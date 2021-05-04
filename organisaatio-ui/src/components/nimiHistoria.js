import moment from 'moment';

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
        nimihistoria: [],

        // Nimi rakenne stringiksi (ei alkupäivämäärää)
        nimiToString: function (nimi) {
            var nimiStr = '';
            if (nimi && 'nimi' in nimi) {
                ['fi', 'sv', 'en'].forEach(function (key) {
                    if (key in nimi.nimi) nimiStr += '[' + key + '] ' + nimi.nimi[key];
                });
            }
            return nimiStr;
        },

        // Tyhjenneteään mallin tiedot
        clear: function () {
            console.debug('clear()');

            this.nimihistoria = [];
        },

        // Palauttaa nimihistorian perusteella organisaation nimen.
        // Nimi on joko tämänhetkinen (voimassaoloajaltaan nykyinen) nimi
        // tai sitten uudelle organisaatiolle tulevaisuuden nimi.
        getNimi: function (nimiHistoria) {
            var historia = nimiHistoria;
            if (historia === null) {
                historia = this.nimihistoria;
            }

            var nimi = this.getCurrentNimi(historia);

            if (nimi === null) {
                return this.getUusinNimi(historia);
            }

            return nimi;
        },

        // Haetaan Nimihistorian ajastettu
        getAjastettuNimi: function (nimiHistoria) {
            var historia = nimiHistoria;
            if (historia === null) {
                historia = this.nimihistoria;
            }

            var uusinNimi = this.getUusinNimi(historia);

            // Jos nimi on ajastettu ja eroaa tule
            if (this.isAjastettuMuutos(uusinNimi) === false) {
                return null;
            }
            return uusinNimi;
        },

        // Haetaan Nimihistorian uusin nimi
        getUusinNimi: function (nimiHistoria) {
            var historia = nimiHistoria;
            if (historia === null) {
                historia = this.nimihistoria;
            }
            var nimi = null;
            if (historia === null) {
                console.warn('getUusinNimi() historia == null or undefined');
                return nimi;
            }
            if (historia.length > 0) {
                nimi = historia[0];
            }
            for (var i = 0; i < historia.length; i++) {
                if (moment(historia[i].alkuPvm).isAfter(moment(nimi.alkuPvm))) {
                    nimi = historia[i];
                }
            }

            console.debug('getUusinNimi() ' + model.nimiToString(nimi));
            return nimi;
        },

        // Haetaan nimihistorian sisältämä nykyinen nimi (ei siis tuleva ajastettu nimi)
        getCurrentNimi: function (nimiHistoria) {
            var historia = nimiHistoria;
            if (historia === null) {
                historia = this.nimihistoria;
            }

            var nimi = null;
            if (historia === null) {
                console.warn('getUusinNimi() historia == null or undefined');
                return nimi;
            }
            if (historia.length > 0) {
                nimi = historia[0];
            }
            for (var i = 0; i < historia.length; i++) {
                if (
                    moment(historia[i].alkuPvm).isAfter(moment(nimi.alkuPvm)) &&
                    moment(historia[i].alkuPvm).isBefore(moment())
                ) {
                    nimi = historia[i];
                }
            }

            console.debug('getCurrentNimi() ' + this.nimiToString(nimi));
            return nimi;
        },

        // Tarkastetaan onko annetun nimen muutos ajastus --> siis alkupvm tulevaisuudessa
        isAjastettuMuutos: function (tarkistettavaNimi) {
            var nimi = tarkistettavaNimi;
            if (nimi === null) {
                nimi = this.getUusinNimi();
            }

            var ajastettuMuutos = false;
            if (nimi !== null) {
                if ('alkuPvm' in nimi && moment(nimi.alkuPvm).isValid() && moment(nimi.alkuPvm).isAfter(moment())) {
                    ajastettuMuutos = true;
                }
            }

            console.debug('isAjastettuMuutos() ' + this.nimiToString(nimi) + ' = ' + ajastettuMuutos);
            return ajastettuMuutos;
        },

        // Palauttaa nimihistorian
        getNimihistoria: function () {
            return this.nimihistoria;
        },

        // Alustetaan NimiHistoriaModel uudella nimihistorialla
        setNimihistoria: function (nimihistoria) {
            this.nimihistoria = nimihistoria;
        },
    },
};
