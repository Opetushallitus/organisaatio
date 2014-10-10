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

/**
 * Enhance logging output to contain datestamp + possible location information.
 *
 * Example log line: (time - debug - "class" :: log message)
 * <pre>
 * 085204 - D - BaseReviewController :: parents: []
 * </pre>
 *
 * If this module is loded the "$log" is enchanced with "getInstance(CLASS_NAME)" method
 * and the all log entries logged with that logger has that "ClASS_NAME" displayed.
 * Helps to locate logged lines in the code.
 */

app = angular.module("Logging", []);

app.config(["$provide", function($provide) {

        // console.log("Logging.config - enhance logging...");

        function formatDate(date) {
            var result = "";
            if (date) {
                var d = date; // new Date();

                result = result + ((d.getHours() < 10) ? "0" : "") + d.getHours();
                result = result + ":" + ((d.getMinutes() < 10) ? "0" : "") + d.getMinutes();
                result = result + " " + ((d.getSeconds() < 10) ? "0" : "") + d.getSeconds();
                result = result + "." + ((d.getMilliseconds() < 10) ? "0" : "") + ((d.getMilliseconds() < 100) ? "0" : "") + d.getMilliseconds();
            }

            return result;
        }


        $provide.decorator('$log', ["$delegate", function($delegate)
            {
                var _$log = (function($log)
                {
                    return {
                        log: $log.log,
                        info: $log.info,
                        warn: $log.warn,
                        debug: $log.debug,
                        error: $log.error
                    };
                })($delegate);

                var prepareLogFn = function(logFn, logLevel, logClass) {
                    var enhanced = function() {
                        var args = [].slice.call(arguments),
                                now = new Date();

                        // Prepend timestamp, level, class + actual result
                        args[0] = formatDate(now) + " - " + logLevel + " - " + logClass + " :: " + args[0];

                        // Call the original with the output prepended with formatted timestamp
                        logFn.apply(null, args);
                    };

                    // Special... only needed to support angular-mocks expectations
                    enhanced.logs = [ ];

                    return enhanced;
                };

                // Default implementations, no class name
                $delegate.log = prepareLogFn(_$log.log, "?", "?");
                $delegate.info = prepareLogFn(_$log.info, "I", "?");
                $delegate.warn = prepareLogFn(_$log.warn, "W", "?");
                $delegate.debug = prepareLogFn(_$log.debug, "D", "?");
                $delegate.error = prepareLogFn(_$log.error, "E", "?");
                //$delegate.error = _$log.error;

                // Class spesific implementations
                $delegate.getInstance = function(logClass) {
                    return {
                        log : prepareLogFn(_$log.log, "?", logClass),
                        info : prepareLogFn(_$log.info, "I", logClass),
                        warn : prepareLogFn(_$log.warn, "W", logClass),
                        debug : prepareLogFn(_$log.debug, "D", logClass),
                        error : prepareLogFn(_$log.error, "E", logClass)
                        //error : _$log.error
                    };
                };

                return $delegate;
            }]);

        // console.log("Logging.config - enhance logging... done.");

}]);
