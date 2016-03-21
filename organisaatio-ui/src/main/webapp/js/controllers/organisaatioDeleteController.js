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

app.controller('OrganisaatioDeleteController', function OrganisaatioDeleteController($scope, $uibModalInstance, $log, nimi, tyypit) {

    $log = $log.getInstance("OrganisaatioDeleteController");

    $scope.nimi = nimi;
    $scope.tyypit = tyypit;

    if (nimi && tyypit) {
        $log.debug("Organisaation poisto (vaatii vahvistuksen): " + tyypit + " " + nimi);
    }
});