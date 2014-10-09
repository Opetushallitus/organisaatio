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

var mod = angular.module('loading', []);

mod.factory('loadingService', function() {
  var service = {
    requestCount: 0,
    isLoading: function() {
      return service.requestCount > 0;
    }
  };
  return service;
});

mod.factory('onStartInterceptor', function(loadingService) {
    return function (data, headersGetter) {
        loadingService.requestCount++;
        return data;
    };
});

mod.factory('onCompleteInterceptor', function(loadingService, $q) {
  return function(promise) {
    var decrementRequestCountSuccess = function(response) {
        loadingService.requestCount--;
        return response;
    };
    var decrementRequestCountError = function(response) {
        loadingService.requestCount--;
        return $q.reject(response);
    };
    return promise.then(decrementRequestCountSuccess, decrementRequestCountError);
  };
});

mod.config(function($httpProvider) {
    $httpProvider.responseInterceptors.push('onCompleteInterceptor');
});

mod.run(function($http, onStartInterceptor) {
    $http.defaults.transformRequest.push(onStartInterceptor);
});

mod.controller('LoadingCtrl', function($scope, loadingService) {
    $scope.$watch(function() {
        return loadingService.isLoading();
    }, function(value) {
        $scope.loading = value;
    });
});