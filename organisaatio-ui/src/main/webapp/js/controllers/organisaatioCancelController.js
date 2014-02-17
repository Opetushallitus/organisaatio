function OrganisaatioCancelController($scope, $modalInstance, $log, OrganisaatioModel) {
    $log.debug("Organisaation muokkauksen peruutus (vaatii vahvistuksen)");
    $scope.model = OrganisaatioModel;

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };

    $scope.continueWithoutSave = function() {
        $modalInstance.close();
    };

    $scope.continueSave = function() {
        $scope.model.persistOrganisaatio();
        $modalInstance.close();
    };
}