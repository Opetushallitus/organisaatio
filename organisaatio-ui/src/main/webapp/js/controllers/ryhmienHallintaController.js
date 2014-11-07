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

function RyhmienHallintaController($scope, $filter, $routeParams, $modal,
                                   $log, $injector,
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
        language = s.lang.toLowerCase();
    });

    $scope.model = RyhmienHallintaModel;
    $scope.currentGroup = null;

    $scope.koodisto = RyhmaKoodisto;

    $scope.localizeNimi = function(ryhma) {
        return ryhma.nimi[language] ||
                ryhma.nimi[vaihtoehtoisetKielikoodit[language][0]] ||
                ryhma.nimi[vaihtoehtoisetKielikoodit[language][1]];
    };

    $scope.luoUusi = function() {
        $scope.currentGroup = $scope.model.create($routeParams.parentoid);
    };

    $scope.poista = function() {
        if ($scope.currentGroup !== null) {
            var modalInstance = $modal.open({
                templateUrl: 'ryhmanpoisto.html',
                controller: RyhmaDeleteController,
                resolve: {
                    nimi: function () {
                        return $scope.localizeNimi($scope.currentGroup);
                    }
                }
            });

            modalInstance.result.then(function() {
                $log.debug('Ryhmän poisto vahvistettu');

                $scope.model.delete($scope.currentGroup, function(result) {
                    $scope.currentGroup = null;
                }, function(error) {
                    loadingService.onErrorHandled();
                    $log.warn("Failed to delete group: ", $scope.currentGroup);
                    Alert.add("error", error, false);
                });
            }, function () {
                $log.debug('Ryhmän poisto peruttu');
            });
        }
    };

    $scope.tallenna = function() {
        if ($scope.currentGroup !== null) {
            $scope.model.save($scope.currentGroup, function(savedGroup) {
                $scope.currentGroup = savedGroup;
                $scope.form.$setPristine();
            }, function(error) {
                loadingService.onErrorHandled();
                $log.warn("Failed to save group: ", $scope.currentGroup);
                Alert.add("error", $filter('i18n')(error.data.errorKey || 'generic.error'), false);
            });
        }
    };

    $scope.peruuta = function() {
        $scope.currentGroup = null;
        $scope.model.reload($routeParams.parentoid, function(result) {
            $scope.form.$setPristine();
        }, function(error) {
            loadingService.onErrorHandled();
            $log.warn("Failed to reloud groups: ", $routeParams.parentoid);
            Alert.add("error", error, false);
        });
    };

    $scope.model.reload($routeParams.parentoid, function(result) {
    }, function(error) {
        loadingService.onErrorHandled();
        Alert.add("error", error, false);
    });
}
