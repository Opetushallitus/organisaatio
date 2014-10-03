function NimenMuokkausController($scope, $modalInstance, $log, NimenMuokkausModel,
                                 oid, nimihistoria, organisaatioAlkuPvm,
                                 koulutustoimija, oppilaitos, parentNimi,
                                 nameFormat, parentPattern) {

    $scope.model = NimenMuokkausModel;
    $scope.model.refresh(oid, nimihistoria, organisaatioAlkuPvm,
                         koulutustoimija, oppilaitos, parentNimi,
                         nameFormat);

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
        $scope.model.clear();
    };

    $scope.accept = function() {
        $modalInstance.close($scope.model);
        $scope.model.accept();
    };

    $scope.newNimiMode = function(form) {
        $log.debug('NimenMuokkausController::newNimiMode()');
        $scope.model.clearVisibleNimi();
    };

    $scope.updateNimiMode = function(form) {
        $log.debug('NimenMuokkausController::updateNimiMode()');
        $scope.model.setUusinNimiVisible();
        $log.debug($scope);
        if ($scope.model.isUusinNimiChanged() === false) {
            form.$setPristine();
        }
    };

    $scope.cancelNimenMuutosMode = function(form) {
        $log.debug('NimenMuokkausController::cancelNimenMuutosMode()');
        $scope.model.setUusinNimiVisible();
    };


}
