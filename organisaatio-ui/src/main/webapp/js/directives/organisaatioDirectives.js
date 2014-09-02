app.directive('formatteddate', function($log, $filter) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, element, attrs, ctrl) {
            var maxDate = scope[attrs.max];
            var minDate = scope[attrs.min];

            function isRangeValid(date) {
                if (date < minDate || date > maxDate) {
                    return false;
                }
                return true;
            }

            // Tämä validoi päivämäärän
            function validateDate(viewValue) {
                $log.log("Validation starts");
                $log.log("Element value= "+element.val() + " ViewValue= " + viewValue + " ctrl.$viewValue= " +ctrl.$viewValue);

                if (viewValue === undefined) {
                    $log.log("ViewValue undefined");
                    ctrl.$setValidity('date', false);
                    ctrl.$setValidity('dateYear', true);
                    return null;
                }

                if (!viewValue) {
                    $log.log("ViewValue empty");
                    ctrl.$setValidity('date', true);
                    ctrl.$setValidity('dateYear', true);
                    return null;
                }

                if (typeof viewValue === "object" && moment(viewValue).isValid()) {
                    $log.log("Valid object ViewValue= " + viewValue);

                    var date = moment(viewValue);
                    if (!isRangeValid(date.toDate())) {
                        ctrl.$setValidity('dateYear', false);
                        return viewValue;
                    }
                    ctrl.$setValidity('dateYear', true);
                    ctrl.$setValidity('date', true);
                    return viewValue;
                }
                else if (angular.isString(viewValue)) {
                    $log.log("String ViewValue= " + viewValue);
                    if (!moment(viewValue,'DD.MM.YYYY').isValid()) {
                        $log.log("Invalid string viewValue= " + viewValue);
                        ctrl.$setValidity('date', false);
                        return undefined;
                    }
                    var date = moment(viewValue, 'DD.MM.YYYY');
                    if (!date.isValid()) {
                        $log.log("String ViewValue invalid");
                        ctrl.$setValidity('date', false);
                        return undefined;
                    }
                    if (!isRangeValid(date.toDate())) {
                        ctrl.$setValidity('dateYear', false);
                        return viewValue;
                    }
                    ctrl.$setValidity('dateYear', true);
                    ctrl.$setValidity('date', true);
                    return date.toDate();
                }
                else {
                    ctrl.$setValidity('date', false);
                    return undefined;
                }
            }
            ctrl.$parsers.unshift(validateDate);

            // Tämä hoitaa sen, että DatePicker saa päivämäärän oikeassa muodossa
            ctrl.$parsers.unshift(function(viewValue) {
                $log.log("Format starts");
                $log.log("ElementValue= " + element.val() + " ViewValue= " + viewValue + " ctrl.$viewValue= " +ctrl.$viewValue);

                // pass through if we clicked date from popup
                if (typeof ctrl.$viewValue === "object" || ctrl.$viewValue === "") {
                    $log.log("Pass through");
                    return ctrl.$viewValue;
                }
                var date = moment(ctrl.$viewValue, 'DD.MM.YYYY');
                if (date.isValid()) {
                    $log.log("Valid ctrl.$viewValue= " + ctrl.$viewValue);
                    ctrl.$setViewValue(date);
                    return date.toDate();
                }
                return ctrl.$viewValue;
            });
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

app.directive('uniqueYhteystietojenTyyppiField', function($log) {
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
app.directive("ophDecodeName", function($compile, $log) {
    return {
        require: 'ngModel',
        restrict: "A",
        link: function(scope, elm, attrs, ctrl) {
            var formatterValidator = function(viewValue) {
                if (viewValue) {
                    return viewValue.replace(/&amp;/g, '&');
                }
            };
            ctrl.$formatters.unshift(formatterValidator);
            ctrl.$parsers.unshift(formatterValidator);
        }
    };
});

app.directive('ophFileupload', function($log, $http) {
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
