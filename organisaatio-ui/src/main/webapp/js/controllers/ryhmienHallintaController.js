function RyhmienHallintaController($scope, $filter, $routeParams, RyhmienHallintaModel, Alert, UserInfo, RyhmaKoodisto) {
    "use strict";
    var language;

    var vaihtoehtoisetKielikoodit = {
        fi: ['sv', 'en'],
        sv: ['fi', 'en'],
        en: ['fi', 'sv']
    };

    UserInfo.then(function(s) {
        language = s.lang.toLowerCase();
    });

    $scope.model = RyhmienHallintaModel;
    $scope.currentGroup = null;

    $scope.koodisto = RyhmaKoodisto;

    $scope.localizeNimi = function(ryhma) {
        return ryhma.nimi[language] || ryhma.nimi[vaihtoehtoisetKielikoodit[language][0]] || ryhma.nimi[vaihtoehtoisetKielikoodit[language][1]];
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
