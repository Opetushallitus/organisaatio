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

app.factory('RyhmaKoodisto', function($q, $log, $filter, $injector,
                                      KoodistoArrayByUri, KoodistoKoodi, Alert) {

    $log = $log.getInstance("RyhmaKoodisto");
    var loadingService = $injector.get('LoadingService');

    var showAndLogError = function(msg, response) {
        loadingService.onErrorHandled();
        model.alert = Alert.add("error", $filter('i18n')(response.data.errorKey || msg), false);
        $log.error(msg + " (status: " + response.status + ")");
    };

    /*
     * Palauttaa koodiston arrayna, jonka itemit muotoa { uri: <koodiuri>, nimi: <lokalisoitu nimi> }
     * Parametrit:
     *   uri: koodistoUri
     *   resultArray: Array johon koodi-itemit tallennetaan
     *   defaultArray: jos tyyppiä Array, palautetaan virhetilanteessa eikä näytetä virheilmoitusta.
     *                 Jos muu kuin Array, näytetään virheilmoitus.
     */
    var getKoodistoArray = function(uri, resultArray, defaultArray) {
        /* Poistetaan koodikommentti kun toteutetaan: https://jira.oph.ware.fi/jira/browse/OVT-8892
        KoodistoArrayByUri.get({uri: uri}, function(result) {
            resultArray.length = 0;
            result.forEach(function(rTyyppiKoodi) {
                resultArray.push({uri: rTyyppiKoodi.koodiUri + "#" + rTyyppiKoodi.versio,
                                  nimi: KoodistoKoodi.getLocalizedName(rTyyppiKoodi)});
            });
        }, function(response) {
            // koodeja ei löytynyt
            if (defaultArray instanceof Array) {
                defaultArray.forEach(function(rTyyppiKoodi) {
                    resultArray.push(rTyyppiKoodi);
                });
            } else {
                showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
            }
        });
        **/
        // Käytetään default arvoja, kunnes koodistossa tarvittava koodistot: ks. OVT-8892
        if (defaultArray instanceof Array) {
            defaultArray.forEach(function(rTyyppiKoodi) {
                resultArray.push(rTyyppiKoodi);
            });
        } else {
            showAndLogError("Organisaationtarkastelu.koodistohakuvirhe");
        }
    };

    var model = new function() {
        this.ryhmatyypit = [];
        this.kayttoryhmat = [];

        var koodistoArrays = [
            {
                uri: 'ryhmatyypit',
                resultArray: this.ryhmatyypit,
                defaultArray: [
                    // Default-arvoja käytetään kunnes koodistoon lisätään 'ryhmatyyppi'-koodisto
                    // Default-arvojen lähde: Confluence / Ryhmien määrittely
                    {uri: 'organisaatio', nimi: $filter('i18n')("Ryhmienhallinta.organisaatio", "")},
                    {uri: 'hakukohde', nimi: $filter('i18n')("Ryhmienhallinta.hakukohde", "")},
                    {uri: 'perustetyoryhma', nimi: $filter('i18n')("Ryhmienhallinta.perustetyoryhma", "")},
                    {uri: 'koulutus', nimi: $filter('i18n')("Ryhmienhallinta.koulutus", "")}
                ]
            },
            {
                uri: 'kayttoryhmat',
                resultArray: this.kayttoryhmat,
                // Default-arvoja käytetään kunnes koodistoon lisätään 'kayttoryhma'-koodisto
                // Default-arvojen lähde: Confluence / Ryhmien määrittely
                defaultArray: [
                    {uri: 'yleinen', nimi: $filter('i18n')("Ryhmienhallinta.yleinen", "")},
                    {uri: 'hakukohde_rajaava', nimi: $filter('i18n')("Ryhmienhallinta.rajaava", "")},
                    {uri: 'hakukohde_priorisoiva', nimi: $filter('i18n')("Ryhmienhallinta.priorisoiva", "")},
                    {uri: 'hakukohde_liiteosoite', nimi: $filter('i18n')("Ryhmienhallinta.liiteosoite", "")},
                    {uri: 'perusteiden_laadinta', nimi: $filter('i18n')("Ryhmienhallinta.perusteidenlaadinta", "")},
                    {uri: 'kayttooikeus', nimi: $filter('i18n')("Ryhmienhallinta.kayttooikeus", "")}
                ]
            }
        ];

        koodistoArrays.forEach(function(koodistoItem) {
            getKoodistoArray(koodistoItem.uri, koodistoItem.resultArray, koodistoItem.defaultArray);
        });

    };
    return model;
});
