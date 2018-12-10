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

app.controller('OrganisaatioTreeController', function OrganisaatioTreeController($scope, $location, $filter,
                                    $uibModal, $log, $injector,
                                    Alert, Organisaatio, OrganisaatioSiirto,
                                    HakuehdotModel, OrganisaatioTreeModel) {

    $log = $log.getInstance("OrganisaatioTreeController");

    var loadingService = $injector.get('LoadingService');

    $scope.hakuehdot = HakuehdotModel;
    $scope.model     = OrganisaatioTreeModel;
    $scope.tarkemmatHakuehdotVisible = false;
    $scope.currentOid = '';

    // Kun hakuehdot on initalisoitu, tarkastetaan onko ehdot tyhjät.
    // Jos hakuehdoissa on jo jotain (käytännössä käyttäjän organisaatio), päivitetään näkymä
    $scope.hakuehdot.init().then(function() {
        if ($scope.hakuehdot.isEmpty() === false) {
            $scope.model.refresh($scope.hakuehdot).then (function() {

                // Päivitetään organisaatiorajauksen organisaatioiden nimet mallista
                // Jos vanha organisaatiorajaus voimassa, ei tarvitse tehdä päivitystä
                if (!$scope.hakuehdot.rajatutOrganisaatiotStr) {
                    var organisaatiot = "";
                    angular.forEach($scope.hakuehdot.rajatutOrganisaatiot, function(oid, index){
                        if (index === 0) {
                            organisaatiot = $scope.model.getNimiForOid(oid);
                        }
                        else {
                            organisaatiot = organisaatiot + ", " + $scope.model.getNimiForOid(oid);
                        }
                    });
                    $scope.hakuehdot.rajatutOrganisaatiotStr = organisaatiot;
                }
            });
        }
    });

    $scope.getTimes=function(n){
        return new Array(n);
    };

    $scope.setCurrentOid = function(oid) {
        $scope.currentOid = oid;
    };

    $scope.isCurrentOid = function(oid) {
        return $scope.currentOid === oid;
    };

    $scope.isDeleteAllowed = function(node) {
        return $scope.model.hasChildren(node) === false;
    };

    $scope.isMoveAllowed = function(node) {
        // Oppilaitos || Koulutustoimija
        return $scope.model.isTyyppi(node, "organisaatiotyyppi_02") || $scope.model.isTyyppi(node, "organisaatiotyyppi_01");
    };

    $scope.isCreateSubAllowed = function(node) {
        // Vain OPH-käyttäjä saa luoda alaorganisaation koulutustoimijalle
        return ($scope.hakuehdot.organisaatioRajausVisible===false) ?
                // OPH-käyttäjän tapauksessa oppisopimustoimipisteelle ei voi lisätä aliorganisaatiota
                !$scope.model.isTyyppi(node, "organisaatiotyyppi_04") : // Oppisopimustoimipiste
                // Muiden käyttäjien tapauksessa seuraaville tyypeille ei voi lisätä aliorganisaatioita
                !$scope.model.isTyyppi(node, "organisaatiotyyppi_01") && // Koulutustoimija
                !$scope.model.isTyyppi(node, "organisaatiotyyppi_05") && // Muu organisaatio
                !$scope.model.isTyyppi(node, "organisaatiotyyppi_07") && // Varhaiskasvatuksen jarjestaja
                !$scope.model.isTyyppi(node, "organisaatiotyyppi_08") && // Varhaiskasvatuksen toimipaikka
                !$scope.model.isTyyppi(node, "organisaatiotyyppi_06") && // Tyoelamajarjesto
                !$scope.model.isTyyppi(node, "organisaatiotyyppi_04"); // Oppisopimustoimipiste
    };

    $scope.moveOrganisaatio = function (node) {
        var modalInstance = $uibModal.open({
            templateUrl: 'organisaationsiirto.html',
            controller: 'OrganisaatioMoveController',
            windowClass:'modal-wide',
            resolve: {
                nimi: function () {
                    return $scope.model.getNimi(node);
                },
                node: function () {
                    return node;
                }
            },
            scope: $scope
        });

        modalInstance.result.then(function (options) {
            var confirm = $uibModal.open({
                templateUrl: 'organisaatiosiirtovarmistus.html',
                controller: function ($scope, current, newParent) {
                    $scope.current = current;
                    $scope.newParent = newParent;
                },
                resolve: {
                    current: function () {
                        return $scope.model.getNimi(node);
                    },
                    newParent: function () {
                        return options.newParentOrganization.name;
                    }
                },
                scope: $scope
            });

            confirm.result.then(function () {
                function reply() {
                    $log.info('Organisaatio siirretty osaksi: ' + node.oid);

                    if (!$scope.hakuehdot.isEmpty()) {
                        $scope.model.refresh($scope.hakuehdot);
                    }
                }
                function error(response) {
                    loadingService.onErrorHandled(response);
                    $log.error("Organisaatio siirto response: " + response.status);
                    Alert.add("error", $filter('i18n')("organisaatio.move.error", "") + ' '
                                + $filter('i18n')(response.data.errorKey), true);
                }

                var params = {
                    oid: options.organisaatio.oid,
                    moveDate: moment(options.date).format('YYYY-MM-DD'), // date.type == Date object
                    merge: options.merge
                };
                OrganisaatioSiirto.post(params, options.newParentOrganization.oid, reply, error);
            });
        });
    };

    $scope.deleteOrganisaatio = function (node) {
        var modalInstance = $uibModal.open({
            templateUrl: 'organisaationpoisto.html',
            controller: 'OrganisaatioDeleteController',
            resolve: {
                nimi: function () {
                    return $scope.model.getNimi(node);
                },
                tyypit:  function () {
                    return $scope.model.getTyypit(node);
                }
            },
            scope: $scope
        });

        modalInstance.result.then(function () {
            $log.info('Organisaatio poisto vahvistettu: ' + node.oid);

            Organisaatio.delete({oid: node.oid}, function(result) {
                $log.log(result);

                // Poistetaan organisaatio puumallista
                $scope.model.deleteNode(node);
            },
            // Error case
            function(response) {
                loadingService.onErrorHandled(response);
                $log.error("Organisaatio delete response: " + response.status);
                Alert.add("error", $filter('i18n')("Organisaationpoisto.poistoVirhe", "") + ' '
                                + $filter('i18n')(response.data.errorKey), true);
            });

        }, function () {
            $log.info('Organisaation poistoa ei vahvistettu: ' + node.oid);
        });
    };

    $scope.search = function() {
        if ($scope.hakuehdot.isEmpty()) {
            $log.warn("Hakuehdo tyhjät / liian väljät!");
            Alert.add("warning", $filter('i18n')("Organisaatiot.tarkennaHakuehtoja", ""), true);
            return;
        }
        $scope.model.refresh($scope.hakuehdot);
    };

    $scope.resetHakuehdot = function() {
        $scope.hakuehdot.resetAll();
    };

    $scope.hideTarkemmatHakuehdot = function() {
        $scope.tarkemmatHakuehdotVisible = false;

        // Tarkempien ehtojen piilotus tyhjentää tarkemmat hakukentät
        //$scope.hakuehdot.resetTarkemmatEhdot();
    };

    $scope.showTarkemmatHakuehdot = function() {
        $scope.hakuehdot.refreshIfNeeded();
        $scope.tarkemmatHakuehdotVisible = true;
    };

    $scope.luoYlinTaso = function () {
        var modalInstance = $uibModal.open({
            templateUrl: 'yritysvalinta.html',
            controller: 'YritysValintaController',
            windowClass:'modal-wide',
            resolve: {
                // return undefined --> ei ytunnuksen esivalintaa
                ytunnus: function () {
                    return;
                }
            },
            scope: $scope
        });

        modalInstance.result.then(function (ytunnus) {
            if (ytunnus) {
                $log.log('Luodaan uusi organisaatio YTynnuksella: ' + ytunnus);
                $location.search('ytunnus',ytunnus).path($location.path() +
                        "/" + ROOT_ORGANISAATIO_OID +"/new");
            }
            else {
                $location.path($location.path() + "/" + ROOT_ORGANISAATIO_OID + "/new");
            }
        }, function () {
            $log.log('Modal dismissed at: ' + new Date());
        });
    };

    $scope.ryhmienHallinta = function() {
        $location.path($location.path() + "/" + ROOT_ORGANISAATIO_OID + "/groups");
    };

    $scope.organisaatiotyyppiChanged = function() {
        if ($scope.hakuehdot.organisaatiotyyppi !== 'organisaatiotyyppi_02') {
            $scope.hakuehdot.oppilaitostyyppiKoodi = '';
        }
    };

    $scope.isOppilaitosSelected = function() {
        return $scope.hakuehdot.organisaatiotyyppi === 'organisaatiotyyppi_02';
    };
});
