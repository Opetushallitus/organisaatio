function NimenMuokkausController($scope, $modalInstance, $log, NimenMuokkausModel, oid, nimihistoria, organisaatioAlkuPvm) {

    $scope.model = NimenMuokkausModel;
    $scope.model.refresh(oid, nimihistoria, organisaatioAlkuPvm);

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
        $scope.model.clear();
    };

    $scope.accept = function() {
        $modalInstance.close($scope.model);
    };
}
