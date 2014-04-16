app.directive('formatteddate', function($log, $filter) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, element, attrs, ctrl) {

            // Tämä muuttaa päivämäärän oikeaksi jos syötetty esim. 31.2.1999 --> 03.03.1999
            element.bind('blur', function() {
                var val = element.val();

                if (!val || ctrl.$valid === false) {
                    return val;
                }

                var dateParts = val.split('.');
                parsed = new Date(dateParts[2], dateParts[1] - 1, dateParts[0]);

                var newVal = $filter('date')(parsed, 'dd.MM.yyyy');

                element.val(newVal);
            });

            // Tämä validoi päivämäärän
            function validateDate(viewValue) {
                var val = element.val();
                var pattern = /^(0?[1-9]|[12][0-9]|3[01])\.(0?[1-9]|1[012])\.(19[7-9]\d)|([2-9]\d{3})$/i;

                if (val && val.match(pattern) === null) {
                    ctrl.$setValidity('date', false);
                    return viewValue;
                }

                if (!viewValue) {
                    ctrl.$setValidity('date', true);
                    return null;
                } else if (angular.isDate(viewValue) && !isNaN(viewValue)) {
                    ctrl.$setValidity('date', true);
                    return viewValue;
                } else if (angular.isString(viewValue)) {
                    var date = new Date(viewValue);
                    if (isNaN(date)) {
                        ctrl.$setValidity('date', false);
                        return undefined;
                    } else {
                        ctrl.$setValidity('date', true);
                        return date;
                    }
                } else {
                    ctrl.$setValidity('date', false);
                    return undefined;
                }
            }
            ctrl.$parsers.unshift(validateDate);

            // Tämä hoitaa sen, että DatePicker saa päivämäärän oikeassa muodossa
            ctrl.$parsers.unshift(function(viewValue) {
                var val = element.val();

                if (!val)
                    return viewValue;
                var dateStr = $filter('date')(val, 'dd.MM.yyyy');

                if (dateStr === undefined) {
                    return viewValue;
                }
                var parsed = viewValue;
                try
                {
                    var dateParts = dateStr.split('.');
                    parsed = new Date(dateParts[2], dateParts[1] - 1, dateParts[0]);
                }
                catch (e) {
                    $log.log("catch --> invalid");
                }
                return parsed;
            });
        }
    };
});

app.directive('testField', function($log) {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            ctrl.$parsers.unshift(function(viewValue) {
                $log.log("ret " + viewValue);
                return viewValue;
            });
        }
    };
});

// Tätä voidaan käyttää ng-patternin sijaan, jos halutaan että epävalidi modelin arvo näytetään.
// Angular ei näytä epävalidia arvoa vaan tyhjän kentän (https://github.com/angular/angular.js/issues/1412).
app.directive('ophPattern', function($log) {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            var validator = function(viewValue) {
                var isValid = (viewValue===null || typeof viewValue=== 'undefined') || (typeof viewValue === 'string' && viewValue.match(attrs.ophPattern));
                ctrl.$setValidity('ophPattern', isValid);
                return viewValue;
            };
            ctrl.$parsers.unshift(validator);
            ctrl.$formatters.unshift(validator);
        }
    };
});

app.directive('ophNullIfZeroLength', function($log) {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            var parserValidator = function(viewValue) {
                if (viewValue === null) {
                    return viewValue;
                }
                if (typeof viewValue === 'undefined') {
                    return null;
                }
                return ((viewValue.length === 0) ? null : viewValue);
            };
            ctrl.$parsers.unshift(parserValidator);
            var formatterValidator = function(viewValue) {
                if (elm[0] && (elm[0].value !== null) && (elm[0].value.length === 0)) {
                    elm.value = null;
                }
                return viewValue;
            };
            ctrl.$formatters.unshift(formatterValidator);
        }
    };
});

app.directive('namesCombinedField', function() {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            var parserValidator = function(viewValue) {
                scope.form.nimifi.$setValidity('namescombinedrequired', true);

                if (!viewValue && !scope.form.nimifi.$viewValue &&
                        !scope.form.nimisv.$viewValue && !scope.form.nimien.$viewValue) {
                    scope.form.nimifi.$setValidity('namescombinedrequired', false);
                }
                return viewValue;
            };
            ctrl.$parsers.unshift(parserValidator);

            var formatterValidator = function(viewValue) {
                scope.form.nimifi.$setValidity('namescombinedrequired', true);

                if (!viewValue && !scope.form.nimifi.$viewValue &&
                        !scope.form.nimisv.$viewValue && !scope.form.nimien.$viewValue) {
                    scope.form.nimifi.$setValidity('namescombinedrequired', false);
                }
                return viewValue;
            };
            ctrl.$formatters.unshift(formatterValidator);
        }
    };
});

app.directive('addressCombinedField', function() {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            var parserValidator = function(viewValue) {
                var returnUndefined = false;
                scope.form.kayntiosoitefi.$setValidity('addresscombinedrequired', true);
                if (scope.optional) {
                    return viewValue;
                }
                if (!(scope.form.kayntiosoitefi.$viewValue && scope.form.postiosoitefi.$viewValue)
                        && !(scope.form.kayntiosoitesv.$viewValue && scope.form.postiosoitesv.$viewValue)
                        && !scope.form.kayntiosoitekv.$viewValue && scope.form.postiosoitekv.$viewValue) {
                    scope.form.kayntiosoitefi.$setValidity('addresscombinedrequired', false);
                    returnUndefined = true;
                }
                if (returnUndefined === true) {
                    return viewValue;
                } else {
                    return viewValue;
                }
            };
            ctrl.$parsers.unshift(parserValidator);

            var formatterValidator = function(viewValue) {
                if (scope.mode === "new") {
                    scope.form.kayntiosoitefi.$setValidity('addresscombinedrequired', false);
                }
                return viewValue;
            };
            ctrl.$formatters.unshift(formatterValidator);
        }
    };
});

// Validoi että syötetty postinumero löytyy koodistosta
app.directive('ophValidatePostcode', function($log) {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            var validator = function(viewValue) {
                // Kieli on pakko lukea suoraan modelista, attribuutin arvona ei näytä päivittyvän oikein
                var lang = (attrs.ophValidatePostcode==="yt" ? scope.model.ytlang : scope.model.hplang);
                if (!viewValue) {
                    ctrl.$setValidity('ophpostcode', true);
                    ctrl.$setValidity('ophpostcode_' + lang, true);
                    return viewValue;
                }
                var isValid = (typeof viewValue === 'string' && scope.model.koodisto.postinumerot.indexOf(viewValue) !== -1);
                ctrl.$setValidity('ophpostcode', isValid);
                ctrl.$setValidity('ophpostcode_' + lang, isValid);
                return viewValue;
            };
            ctrl.$parsers.unshift(validator);
            ctrl.$formatters.unshift(validator);
        }
    };
});

app.directive("dynamicName",function($compile, $log){
  return {
      restrict:"A",
      terminal:true,
      priority:1000,
      link:function(scope,element,attrs){
          element.attr('name', attrs.dynamicNamePrefix + "_" + scope.$eval(attrs.dynamicName));
          element.removeAttr("dynamic-name");
          element.removeAttr("dynamic-name-prefix");
          $compile(element)(scope);
      }
   };
});

// Asettaa lomakkeen dirty-flagin päälle jos oph-set-dirty -attribuutti on true
app.directive('ophSetDirty', function($log) {
    return {
        require: [ 'ngModel' ,'^form'],
        restrict: "A",
        link: function(scope, elm, attrs, ctrls) {
            ctrls[0].$formatters.unshift(function(viewValue) {
                if (typeof viewValue === 'string' && viewValue.length>0 && scope.$eval(attrs.ophSetDirty) === true) {
                    ctrls[1].$setDirty();
                }
                return viewValue;
            });
        }
    };
});

app.directive('ophFileupload', function($log, $http) {
    $log.info('init file upload');
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var tempFileUrl = SERVICE_URL_BASE + 'tempfile/';
            element.attr('data-url', tempFileUrl);
            $(function() {
                $(element).fileupload({
                    dataType: 'json',
                    add: function(e, data) {
                        $log.info('added file');
                        data.submit();
                    },
                    done: function(e, data) {
                        $http.get(tempFileUrl + data.result.name).success(function(res) {
                            scope.model.organisaatio.metadata.kuvaEncoded = res;
                            $http.delete(tempFileUrl + data.result.name).success(function() {
                                $log.debug('deleted temp file from server');
                            }).error(function(err) {
                                $log.debug('failed to delete temp file from server: ' + err);
                            });
                        });
                    }
                });
            });
        }
    };
});