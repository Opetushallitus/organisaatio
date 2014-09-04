function VoimassaolonMuokkausController($scope, $modalInstance, $log, VoimassaolonMuokkausModel, muokataanAlkupvm, oid, nimi, alkuPvm, lakkautusPvm, aliorganisaatioHaunTulos, monikielinenTekstiLocalizer) {

    $scope.model = VoimassaolonMuokkausModel;
    
    $scope.model.configure(muokataanAlkupvm, oid, nimi, alkuPvm, lakkautusPvm, aliorganisaatioHaunTulos, monikielinenTekstiLocalizer);
    
    $scope.cancel = function() {
        $scope.model.cancel();
        $modalInstance.dismiss('cancel');
    };

    $scope.accept = function() {
        $scope.model.accept();
        $modalInstance.close($scope.model);
    };
}
