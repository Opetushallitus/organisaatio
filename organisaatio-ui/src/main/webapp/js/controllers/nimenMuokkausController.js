function NimenMuokkausController($scope, $modalInstance, $log, nimihistoria, organisaatioAlkuPvm) {
    // Haetaan Nimihistorian uusin nimi
    uusinNimi = function() {
        var nimi = nimihistoria[0];
        for(var i=0; i < nimihistoria.length; i++) {
            if (moment(nimihistoria[i].alkuPvm).isAfter(moment(nimi.alkuPvm))) {
                nimi = nimihistoria[i];
            }
        }
        return nimi;
    };

    // Haetaan nimihistorian sisältämä nykyinen nimi (ei siis tuleva ajastettu nimi)
    currentNimi = function() {
        var nimi = nimihistoria[0];
        for(var i=0; i < nimihistoria.length; i++) {
            $log.debug(nimihistoria[i]);
            if (moment(nimihistoria[i].alkuPvm).isAfter(moment(nimi.alkuPvm)) &&
                    moment(nimihistoria[i].alkuPvm).isBefore(moment())) {
                nimi = nimihistoria[i];
            }
        }
        return nimi;
    };

    $scope.uusinNimi         = uusinNimi();
    $scope.voimassaolevaNimi = currentNimi();

    // Uudella nimellä on minimialkupäivämäärä.
    // Viimeisimmän voimassaolevan nimen alkupäivämäärä tai organisaation alkupäiviämäärä.
    if(moment($scope.voimassaolevaNimi.alkuPvm).isValid()) {
        $scope.minAlkupvm = $scope.voimassaolevaNimi.alkuPvm;
    }
    else {
        $scope.minAlkupvm = organisaatioAlkuPvm;
    }

    $log.debug('Minimi alkupäivämäärä: ' + $scope.minAlkupvm);

    $scope.nimi_fi = $scope.uusinNimi.nimi.fi;
    $scope.nimi_sv = $scope.uusinNimi.nimi.sv;
    $scope.nimi_en = $scope.uusinNimi.nimi.en;
    $scope.alkupvm = $scope.uusinNimi.alkuPvm;
    $scope.mode = "update";
}
