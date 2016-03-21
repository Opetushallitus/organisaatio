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

app.controller('OrganisaatioMoveController', function OrganisaatioMoveController($scope, $uibModalInstance, $filter, $log,
                                    OrganisaatiotFlat, Organisaatio,
                                    Alert, LocalisationService,
                                    nimi, node) {


    $log = $log.getInstance("OrganisaatioMoveController");

    $scope.nimi = nimi;
    $scope.suggests = [];
    $scope.koulutustoimija = false;
    $scope.oppilaitos = false;

    $scope.siirtoKohdeTitle = $filter('i18n')("organisaatio.move.new.parent.organization");

    $scope.options = {
        newParentOrganization: null,
        merge: false,
        date: new Date()
    };

    function isKoulutustoimija() {
        var currentOrganizationTypes = $scope.options.organisaatio.tyypit;
        return currentOrganizationTypes.indexOf("Koulutustoimija") > -1;
    }

    function isOppilaitos() {
        var currentOrganizationTypes = $scope.options.organisaatio.tyypit;
        return currentOrganizationTypes.indexOf("Oppilaitos") > -1;
    }

    function getNimi(organisaatio) {
        if (LocalisationService.getLocale() in organisaatio.nimi &&
                organisaatio.nimi[LocalisationService.getLocale()]) {
            return organisaatio.nimi[LocalisationService.getLocale()];
        }

        // Ei löytynyt nimeä käyttäjän kielellä, kokeillaan muut vaihtoehdot
        if ('fi' in organisaatio.nimi && organisaatio.nimi.fi) {
            return organisaatio.nimi.fi;
        }
        if ('sv' in organisaatio.nimi && organisaatio.nimi.sv) {
            return organisaatio.nimi.sv;
        }
        if ('en' in organisaatio.nimi && organisaatio.nimi.en) {
            return organisaatio.nimi.en;
        }
        return "--";
    }

    function getTunnus(organisaatio) {
        if ('oppilaitosKoodi' in organisaatio) {
            return organisaatio.oppilaitosKoodi;
        }
        if ('ytunnus' in organisaatio) {
            return organisaatio.ytunnus;
        }
        return null;
    };

    function updateSearch() {
        var organizationType = "";
        $scope.koulutustoimija = isKoulutustoimija();
        $scope.oppilaitos = isOppilaitos();

        if ($scope.koulutustoimija) {
            // Koulutustoimijan tapauksessa voidaan tehdä vain liitos toiseen
            // koulutustoimijaan
            organizationType = 'Koulutustoimija';
            $scope.siirtoKohdeTitle = $filter('i18n')("organisaatio.move.new.parent.koulutustoimija");
            $scope.options.merge = true;
        } else if ($scope.oppilaitos) {
            // Jos liitetään oppilaitos, niin liitos voi tapahtua vain toiseen
            // oppilaitokseen. Jos taas siirretään oppilaitos, se voidaan siirtää
            // vain koulutustoimijan alle.
            if ($scope.options.merge) {
                organizationType = 'Oppilaitos';
                $scope.siirtoKohdeTitle = $filter('i18n')("organisaatio.move.new.parent.oppilaitos");
            }
            else {
                organizationType = 'Koulutustoimija';
                $scope.siirtoKohdeTitle = $filter('i18n')("organisaatio.move.new.parent.koulutustoimija");
            }
        }
        else {
            $log.warn("Virheellinen organisaatiotyyppi organisaationsiirrossa: ",
                $scope.options.organisaatio.tyypit);
        }

        var parametrit = {"searchstr": "",
                          "organisaatiotyyppi": organizationType,
                          "aktiiviset": true,
                          "suunnitellut": true,
                          "lakkautetut": false};
        OrganisaatiotFlat.get(parametrit, function (result) {
            var values = result.organisaatiot.map(function (org) {
                // Tarkistetaan, ettei organisaatiota yritetä sulauttaa itseensä
                // eikä siirtää jo olemassa olevan parentin alle.
                if (org.oid !== node.oid && org.oid !== node.parentOid) {
                    return {
                        "name": getNimi(org),
                        "oid": org.oid,
                        "tunnus": getTunnus(org),
                        "nameTunnus": getNimi(org)+ " " + getTunnus(org),
                        "nameTunnusBrackets": getNimi(org)+ " (" + getTunnus(org) + ")"
                    };
                }
            });

            $scope.suggests = $filter('orderBy')(values, 'name');
        }, function (error) {
            $log.error("Organisaatioiden lataus epäonnistui ", error);
            Alert.add("error", error, false);
        });
    }

    $scope.updateSearch = updateSearch;

    Organisaatio.get({oid: node.oid}, function (result) {
            $scope.options.organisaatio = result;
            updateSearch();
        }, function (error) {
            $log.error("organisaation tietojen lataus epäonnistui ", error);
            Alert.add("error", error, false);
        }
    );
});