function YritysValintaController($scope, $modalInstance, $location, $log, YritysValintaModel) {
    $scope.model = YritysValintaModel;
    
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
        $modalInstance.dismiss('continue');
        $location.path($location.path() + "/" + ROOT_ORGANISAATIO_OID + "/new");  
    };
}