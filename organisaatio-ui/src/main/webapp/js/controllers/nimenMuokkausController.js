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

function NimenMuokkausController($scope, $modalInstance, $log,
                                 NimenMuokkausModel,
                                 oid, nimihistoria, organisaatioAlkuPvm,
                                 koulutustoimija, oppilaitos, parentNimi,
                                 nameFormat, parentPattern) {

    $log = $log.getInstance("NimenMuokkausController");

    $scope.model = NimenMuokkausModel;
    $scope.model.refresh(oid, nimihistoria, organisaatioAlkuPvm,
                         koulutustoimija, oppilaitos, parentNimi,
                         nameFormat);

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
        $scope.model.clear();
    };

    $scope.accept = function() {
        $modalInstance.close($scope.model);
        $scope.model.accept();
    };

    $scope.newNimiMode = function(form) {
        $log.debug('newNimiMode()');
        $scope.model.clearVisibleNimi();
    };

    $scope.updateNimiMode = function(form) {
        $log.debug('updateNimiMode()');
        $scope.model.setUusinNimiVisible(koulutustoimija, oppilaitos, parentNimi);
        if ($scope.model.isUusinNimiChanged() === false) {
            form.$setPristine();
        }
    };

    $scope.cancelNimenMuutosMode = function(form) {
        $log.debug('cancelNimenMuutosMode()');
        $scope.model.setUusinNimiVisible();
    };


}
