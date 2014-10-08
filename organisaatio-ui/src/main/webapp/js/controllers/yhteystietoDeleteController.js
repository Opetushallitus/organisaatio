var YhteystietoDeleteController = function ($scope, $modalInstance, $log, nimi) {
    $log = $log.getInstance("YhteystietoDeleteController");

    $scope.nimi = nimi;

    if (nimi) {
        $log.debug("Yhteystiedon poisto (vaatii vahvistuksen): " + nimi);
    }
};