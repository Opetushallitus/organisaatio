/*
 Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus

 This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 soon as they will be approved by the European Commission - subsequent versions
 of the EUPL (the "Licence");

 You may not use this work except in compliance with the Licence.
 You may obtain a copy of the Licence at: http://www.osor.eu/eupl/

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 European Union Public Licence for more details.
 */

app.controller('YritysValintaController', function YritysValintaController($scope, $modalInstance, $location, $log,
                                 YritysValintaModel, ytunnus) {

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
});