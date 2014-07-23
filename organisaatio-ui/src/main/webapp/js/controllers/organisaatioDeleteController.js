function OrganisaatioDeleteController($scope, $modalInstance, $log, nimi, tyypit) {
    $scope.nimi = nimi;
    $scope.tyypit = tyypit;

    if (nimi && tyypit) {
        $log.debug("Organisaation poisto (vaatii vahvistuksen): " + tyypit + " " + nimi);
    }
}