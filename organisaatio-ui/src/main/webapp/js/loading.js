angular.module('Loading', ['Localisation'])

.factory('LoadingService', function($log) {

    $log = $log.getInstance("LoadingService");

    var service = {
        requestCount: 0,
        operationCount: 0,
        errors: 0,
        modal:false,
        timeout: null,
        timeoutMinor: false,
        timeoutMajor: false,
        scope: null,
        errorHandlingRequested: null,
        isEnabled: true,

        timeoutShort: 1000, //window.CONFIG.env["ui.timeout.short"],
        timeoutLong: 60000, //window.CONFIG.env["ui.timeout.long"],

        isLoading: function() {
            return (service.requestCount > 0 || service.operationCount > 0) && service.isEnabled;
        },
        isModal: function() {
            return service.requestCount > 0;
        },
        isError: function() {
            return service.errors!==0;
        },
        beforeOperation: function() {
            //$log.log("LOADING beforeOperation", service);
            service.operationCount++;
        },
        afterOperation: function() {
            //$log.log("LOADING afterOperation", service);
            service.operationCount--;
        },
        beforeRequest: function() {
            //$log.log("LOADING beforeRequest", service);
            service.modal = true;
            service.startTimeout();
            service.requestCount++;
        },
        afterRequest: function(success, req) {
            //$log.log("LOADING afterRequest "+success, service);
            if (success) {
                service.requestCount--;
            }
            else {
                $log.warn("Error afterRequest ", req);
                service.errors++;
            }
            service.clearTimeout();
            service.errorHandlingRequested = null;
        },
        commit: function() {
            //$log.log("LOADING commit", service);
            service.requestCount -= service.errors;
            service.errors = 0;
            service.clearTimeout();
        },
        clearTimeout: function() {
            //$log.log("LOADING clearTimeout", service);
            if (service.requestCount===0 && service.timeout!==null) {
                window.clearTimeout(service.timeout);
                service.timeout = null;
                service.timeoutMinor = false;
                service.timeoutMajor = false;
            }
        },
        startTimeout: function() {
            //$log.log("LOADING startTimeout", service);
            if (service.timeout!==null) {
    		return;
            }
            service.timeout = window.setTimeout(function(){
    		service.timeoutMinor = true;
    		service.scope.$apply();

    		service.timeout = window.setTimeout(function(){
                    service.timeoutMajor = true;
                    service.scope.$apply();
        	}, service.timeoutShort);

            }, service.timeoutLong);
        },

        /**
         * Kutsutaan error-callbackissa; estää teknisen virheen dialogin näytön.
         */
        onErrorHandled: function(response) {
            if (service.errorHandlingRequested) {
    		    service.errorHandlingRequested=false;
                service.afterRequest(!service.errorHandlingRequested, response);

            } else if (service.errorHandlingRequested===null) {
    		    throw "LoadingService.onErrorHandled called from outside of error callback";
            }
        },

        /**
         * Disabloi spinneri
         */
        setSpinnerEnabled: function(enabled) {
            service.isEnabled = enabled;
        }
    };

  return service;
})

.factory('onStartInterceptor', function(LoadingService) {
    return function (data, headersGetter, status) {
        if (!headersGetter(ORGANISAATIO_NO_LOADING_HEADER)) {
            LoadingService.beforeRequest();
        }
        return data;
    };
})

// Intercept http responses.
.factory('onCompleteInterceptor', function(LoadingService, $q, $log) {
    $log = $log.getInstance('onCompleteInterceptor');
    return {
        // Just call afterRequest() to clear timeout and pass the response to the orginal caller.
        // "decrementRequestCountSuccess"
        response: function(response) {
            if (!response.config || !response.config.headers || !response.config.headers[ORGANISAATIO_NO_LOADING_HEADER]) {
                LoadingService.afterRequest(true, response);
            }
            return response;
        },
        // "decrementRequestCountError"
        responseError: function(response) {
            var ret = $q.reject(response);
            LoadingService.errorHandlingRequested = true;
            return ret;
        }
    };
})

.config(function($httpProvider) {
    $httpProvider.interceptors.push('onCompleteInterceptor');
})

.run(function($http, onStartInterceptor) {
    $http.defaults.transformRequest.push(onStartInterceptor);
})

.controller('LoadingCtrl', function($scope, $rootElement, $uibModal, LoadingService) {

    var ctrl = $scope;
    LoadingService.scope = ctrl;

    $scope.restart = function() {
    	location.hash = "";
    	location.reload();
    };

    function showErrorDialog() {
        $uibModal.open({
            controller: function($scope, $uibModalInstance) {
                $scope.commit = function() {
                    LoadingService.commit();
                    $uibModalInstance.dismiss();
                };
                $scope.restart = function() {
                    ctrl.restart();
                };
            },
            templateUrl: "loading-error-dialog.html"
            //scope: ns
        });
    }

    $scope.$watch(function() {
        return LoadingService.isLoading();
    }, function(value) {
        $scope.loading = value;
        if(value) {
          $rootElement.addClass('spinner');
        } else {
          $rootElement.removeClass('spinner');
        }
    });

    $scope.$watch(function() {
        return LoadingService.errors;
    }, function(value, oldv) {
        if(value>0 && oldv===0) {
            showErrorDialog();
        }
    });

    $scope.isModal = function() {
    	return LoadingService.isModal();
    };

    $scope.isError = function() {
    	return LoadingService.isError();
    };

    $scope.isTimeout = function(major) {
    	return major ? LoadingService.timeoutMajor : LoadingService.timeoutMinor;
    };

});