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

function OrganisaatioMoveController($scope, $modalInstance, $log, Organisaatio, Organisaatiot, nimi, node) {

    $scope.nimi = nimi;

    $scope.suggests = [];
    //['1.2.246.562.10.346830761110','1.2.246.562.10.97053458523','1.2.246.562.10.48442622063', '1.2.246.562.10.53542906168'];


    var parametrit = {"searchstr": "", "aktiiviset": true, "suunnitellut": true, "lakkautetut": false}

    Organisaatiot.get(parametrit, function (result) {
        var values = result.organisaatiot.map(function (org) {
            return {
                "name": org.nimi.fi,
                "oid": org.oid
            };
        });
        $scope.suggests = $scope.suggests.concat(values);
    });

    $scope.options = {
        newParentOrganization: null,        //possible values move, remove
        //suborganization: "move",
        //possible values move, remove
        educationAndStudyTarget: "move",
        //date
        date: new Date()
    };

    $scope.select = function (org) {
        $scope.options.newParentOrganization = org.oid;
    };

    Organisaatio.get({oid: node.oid}, function (result) {
            $scope.options.organisaatio = result;
        }
    );


}