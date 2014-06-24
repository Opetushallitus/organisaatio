(function(window, angular) {

    var app = angular.module('organisaatio');

    app.directive('idle', ['$idle', '$timeout', '$interval', function($idle, $timeout, $interval){
        return {
            restrict: 'A',
            link: function(scope, elem, attrs) {
                var timeout;
                var timestamp = localStorage.lastEventTime;

                // Watch for the events set in ng-idle's options
                // If any of them fire (considering 500ms debounce), update localStorage.lastEventTime with a current timestamp
                elem.on($idle._options().events, function(){
                    if($idle.running()) {
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
                        $idle.watch();
                        timestamp = localStorage.lastEventTime;
                    }
                }, 5000, false);
            }
        };
    }]);

    app.controller('SessionExpiresCtrl', ['$idle', '$scope', '$modalInstance', '$window', 'LocalisationService', function( $idle, $scope, $modalInstance, $window, LocalisationService) {
        $scope.timeoutMessage = function() {
            var duration = Math.floor(MAX_SESSION_IDLE_TIME_IN_SECONDS / 60);
            return LocalisationService.t('session.expired.text1.part1') + " " + duration +  " " + LocalisationService.t('session.expired.text1.part2');
        };

        $scope.okConfirm = function() {
            $idle.watch();
            $modalInstance.close();
        };
        $scope.redirectToLogin = function() {
            $window.location.reload();
        };
    }]);

    app.controller('EventsCtrl', ['$scope','$idle', '$modal', '$http', function($scope, $idle, $modal, $http) {
        var openModal = function(template) {
            return $modal.open({
                    templateUrl: TEMPLATE_URL_BASE + template,
                    controller: 'SessionExpiresCtrl',
                    keyboard: false,
                    backdrop: 'static',
                    windowClass: 'modal-warning'
                });
        };

        $scope.$on('$idleWarn', function(e, countdown) {
            if (!$scope.sessionWarning || angular.element('#sessionWarning').length < 1) {
                $scope.sessionWarning = openModal('sessionWarning.html');
            }
        });

        $scope.$on('$idleTimeout', function() {
            $scope.sessionWarning.close();
            $scope.sessionWarning = openModal('sessionExpired.html');
            $idle.unwatch();
        });

    }])
    .config(['$idleProvider', '$keepaliveProvider', function($idleProvider, $keepaliveProvider) {
        var warningDuration = 300;
        $idleProvider.idleDuration(MAX_SESSION_IDLE_TIME_IN_SECONDS - warningDuration);
        $idleProvider.warningDuration(warningDuration);
        $keepaliveProvider.interval(SESSION_KEEPALIVE_INTERVAL_IN_SECONDS);
        $keepaliveProvider.http(SERVICE_URL_BASE + "session/maxinactiveinterval");
    }])
    .run(['$idle', function($idle){
        $idle.watch();
    }]);
    
})(window, window.angular)
