function OrganisaatioController($scope, $location, $routeParams, $log, OrganisaatioModel) {
    $scope.oid = $routeParams.oid;
    $scope.model = OrganisaatioModel;
    
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

}
