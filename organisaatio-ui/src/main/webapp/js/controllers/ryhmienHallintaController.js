function RyhmienHallintaController($scope, $filter, RyhmienHallintaModel, Alert, UserInfo) {
    UserInfo.then(function(s) {
        language = s.lang;
    });

    $scope.model = RyhmienHallintaModel;
    $scope.currentGroup = null;

    $scope.luoUusi = function() {
    };

    $scope.poista = function() {
    };

    $scope.tallenna = function() {
    };

    $scope.peruuta = function() {
        $scope.currentGroup = null;
        $scope.model.reload();
    };
}
