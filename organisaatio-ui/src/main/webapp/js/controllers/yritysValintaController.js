function YritysValintaController($scope, $modalInstance, $location, $log, YritysValintaModel, ytunnus) {
    $log = $log.getInstance("YritysValintaController");

    $scope.model = YritysValintaModel;

    if (ytunnus) {
        $log.debug("Yritysvalinta ytunnuksella: " + ytunnus);
        $scope.model.hakuString = ytunnus;
    }
    $scope.model.yritykset.lenght = 0;
    $scope.model.refresh();

    $scope.select = function (yritys) {
        $log.info(yritys);
        $modalInstance.close(yritys.ytunnus);
        $scope.model.hakuString = "";
    };

    $scope.search = function() {
        $scope.model.refresh();
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
        $scope.model.hakuString = "";
    };

    $scope.continueWithouSelect = function () {
        $modalInstance.close();
        $scope.model.hakuString = "";
    };
}