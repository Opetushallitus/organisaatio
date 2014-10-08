function NimenMuokkausController($scope, $modalInstance, $log, NimenMuokkausModel,
                                 oid, nimihistoria, organisaatioAlkuPvm,
                                 koulutustoimija, oppilaitos, parentNimi,
                                 nameFormat, parentPattern) {

    $log = $log.getInstance("NimenMuokkausController");

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
        $log.debug('newNimiMode()');
        $scope.model.clearVisibleNimi();
    };

    $scope.updateNimiMode = function(form) {
        $log.debug('updateNimiMode()');
        $scope.model.setUusinNimiVisible();
        if ($scope.model.isUusinNimiChanged() === false) {
            form.$setPristine();
        }
    };

    $scope.cancelNimenMuutosMode = function(form) {
        $log.debug('cancelNimenMuutosMode()');
        $scope.model.setUusinNimiVisible();
    };


}
