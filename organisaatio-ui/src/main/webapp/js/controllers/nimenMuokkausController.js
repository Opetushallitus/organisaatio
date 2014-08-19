function NimenMuokkausController($scope, $modalInstance, $log, NimenMuokkausModel, nimihistoria, organisaatioAlkuPvm) {
    $scope.modeChanged = function(mode) {
        if (mode === "update") {
            $scope.model.setUusinNimiVisible();
        }
        else {
            $scope.model.clearVisibleNimi();
        }
    };

    $scope.model = NimenMuokkausModel;
    $scope.model.refresh(nimihistoria, organisaatioAlkuPvm);
    $scope.mode = "update";
    $scope.modeChanged($scope.mode);


    $scope.tallenna = function() {
        uusiNimi = {
            "nimi" : {
                "fi" : $scope.nimi_fi,
                "sv" : $scope.nimi_sv,
                "en" : $scope.nimi_en
            },
            "alkuPvm" : $scope.alkupvm
        };
        $log.debug('Tallenna: ' + $scope.mode + ' '+ uusiNimi);
        if ($scope.mode === "update") {
        }
        else {
            NimenMuokkaus.put({oid: "1.2.246.562.10.2014031214412603544642"}, uusiNimi, function(result) {
                $log.log(result);
            },
            // Error case
            function(response) {
                $log.error("Nimet put response: " + response.status);
                Alert.add("error", $filter('i18n')("Nimenmuokkaus.uusinimi.virhe", ""), true);
            });
        }
    };
}
