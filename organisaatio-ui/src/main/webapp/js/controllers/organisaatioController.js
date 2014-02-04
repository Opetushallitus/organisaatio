function OrganisaatioController($scope, $location, $routeParams, $modal, $log, OrganisaatioModel) {
    $scope.oid = $routeParams.oid;
    $scope.model = OrganisaatioModel;
    $scope.modalOpen = false; // Käytetään piilottamaan tallennuslaatikko, kun modaali dialogi auki
    
    $scope.model.mode = "show";
    if (/new$/.test($location.path())) {
        $scope.model.mode = "new";
        if ('ytunnus' in $routeParams) {
            $log.log("Uusi organisaatio Ytunnuksella: " + $routeParams.ytunnus);
            $scope.model.createOrganisaatioYTunnuksella($routeParams.parentoid, $routeParams.ytunnus);
        }
        else {
            $scope.model.createOrganisaatio($routeParams.parentoid);
        }
    } else if (/edit$/.test($location.path())) {
        $scope.model.mode = "edit";
    }

    $scope.model.refreshIfNeeded($scope.oid);

    $scope.save = function() {
        $scope.model.persistOrganisaatio($scope.form);
    };

    $scope.cancel = function() {
        $location.path("/");
    };

    $scope.edit = function () {
      $location.path($location.path() + "/edit");
    };

    $scope.haeYtjTiedot = function (organisaationYtunnus) {
        var modalInstance = $modal.open({
            templateUrl: 'yritysvalinta.html',
            controller: YritysValintaController,
            windowClass:'modal-wide',
            resolve: {
                ytunnus: function () {
                    return organisaationYtunnus;
                }
            }
        });
        $scope.modalOpen = true;
        
        modalInstance.result.then(function (ytunnus) {
            if (ytunnus) {
                $log.log('Päivitetään organisaation tiedot tiedoilla YTynnukselta: ' + ytunnus);
                $scope.model.updateOrganisaatioYTunnuksella(ytunnus);
            }
            $scope.modalOpen = false;
        }, function () {
            $log.log('Modal dismissed at: ' + new Date());
            $scope.modalOpen = false;
        });
    };

}
