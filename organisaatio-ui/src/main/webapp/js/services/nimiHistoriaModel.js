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

app.factory('NimiHistoriaModel', function($log) {
//    emptyNimi = {
//        "nimi" : {
//            "fi" : "",
//            "sv" : "",
//            "en" : ""
//        },
//        "alkuPvm" : ""
//    };

    $log = $log.getInstance("NimiHistoriaModel");

    var model = {
        nimihistoria : [],

        // Nimi rakenne stringiksi (ei alkupäivämäärää)
        nimiToString: function(nimi) {
            var nimiStr = '';
            if (nimi && 'nimi' in nimi) {
                ['fi', 'sv', 'en'].forEach(function(key) {
                    if (key in nimi.nimi)
                        nimiStr += '[' + key + '] ' + nimi.nimi[key];
                });
            }
            return nimiStr;
        },

        // Tyhjenneteään mallin tiedot
        clear: function() {
            $log.debug('clear()');

            this.nimihistoria = [];
        },

        // Palauttaa nimihistorian perusteella organisaation nimen.
        // Nimi on joko tämänhetkinen (voimassaoloajaltaan nykyinen) nimi
        // tai sitten uudelle organisaatiolle tulevaisuuden nimi.
        getNimi: function(nimiHistoria) {
            var historia = nimiHistoria;
            if (!angular.isDefined(historia) || historia === null) {
                historia = this.nimihistoria;
            }

            var nimi = this.getCurrentNimi(historia);

            if (nimi === null) {
                return this.getUusinNimi(historia);
            }

            return nimi;
        },

        // Haetaan Nimihistorian ajastettu
        getAjastettuNimi: function(nimiHistoria) {
            var historia = nimiHistoria;
            if (!angular.isDefined(historia) || historia === null) {
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
        getUusinNimi: function(nimiHistoria) {
            var historia = nimiHistoria;
            if (!angular.isDefined(historia) || historia === null) {
                historia = this.nimihistoria;
            }
            var nimi = null;
            if (!angular.isDefined(historia) || historia === null) {
                $log.warn('getUusinNimi() historia == null or undefined');
                return nimi;
            }
            if (historia.length > 0) {
                nimi = historia[0];
            }
            for(var i=0; i < historia.length; i++) {
                if (moment(historia[i].alkuPvm).isAfter(moment(nimi.alkuPvm))) {
                    nimi = historia[i];
                }
            }

            $log.debug('getUusinNimi() ' + model.nimiToString(nimi));
            return nimi;
        },

        // Haetaan nimihistorian sisältämä nykyinen nimi (ei siis tuleva ajastettu nimi)
        getCurrentNimi: function(nimiHistoria) {
            var historia = nimiHistoria;
            if (!angular.isDefined(historia) || historia === null) {
                historia = this.nimihistoria;
            }

            var nimi = null;
            if (!angular.isDefined(historia) || historia === null) {
                $log.warn('getUusinNimi() historia == null or undefined');
                return nimi;
            }
            if (historia.length > 0) {
                nimi = historia[0];
            }
            for(var i=0; i < historia.length; i++) {
                if (moment(historia[i].alkuPvm).isAfter(moment(nimi.alkuPvm)) &&
                        moment(historia[i].alkuPvm).isBefore(moment())) {
                    nimi = historia[i];
                }
            }

            $log.debug('getCurrentNimi() ' + model.nimiToString(nimi));
            return nimi;
        },

        // Tarkastetaan onko annetun nimen muutos ajastus --> siis alkupvm tulevaisuudessa
        isAjastettuMuutos: function(tarkistettavaNimi) {
            var nimi = tarkistettavaNimi;
            if (!angular.isDefined(nimi) || nimi === null) {
                nimi = this.getUusinNimi();
            }

            var ajastettuMuutos = false;
            if (nimi !== null) {
                if('alkuPvm' in nimi &&
                        moment(nimi.alkuPvm).isValid() &&
                        moment(nimi.alkuPvm).isAfter(moment())) {
                    ajastettuMuutos = true;
                }
            }

            $log.debug('isAjastettuMuutos() ' + model.nimiToString(nimi) + " = " + ajastettuMuutos);
            return ajastettuMuutos;
        },

        // Palauttaa nimihistorian
        getNimihistoria: function() {
            return this.nimihistoria;
        },

        // Alustetaan NimiHistoriaModel uudella nimihistorialla
        setNimihistoria: function(nimihistoria) {
            $log.log('init()');
            this.nimihistoria = nimihistoria;

            $log.log("init() done");
        }
    };

    return model;
});



