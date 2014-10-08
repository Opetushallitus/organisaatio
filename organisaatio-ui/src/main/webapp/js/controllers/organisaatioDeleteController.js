function OrganisaatioDeleteController($scope, $modalInstance, $log, nimi, tyypit) {
    $log = $log.getInstance("OrganisaatioDeleteController");

    $scope.nimi = nimi;
    $scope.tyypit = tyypit;

    if (nimi && tyypit) {
        $log.debug("Organisaation poisto (vaatii vahvistuksen): " + tyypit + " " + nimi);
    }
}