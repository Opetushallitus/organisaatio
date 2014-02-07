function OrganisaatioDeleteController($scope, $modalInstance, $log, nimi, tyyppi) {
    $scope.nimi = nimi;
    $scope.tyyppi = tyyppi;
    
    if (nimi && tyyppi) {
        $log.debug("Organisaation poisto (vaatii vahvistuksen): " + tyyppi + " " + nimi);
    }
}