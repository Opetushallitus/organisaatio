function OrganisaatioTreeController($scope, $location, $filter,
                                    $modal, $log, Alert, Organisaatio,
                                    HakuehdotModel, OrganisaatioTreeModel) {
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
        // Tarkistetaan ettei ole aliorganisaatioita
        return $scope.model.isLeaf(node);
    };

    $scope.isCreateSubAllowed = function(node) {
        // Vain OPH-käyttäjä saa luoda alaorganisaation koulutustoimijalle
        return ($scope.hakuehdot.organisaatioRajausVisible===false) ?
                node.tyyppi!=="Oppisopimustoimipiste" :
                node.tyyppi!=="Koulutustoimija" && node.tyyppi!=="Muu organisaatio" && node.tyyppi!=="Oppisopimustoimipiste";
    };

    $scope.deleteOrganisaatio = function (node) {
        var modalInstance = $modal.open({
            templateUrl: 'organisaationpoisto.html',
            controller: OrganisaatioDeleteController,
            resolve: {
                nimi: function () {
                    return $scope.model.getNimi(node);
                },
                tyyppi:  function () {
                    return $scope.model.getTyyppi(node);
                }
            }
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
                $log.error("Organisaatio delete response: " + response.status);
                Alert.add("error", $filter('i18n')("Organisaationpoisto.poistoVirhe", ""), true);
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
        var modalInstance = $modal.open({
            templateUrl: 'yritysvalinta.html',
            controller: YritysValintaController,
            windowClass:'modal-wide',
            resolve: {
                // return undefined --> ei ytunnuksen esivalintaa
                ytunnus: function () {
                    return;
                }
            }
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
}
