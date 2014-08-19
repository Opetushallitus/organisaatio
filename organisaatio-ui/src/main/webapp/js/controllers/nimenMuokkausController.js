function NimenMuokkausController($scope, $modalInstance, $log, NimenMuokkausModel, nimihistoria, organisaatioAlkuPvm) {

    $scope.model = NimenMuokkausModel;
    $scope.model.refresh(nimihistoria, organisaatioAlkuPvm);

    $scope.tallenna = function() {
        $log.log('Tallenna mode: ' + $scope.mode);
        if ($scope.mode === "update") {
            $scope.model.saveUpdatedNimi("1.2.246.562.10.2014031214412603544642");
        }
        else {
            $scope.model.saveNewNimi("1.2.246.562.10.2014031214412603544642");
        }
    };
}
