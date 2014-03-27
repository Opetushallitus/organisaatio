function RyhmienHallintaController($scope, $filter, $routeParams, $log, RyhmienHallintaModel, Organisaatio, Alert, UserInfo) {
    UserInfo.then(function(s) {
        language = s.lang;
    });

    $scope.model = RyhmienHallintaModel;
    $scope.currentGroup = null;

    $scope.$watch('model', function(m) {
        $log.debug(m);
    });

    $scope.localizeNimi = function(ryhma) {
        for (var k in ryhma.nimi) {
            if (k === language.toLowerCase()) {
                return (ryhma.nimi[k] === null ? "" : ryhma.nimi[k]);
            }
        }
    };

    $scope.luoUusi = function() {
        $scope.currentGroup = $scope.model.create($routeParams.parentoid);
    };

    $scope.poista = function() {
        if ($scope.currentGroup !== null) {
            $scope.model.delete($scope.currentGroup, function(result) {
                $scope.currentGroup = null;
            }, function(error) {
                Alert.add("error", error, false);
            });
        }
    };

    $scope.tallenna = function() {
        if ($scope.currentGroup !== null) {
            $scope.model.save($scope.currentGroup, function(savedGroup) {
                $scope.currentGroup = savedGroup;
            }, function(error) {
                Alert.add("error", error, false);
            });
        }
    };

    $scope.peruuta = function() {
        $scope.currentGroup = null;
        $scope.model.reload();
    };
}
