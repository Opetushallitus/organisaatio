function NimenMuokkausController($scope, $modalInstance, $log, NimenMuokkausModel,
                                 oid, nimihistoria, organisaatioAlkuPvm,
                                 koulutustoimija, oppilaitos, parentNimi,
                                 nameFormat, parentPattern) {

    $scope.model = NimenMuokkausModel;
    $scope.model.refresh(oid, nimihistoria, organisaatioAlkuPvm,
                         koulutustoimija, oppilaitos, parentNimi,
                         nameFormat, parentPattern);

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
        $scope.model.clear();
    };

    $scope.accept = function() {
        $modalInstance.close($scope.model);
    };
}
