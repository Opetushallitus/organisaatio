function OrganisaatioController($scope, $location, $routeParams, OrganisaatioModel) {
    $scope.oid = $routeParams.oid;
    $scope.model = OrganisaatioModel;
    $scope.model.refreshIfNeeded($scope.oid);
    $scope.model.mode = "show";
    
    if (/new$/.test($location.path())) {
        $scope.model.mode = "new";
        $scope.model.createOrganisaatio($routeParams.parentoid);
    } else if (/edit$/.test($location.path())) {
        $scope.model.mode = "edit";
        $scope.model.refreshKoodisto($scope.oid);
    }

    $scope.save = function() {
        $scope.model.persistOrganisaatio($scope.form);
    }

    $scope.cancel = function() {
        $location.path("/");
    }

    $scope.edit = function () {
      $location.path($location.path() + "/edit");
    };

}
