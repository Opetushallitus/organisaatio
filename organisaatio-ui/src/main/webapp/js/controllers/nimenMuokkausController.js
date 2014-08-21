function NimenMuokkausController($scope, $modalInstance, $log, NimenMuokkausModel, oid, nimihistoria, organisaatioAlkuPvm) {

    $scope.model = NimenMuokkausModel;
    $scope.model.refresh(oid, nimihistoria, organisaatioAlkuPvm);

    $scope.tallenna = function() {
        $log.log('Tallenna mode: ' + $scope.model.mode);
        if ($scope.model.mode === "update") {
            $scope.model.saveUpdatedNimi();
        }
        else {
            $scope.model.saveNewNimi();
        }
    };
}
