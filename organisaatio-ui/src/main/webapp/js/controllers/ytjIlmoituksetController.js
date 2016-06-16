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

app.controller('YtjIlmoituksetController', ['$scope', '$log', '$location', 'YtjLoki', 'LocalisationService',
    function YtjIlmoituksetController($scope, $log, $location, YtjLoki, LocalisationService) {
        $log = $log.getInstance("YtjIlmoituksetController");
        $scope.logs = [];
        $scope.openIndex = [true];

        $scope.$on('$routeChangeSuccess', function() {
            YtjLoki.get({}, function (result) {
                $log.info("resuls", result);
                $scope.logs.length = 0;
                angular.forEach(result, function (loki) {
                    var obj = {};
                    var virheet = loki.ytjVirheet;
                    loki.ytjVirheet = [];
                    // Group by oid and name
                    angular.forEach(virheet, function(virhe) {
                        if(angular.isUndefined(obj[virhe.oid])) {
                            obj.oid = virhe[0].oid;
                            obj.orgNimi = virhe[0].orgNimi;
                            obj.virheet = virhe;
                        }
                        else {
                            obj.virheet.push(virhe);
                        }
                        loki.ytjVirheet.push(obj);
                    });
                    $scope.logs.push(loki);
                });
            }, function () {
                $log.error("Could not fetch ytj log information.");
            })
        });
        
        $scope.getVirheHeader = function(log) {
            if(log.paivitysTila === 'ONNISTUNUT') {
                return LocalisationService.t('ilmoitukset.log.' + log.paivitysTila);
            }
            else if(log.paivitysTila === 'ONNISTUNUT_VIRHEITA') {
                return LocalisationService.t('ilmoitukset.log.' + log.paivitysTila, [log.ytjVirheet.length]);
            }
            else if(log.paivitysTila === 'EPAONNISTUNUT') {
                return LocalisationService.t('ilmoitukset.log.' + log.paivitysTila, [log.ytjVirheet[0].virheet[0].virheViesti]);
            }
            else {
                $log.error('Invalid paivitysTila');
            }
        };

        // Siirtyminen organisaatioiden pääsivulle organisaatiopuu näkymään
        $scope.cancel = function() {
            $location.path("/");
        };

        $scope.closeOthers = function(index) {
            angular.forEach($scope.openIndex, function (value, key) {
                $scope.openIndex[key] = false;
            });
            $scope.openIndex[index] = true;
        }
    }
]);
