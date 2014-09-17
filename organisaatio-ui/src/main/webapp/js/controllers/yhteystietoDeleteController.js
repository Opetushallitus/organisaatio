var YhteystietoDeleteController = function ($scope, $modalInstance, $log, nimi) {
    $scope.nimi = nimi;

    if (nimi) {
        $log.debug("Yhteystiedon poisto (vaatii vahvistuksen): " + nimi);
    }
};