app.directive('formatteddate', function($log) {
    $log = $log.getInstance("formatteddate directive");

    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, element, attrs, ctrl) {
            var maxDate = moment(attrs.max, 'YYYY-MM-DD');
            var minDate = moment(attrs.min, 'YYYY-MM-DD');

            function isRangeValid(date) {
                return (date > minDate && date < maxDate);
            }

            // Tämä validoi päivämäärän
            ctrl.$validators.date = function validateDate(modelValue, viewValue) {
                $log.log("Validation starts");
                var date;

                if (angular.isUndefined(viewValue)) {
                    $log.log("ViewValue undefined.");
                    viewValue = null;
                    return true;
                }
                if (!viewValue) {
                    $log.log("ViewValue empty");
                    return true;
                }
                // Viewvalue should always be string.
                if (angular.isString(viewValue)) {
                    $log.log("String ViewValue= " + viewValue);

                    if (!moment(viewValue,'DD.MM.YYYY').isValid()) {
                        $log.log("Invalid string viewValue= " + viewValue);
                        return false;
                    }
                    date = moment(viewValue, 'DD.MM.YYYY');
                    if (!date.isValid()) {
                        $log.log("String ViewValue invalid");
                        return false;
                    }
                    return true;
                }
                if (typeof viewValue === "object" && moment(viewValue).isValid()) {
                    $log.log("Valid object ViewValue= " + viewValue);
                    return true;
                }
                log.warn("Unknown viewvalue type or invalid value.");
                return false;
            };

            ctrl.$validators.dateYear = function(modelValue, viewValue) {
                $log.log("Format starts");
                if (!viewValue) {
                    $log.log("ViewValue empty");
                    return true;
                }
                // Viewvalue should always be string
                if (angular.isString(viewValue)) {
                    $log.log("String ViewValue= " + viewValue);
                    date = moment(viewValue, 'DD.MM.YYYY');
                    return isRangeValid(date.toDate());
                }
                if (typeof viewValue === "object" && moment(viewValue).isValid()) {
                    $log.log("Valid object ViewValue= " + viewValue);
                    date = moment(viewValue); // viewValue.type == Date object
                    return isRangeValid(date.toDate());
                }
                log.warn("Unknown viewvalue type or invalid value.");
                return false;
            };
        }
    };
});

app.directive('datetext', function($filter) {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            ctrl.$formatters.push(function(data) {
                return $filter('date')(data, "dd.MM.yyyy");
            });
        }
    };
});

app.directive('noedit', function () {
    return {
        link: function (scope, elm, attrs) {
          elm.bind('keypress', function(e){
              e.preventDefault();
              return false;
          });
        }
    }
});


app.directive('testField', function($log) {
    $log = $log.getInstance("testField directive");

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
                var isValid = (viewValue === null || typeof viewValue === 'undefined') || (typeof viewValue === 'string' && viewValue.match(attrs.ophPattern));
                ctrl.$setValidity('ophPattern', isValid);
                return viewValue;
            };
            ctrl.$parsers.unshift(validator);
            ctrl.$formatters.unshift(validator);
        }
    };
});

// Kuten ophPattern, mutta asettaa joko $error-flagin (jos oph-name-format attribuutti on true) tai
// ophPatternWarning-flagin (jos oph-name-format attribuutti on false)
app.directive('ophNamePattern', function($log) {
    return {
        require: 'ngModel',
        scope: {
            text: "@ophNameFormat"
        },
        link: function(scope, elm, attrs, ctrl) {
            var validator = function(viewValue) {
                var isValid = (viewValue === null || typeof viewValue === 'undefined') ||
                        (typeof viewValue === 'string' && viewValue.match(attrs.ophNamePattern));
                if (scope.text) {
                    ctrl.$setValidity('ophNamePattern', isValid);
                } else {
                    ctrl.ophPatternWarning = !isValid;
                }
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
                if(angular.isDefined(elm[0]) && angular.isUndefined(elm[0].value)) {
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
                var valid = viewValue ||
                        (ctrl.$name !== 'nimifi' && scope.form.nimifi.$viewValue) ||
                        (ctrl.$name !== 'nimisv' && scope.form.nimisv.$viewValue) ||
                        (ctrl.$name !== 'nimien' && scope.form.nimien.$viewValue);
                scope.form.nimifi.$setValidity('namescombinedrequired', valid);
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
                scope.form.kayntiosoitefi.$setValidity('addresscombinedrequired', true);
                if (scope.optional) {
                    return viewValue;
                }
                if (!(scope.form.kayntiosoitefi.$viewValue && scope.form.postiosoitefi.$viewValue) &&
                        !(scope.form.kayntiosoitesv.$viewValue && scope.form.postiosoitesv.$viewValue) &&
                        !scope.form.kayntiosoitekv.$viewValue && scope.form.postiosoitekv.$viewValue) {
                    scope.form.kayntiosoitefi.$setValidity('addresscombinedrequired', false);
                }
                return viewValue;
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

app.directive('uniqueYhteystietojenTyyppiField', function($log) {
    $log = $log.getInstance("uniqueYhteystietojenTyyppiField directive");

    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            var parserValidator = function(viewValue) {
                $log.log("parserValidator");
                ctrl.$setValidity('unique', scope.yttNimiUnique(viewValue));
                return viewValue;
            };
            ctrl.$parsers.unshift(parserValidator);

            var formatterValidator = function(viewValue) {
                $log.log("formatterValidator");
                ctrl.$setValidity('unique', scope.yttNimiUnique(viewValue));
                return viewValue;
            };
            ctrl.$formatters.unshift(formatterValidator);
        }
    };
});

app.directive('uniqueLisatietotyyppiField', function($log) {
    $log = $log.getInstance("uniqueLisatietotyyppiField directive");

    return {
        require: 'ngModel',
        scope: {
            callback: '&'
        },
        link: function(scope, elm, attrs, ctrl) {
            var parserValidator = function(viewValue) {
                $log.log("parserValidator");
                console.log(scope, elm, attrs, ctrl);
                ctrl.$setValidity('unique', !scope.callback()(viewValue));
                return viewValue;
            };
            ctrl.$parsers.push(parserValidator);
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
                var lang = (attrs.ophValidatePostcode === "yt" ? scope.model.ytlang : scope.model.hplang);
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

// Asettaa kieleen sidotun virheen jos kenttää ei ole täytetty
app.directive('ophRequired', function($log) {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            var validator = function(viewValue) {
                scope.model.updateYhteystiedotValidity(ctrl);
                return viewValue;
            };
            ctrl.$parsers.unshift(validator);
            ctrl.$formatters.unshift(validator);
        }
    };
});

app.directive("dynamicName", function($compile, $log) {
    return {
        restrict: "A",
        terminal: true,
        priority: 1000,
        link: function(scope, element, attrs) {
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
        require: ['ngModel', '^form'],
        restrict: "A",
        link: function(scope, elm, attrs, ctrls) {
            ctrls[0].$formatters.unshift(function(viewValue) {
                if (typeof viewValue === 'string' && viewValue.length > 0 && scope.$eval(attrs.ophSetDirty) === true) {
                    ctrls[1].$setDirty();
                }
                return viewValue;
            });
        }
    };
});

// Konvertoi &amp; => &
app.directive("ophDecodeName", function($log) {
    return {
        require: 'ngModel',
        restrict: "A",
        link: function(scope, elm, attrs, ctrl) {
            var formatterValidator = function(viewValue) {
                if (viewValue) {
                    return viewValue.replace(/&amp;/g, '&');
                }
                return viewValue;
            };
            ctrl.$formatters.unshift(formatterValidator);
            ctrl.$parsers.unshift(formatterValidator);
        }
    };
});

app.directive('ophFileupload', function($log, $http) {
    $log = $log.getInstance("ophFileupload directive");

    $log.info('init file upload');
    return {
        restrict: 'A',
        require: '^form',
        link: function(scope, element, attrs, ctrl) {
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
                            ctrl.$setDirty();
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

app.directive('fileDirty', function(){
    return {
        require : '^form',
        transclude : true,
        link : function($scope, elm, attrs, formCtrl){
            elm.on('change', function(){
                formCtrl.$setDirty();
                $scope.$apply();
            });
        }
    }
  });

// Estä enterin painalluksen default-toiminto
app.directive('ophEnter', function() {
    return  {
        restrict: 'A',
        link: function(scope, element, attrs) {
            element.bind("keydown keypress", function(event) {
                if (event.which === 13) {
                    event.preventDefault();
                }
            });
        }
    };
});

// Lisätään http prefix urliin
app.directive('httpPrefix', function() {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, element, attrs, controller) {
            controller.$validators.validateHttp = function ensureHttpPrefix(modelValue, viewValue) {
                // Need to add prefix if we don't have http:// prefix already AND we don't have part of it
                if(needsHttpPrefix(viewValue) || needsHttpPrefix(modelValue)) {
                    controller.$setViewValue('http://' + viewValue);
                    controller.$render();
                }
                return true;
            };
            function needsHttpPrefix(value) {
                return (value && !/^(https?):\/\//i.test(value) &&
                    'http://'.indexOf(value) !== 0 && 'https://'.indexOf(value) !== 0);
            }
        }
    };
});
