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

app.controller('RyhmienHallintaController', function RyhmienHallintaController($scope, $location, $filter, $routeParams,
                                   $uibModal, $log, $injector, $q,
                                   RyhmienHallintaModel, Alert, UserInfo,
                                   RyhmaKoodisto) {
    "use strict";

    $log = $log.getInstance("RyhmienHallintaController");
    var loadingService = $injector.get('LoadingService');

    var language;

    var vaihtoehtoisetKielikoodit = {
        fi: ['sv', 'en'],
        sv: ['fi', 'en'],
        en: ['fi', 'sv']
    };

    UserInfo.then(function(s) {
        language = s.toLowerCase();
    });

    $scope.model = RyhmienHallintaModel;
    $scope.currentGroup = null;
    $scope.currentGroupSelection = {};

    $scope.koodisto = RyhmaKoodisto;

    $scope.updateUpdateInfo = function() {
        if ($scope.currentGroup === null || $scope.currentGroup.oid === null) {
            $scope.model.clearUpdateInfo();
        }
        else {
            $scope.model.loadUpdateInfo($scope.currentGroup.oid, function(result) {
            }, function(error) {
                loadingService.onErrorHandled(error);
                Alert.add("error", error, false);
            });
        }
    };

    $scope.valitseRyhma = function(group) {
        $scope.currentGroup = group;
        $scope.updateUpdateInfo();
    };

    $scope.localizeNimi = function(ryhma) {
        if (ryhma && ryhma !== undefined && 'nimi' in ryhma) {
            return ryhma.nimi[language] ||
                    ryhma.nimi[vaihtoehtoisetKielikoodit[language][0]] ||
                    ryhma.nimi[vaihtoehtoisetKielikoodit[language][1]];
        }
        return '';
    };

    $scope.luoUusi = function() {
        $scope.currentGroup = $scope.model.create($routeParams.parentoid);
        $scope.updateUpdateInfo();
        $scope.currentGroupSelection = {};
    };

    $scope.poista = function() {
        if ($scope.currentGroup !== null) {
            var modalInstance = $uibModal.open({
                templateUrl: 'ryhmanpoisto.html',
                controller: 'RyhmaDeleteController',
                resolve: {
                    nimi: function () {
                        return $scope.localizeNimi($scope.currentGroup);
                    }
                },
                scope: $scope
            });

            modalInstance.result.then(function() {
                $log.debug('Ryhmän poisto vahvistettu');

                $scope.model.delete($scope.currentGroup, function(result) {
                    $scope.currentGroup = null;
                    $scope.updateUpdateInfo();
                    $scope.currentGroupSelection = {};
                }, function(response) {
                    loadingService.onErrorHandled(response);
                    $log.warn("Failed to delete group: ", $scope.currentGroup);
                    Alert.add("error", $filter('i18n')("Ryhmienhallinta.poistoVirhe", "") + ' '
                        + $filter('i18n')(response.data.errorKey), true);
                });
            }, function () {
                $log.debug('Ryhmän poisto peruttu');
            });
        }
    };

    $scope.tallenna = function() {
        $log.info("tallenna(): ", $scope.currentGroup);

        var deferred = $q.defer();

        if ($scope.currentGroup !== null) {
            $scope.model.save($scope.currentGroup, function(savedGroup) {
                $scope.currentGroup = savedGroup;
                $scope.updateUpdateInfo();
                $scope.currentGroupSelection = {};
                $scope.currentGroupSelection.selected = savedGroup;
                $scope.form.$setPristine();
                deferred.resolve();
            }, function(error) {
                loadingService.onErrorHandled(error);
                $log.warn("Failed to save group: ", $scope.currentGroup);
                Alert.add("error", $filter('i18n')(error.data.errorKey || 'generic.error'), false);
                deferred.reject();
            });
        }
        return deferred.promise;
    };

    $scope.peruuta = function() {
        $scope.currentGroup = null;
        $scope.updateUpdateInfo();
        $scope.currentGroupSelection = {};
        $scope.model.reload($routeParams.parentoid, function(result) {
            $scope.form.$setPristine();
        }, function(error) {
            loadingService.onErrorHandled(error);
            $log.warn("Failed to reloud groups: ", $routeParams.parentoid);
            Alert.add("error", error, false);
        });
    };

    $scope.model.reload($routeParams.parentoid, function(result) {
    }, function(error) {
        loadingService.onErrorHandled(error);
        Alert.add("error", error, false);
    });

    // Siirtyminen organisaatioiden pääsivulle organisaatiopuu näkymään
    $scope.cancel = function() {
        $location.path("/");
    };

    // Käsitellään muokkausnäkymästä poistuminen
    $scope.$on("$locationChangeStart", function(event, next, current) {
        // Tallennetaan next url ja kysytään käyttäjältä haluaako siirtyä vai jatkaa.
        // Jos käyttäjä haluaa siirtyä seuraavalle sivulle --> location change
        var next = next;
        $log.log("Location change: " + current +" -> " + next);

        var changeLocation = function() {
            $log.debug('Poistutaan ryhmienhallinnasta');
            $scope.modalOpen = false;
            $scope.form.$setPristine();
            $location.path(next);
        };

        if ($scope.form.$dirty) {
            event.preventDefault();
            $scope.modalOpen = true;
            var modalInstance = $uibModal.open({
                templateUrl: 'organisaationmuokkauksenperuutus.html',
                controller: 'OrganisaatioCancelController',
                resolve: {
                    invalid: function () {
                        return $scope.form.$invalid;
                    }
                },
                scope: $scope
            });

            // Jos varmistuskyselyssä käyttäjä haluaa tallentaa muokatun
            // organisaation, niin odotetaan tallennusvaihe loppuun ja
            // siirrytää vasta sitten uuteen osoitteeseen.
            modalInstance.result.then(function (save) {
                if (save) {
                    $scope.tallenna().then(function() {
                        changeLocation();
                    }, function(reason) {
                        changeLocation();
                    });
                }
                else {
                   changeLocation();
                }
            }, function() {
                $scope.modalOpen = false;
                $log.debug('Jatketaan ryhmienhallintaa');
            });
        }
    });
});
