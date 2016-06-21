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

app.controller('YtjIlmoituksetController', ['$scope', '$log', '$location', '$filter', 'YtjLoki',
    function YtjIlmoituksetController($scope, $log, $location, $filter, YtjLoki) {
        $log = $log.getInstance("YtjIlmoituksetController");
        $scope.logs = [];
        $scope.openIndex = [true];

        $scope.$on('$routeChangeSuccess', function() {
            YtjLoki.get({alkupvm: (new Date()).setDate((new Date).getDate() - 14), loppupvm: (new Date()).getTime()}, function (result) {
                $log.info("resuls", result);
                $scope.logs.length = 0;
                angular.forEach(result, function (loki) {
                    var oidList = [];
                    var virheList = loki.ytjVirheet;
                    loki.ytjVirheet = [];
                    // Group by oid and name
                    angular.forEach(virheList, function(virhe) {
                        var ytjVirhe = {};
                        if(oidList.indexOf(virhe.oid) === -1) {
                            ytjVirhe.oid = virhe.oid;
                            ytjVirhe.orgNimi = virhe.orgNimi;
                            ytjVirhe.virheet = [virhe];
                            loki.ytjVirheet.push(ytjVirhe);

                            oidList.push(virhe.oid);
                        }
                        else {
                            var ytjVirheList = loki.ytjVirheet.filter(function (obj) {
                                return obj.oid === virhe.oid;
                            });
                            ytjVirheList[0].virheet.push(virhe);
                        }
                    });
                    $scope.logs.push(loki);
                });
            }, function () {
                $log.error("Could not fetch ytj log information.");
            })
        });
        
        // Siirtyminen organisaatioiden pääsivulle organisaatiopuu näkymään
        $scope.cancel = function() {
            $location.path("/");
        };

        $scope.closeOthers = function(index) {
            if($scope.openIndex.indexOf(true) !== -1) {
                angular.forEach($scope.openIndex, function (value, key) {
                    $scope.openIndex[key] = false;
                });
                $scope.openIndex[index] = true;
            }
        }
    }
]);
