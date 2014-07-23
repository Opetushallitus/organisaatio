function RyhmienHallintaController($scope, $filter, $routeParams, $log, RyhmienHallintaModel, Organisaatio, Aliorganisaatiot, Alert, UserInfo, RyhmaKoodisto) {
    UserInfo.then(function(s) {
        language = s.lang;
    });

    $scope.model = RyhmienHallintaModel;
    $scope.currentGroup = null;

    $scope.koodisto = RyhmaKoodisto;

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
                $scope.form.$setPristine();
            }, function(error) {
                Alert.add("error", $filter('i18n')(error.data.errorKey || 'generic.error'), false);
            });
        }
    };

    $scope.peruuta = function() {
        $scope.currentGroup = null;
        $scope.model.reload($routeParams.parentoid, function(result) {
            $scope.form.$setPristine();
        }, function(error) {
            Alert.add("error", error, false);
        });
    };

    $scope.model.reload($routeParams.parentoid, function(result) {
    }, function(error) {
        Alert.add("error", error, false);
    });
}
