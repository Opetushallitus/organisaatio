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

(function(window, angular) {

    var app = angular.module('organisaatio');

    app.directive('idle', ['Idle', '$timeout', '$interval', function(Idle, $timeout, $interval){
        return {
            restrict: 'A',
            link: function(scope, elem, attrs) {
                var timeout;
                var timestamp = localStorage.lastEventTime;

                // Watch for the events set in ng-idle's options
                // If any of them fire (considering 500ms debounce), update localStorage.lastEventTime with a current timestamp
                elem.on(Idle._options().interrupt, function(){
                    if(Idle.running()) {
                        if (timeout) { $timeout.cancel(timeout); }
                        timeout = $timeout(function(){
                            localStorage.setItem('lastEventTime', new Date().getTime());
                        }, 3000, false);
                    }
                });

                // Every 5s, poll localStorage.lastEventTime to see if its value is greater than the timestamp set for the last known event
                // If it is, reset the ng-idle timer and update the last known event timestamp to the value found in localStorage
                window.setInterval(function() {
                    if (localStorage.lastEventTime > timestamp) {
                        var element = angular.element('#sessionWarning .btn');
                        if (element.length > 0) {
                            $timeout(function() {
                                element.click();
                            }, 500, false);
                        }
                        Idle.watch();
                        timestamp = localStorage.lastEventTime;
                    }
                }, 5000, false);
            }
        };
    }]);

    app.controller('SessionExpiresCtrl', ['Idle', '$scope', '$uibModalInstance', '$window', 'LocalisationService',
        function( Idle, $scope, $uibModalInstance, $window, LocalisationService) {
        $scope.timeoutMessage = function() {
            var duration = Math.floor(MAX_SESSION_IDLE_TIME_IN_SECONDS / 60);
            return LocalisationService.t('session.expired.text1.part1') + " " + duration +  " " + LocalisationService.t('session.expired.text1.part2');
        };

        // This is loaded before localisationCtrl so this needs to be done manually here.
        $scope.t = function(key) {
            return LocalisationService.t(key);
        };

        $scope.okConfirm = function() {
            Idle.watch();
            $uibModalInstance.close();
        };
        $scope.redirectToLogin = function() {
            $window.location.reload();
        };
    }]);

    app.controller('EventsCtrl', ['$scope','Idle', '$uibModal', '$http',
        function($scope, Idle, $uibModal, $http) {
        var openModal = function(template) {
            return $uibModal.open({
                    templateUrl: TEMPLATE_URL_BASE + template,
                    controller: 'SessionExpiresCtrl',
                    keyboard: false,
                    backdrop: 'static',
                    windowClass: 'modal-warning',
                    scope: $scope
                });
        };

        $scope.$on('IdleWarn', function(e, countdown) {
            if (!$scope.sessionWarning || angular.element('#sessionWarning').length < 1) {
                $scope.sessionWarning = openModal('sessionWarning.html');
            }
        });

        $scope.$on('IdleTimeout', function() {
            $scope.sessionWarning.close();
            $scope.sessionWarning = openModal('sessionExpired.html');
            Idle.unwatch();
        });

    }])
    .config(['IdleProvider', 'KeepaliveProvider', function(IdleProvider, KeepaliveProvider) {
        var warningDuration = 300;
        IdleProvider.idle(MAX_SESSION_IDLE_TIME_IN_SECONDS - warningDuration);
        IdleProvider.timeout(warningDuration);
        KeepaliveProvider.interval(SESSION_KEEPALIVE_INTERVAL_IN_SECONDS);

        var ORGANISAATIO_REST_ORGAISAATIO_MAXINACTIVEINTERVAL = ORGANISAATIO_REST_ORGAISAATIO_MAXINACTIVEINTERVAL || SERVICE_URL_BASE + "session/maxinactiveinterval";
        KeepaliveProvider.http(ORGANISAATIO_REST_ORGAISAATIO_MAXINACTIVEINTERVAL);
    }])
    .run(['Idle', function(Idle){
        Idle.watch();
    }]);

})(window, window.angular);
