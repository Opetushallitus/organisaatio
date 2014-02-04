function YritysValintaController($scope, $modalInstance, $location, $log, YritysValintaModel, ytunnus) {
    $scope.model = YritysValintaModel;
    
    if (ytunnus) {
        $log.debug("Yritysvalinta ytunnuksella: " + ytunnus);
        $scope.model.hakuString = ytunnus;
    }
    
    $scope.select = function (yritys) {
        $log.info(yritys);
        $modalInstance.close(yritys.ytunnus);
    };
    
    $scope.search = function() {
        $scope.model.refresh();
    };
    
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
    
    $scope.continueWithouSelect = function () {
        $modalInstance.close();
    };
}