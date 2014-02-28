function MuuYhteystietoController($scope, $modalInstance) {
    $scope.data = {
        fi: '',
        sv: ''
    };
    
    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };
    
    $scope.save = function() {
        $modalInstance.close($scope.data);
    };
}

