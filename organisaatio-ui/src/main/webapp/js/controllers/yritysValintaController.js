function YritysValintaController($scope, $modalInstance, $location, YritysValintaModel) {
    $scope.model = YritysValintaModel;
    
    $scope.Ytunnus = "";
    
    $scope.select = function () {
        $modalInstance.close($scope.Ytunnus);
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