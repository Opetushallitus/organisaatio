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

app.factory('YhteystietojentyyppiModel', function($log,
                                                  KoodistoOppilaitostyypit,
                                                  KoodistoKoodi,
                                                  Yhteystietojentyyppi,
                                                  YhteystietojentyypinPoisto)  {

    $log = $log.getInstance("YhteystietojentyyppiModel");

    var model = new function() {
        var organisaatiotyypit = {},
                oppilaitostyypit = [],
                yhteystietotyypit = [],
                oppilaitostyypitMap = {};

        this.organisaatiotyypit = organisaatiotyypit;
        this.oppilaitostyypit = oppilaitostyypit;
        this.yhteystietotyypit = yhteystietotyypit;
        this.oppilaitostyypitMap = oppilaitostyypitMap;

        function loadYhteystietotyypit() {
            Yhteystietojentyyppi.get({}, function(tyypit) {
                tyypit.forEach(function(t) {
                    yhteystietotyypit.push(t);
                });
            });
        }

        function loadOppilaitostyypit() {
            KoodistoOppilaitostyypit.get({onlyValidKoodis:true}, function(tyypit) {
                tyypit.forEach(function(t) {
                    oppilaitostyypit.push({id: t.koodiUri + '#' + t.versio, nimi: KoodistoKoodi.getLocalizedName(t)});
                    oppilaitostyypitMap[t.koodiUri + '#' + t.versio] = KoodistoKoodi.getLocalizedName(t);
                });
                loadYhteystietotyypit();
            });
        }

        loadOppilaitostyypit();

        this.uusiYtt = function() {
            var obj = {
                version: 0,
                oid: null,
                nimi: {
                    teksti: [{
                        value: '',
                        kieliKoodi: 'fi'
                    }, {
                       value: '',
                       kieliKoodi: 'sv'
                    }]
                },
                sovellettavatOrganisaatios: [],
                sovellettavatOppilaitostyyppis: [],
                allLisatietokenttas: []
            };
            yhteystietotyypit.push(obj);
            return obj;
        };

        this.save = function(ytt, callback, virheCallback) {
            var fn;
            if (ytt.oid === null) {
                fn = Yhteystietojentyyppi.put;
            } else {
                fn = Yhteystietojentyyppi.post;
            }
            fn(ytt, function(tyyppi) {
                var ind = yhteystietotyypit.indexOf(ytt);
                if (ind !== -1) {
                    yhteystietotyypit.splice(ind, 1);
                }
                yhteystietotyypit.push(tyyppi);
                callback(tyyppi);
            }, virheCallback);
        };

        this.delete = function(ytt, callback, virheCallback) {
            YhteystietojentyypinPoisto.delete({oid: ytt.oid}, callback, virheCallback);
        };

        function clearOppilaitostyypitMap() {
            for (var k in oppilaitostyypitMap) {
                delete oppilaitostyypitMap[k];
            }
        }

        this.reload = function() {
            yhteystietotyypit.splice(0, yhteystietotyypit.length);
            oppilaitostyypit.splice(0, oppilaitostyypit.length);
            clearOppilaitostyypitMap();
            loadOppilaitostyypit();
        };

    };

    return model;
});
