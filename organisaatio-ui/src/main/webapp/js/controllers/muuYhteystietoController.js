function MuuYhteystietoController($scope, $modalInstance, data) {
    if (data) {
        $scope.data = data;
    } else {
        $scope.data = {
            fi: '',
            sv: ''
        };
    }

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };

    $scope.save = function() {
        $modalInstance.close($scope.data);
    };
}

