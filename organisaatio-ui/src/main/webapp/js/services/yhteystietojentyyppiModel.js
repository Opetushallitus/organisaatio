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

app.factory('YhteystietojentyyppiModel', function($log, $injector, $filter,
                                                  Alert,
                                                  KoodistoOppilaitostyypit,
                                                  KoodistoKoodi,
                                                  Yhteystietojentyyppi,
                                                  YhteystietojentyypinPoisto)  {

    $log = $log.getInstance("YhteystietojentyyppiModel");
    var loadingService = $injector.get('LoadingService');

    var model = new function() {
        this.oppilaitostyypit = [];
        this.yhteystietotyypit = [];
        this.oppilaitostyypitMap = {};

        var loadYhteystietotyypit = function() {
            Yhteystietojentyyppi.get({}, function(tyypit) {
                tyypit.forEach(function(t) {
                    model.yhteystietotyypit.push(t);
                });
            },
            // Error case
            function(response) {
                loadingService.onErrorHandled();
                $log.error("Yhteystietojentyyppi response: " + response.status);
                Alert.add("error", $filter('i18n')("Yhteystietotyypit.hakuvirhe", ""), true);
            });
        };

        var loadOppilaitostyypit =  function() {
            KoodistoOppilaitostyypit.get({onlyValidKoodis:true}, function(tyypit) {
                tyypit.forEach(function(t) {
                    model.oppilaitostyypit.push({id: t.koodiUri + '#' + t.versio, nimi: KoodistoKoodi.getLocalizedName(t)});
                    model.oppilaitostyypitMap[t.koodiUri + '#' + t.versio] = KoodistoKoodi.getLocalizedName(t);
                });
                loadYhteystietotyypit();
            },
            // Error case
            function(response) {
                loadingService.onErrorHandled();
                $log.error("KoodistoOppilaitostyypit response: " + response.status);
                Alert.add("error", $filter('i18n')("Organisaatiot.koodistovirhe", ""), true);
            });
        };

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
            model.yhteystietotyypit.push(obj);
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
                var ind = model.yhteystietotyypit.indexOf(ytt);
                if (ind !== -1) {
                    model.yhteystietotyypit.splice(ind, 1);
                }
                model.yhteystietotyypit.push(tyyppi);
                callback(tyyppi);
            }, virheCallback);
        };

        this.delete = function(ytt, callback, virheCallback) {
            YhteystietojentyypinPoisto.delete({oid: ytt.oid}, callback, virheCallback);
        };

        function clearOppilaitostyypitMap() {
            for (var k in model.oppilaitostyypitMap) {
                delete model.oppilaitostyypitMap[k];
            }
        }

        this.reload = function() {
            model.yhteystietotyypit.splice(0, model.yhteystietotyypit.length);
            model.oppilaitostyypit.splice(0, model.oppilaitostyypit.length);
            clearOppilaitostyypitMap();
            loadOppilaitostyypit();
        };

    };

    return model;
});
