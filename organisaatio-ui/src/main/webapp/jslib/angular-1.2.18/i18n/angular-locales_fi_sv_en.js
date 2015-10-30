/**
 * Created by pryhanen on 26.10.2015.
 */

'use strict';
angular.module("ngLocale", [])

.constant('locales', {
        fi: {
            "DATETIME_FORMATS": {
                "AMPMS": [
                    "ap.",
                    "ip."
                ],
                "DAY": [
                    "sunnuntaina",
                    "maanantaina",
                    "tiistaina",
                    "keskiviikkona",
                    "torstaina",
                    "perjantaina",
                    "lauantaina"
                ],
                "MONTH": [
                    "tammikuuta",
                    "helmikuuta",
                    "maaliskuuta",
                    "huhtikuuta",
                    "toukokuuta",
                    "kes\u00e4kuuta",
                    "hein\u00e4kuuta",
                    "elokuuta",
                    "syyskuuta",
                    "lokakuuta",
                    "marraskuuta",
                    "joulukuuta"
                ],
                "SHORTDAY": [
                    "su",
                    "ma",
                    "ti",
                    "ke",
                    "to",
                    "pe",
                    "la"
                ],
                "SHORTMONTH": [
                    "tammikuuta",
                    "helmikuuta",
                    "maaliskuuta",
                    "huhtikuuta",
                    "toukokuuta",
                    "kes\u00e4kuuta",
                    "hein\u00e4kuuta",
                    "elokuuta",
                    "syyskuuta",
                    "lokakuuta",
                    "marraskuuta",
                    "joulukuuta"
                ],
                "fullDate": "cccc, d. MMMM y",
                "longDate": "d. MMMM y",
                "medium": "d.M.yyyy H.mm.ss",
                "mediumDate": "d.M.yyyy",
                "mediumTime": "H.mm.ss",
                "short": "d.M.yyyy H.mm",
                "shortDate": "d.M.yyyy",
                "shortTime": "H.mm"
            },
            "NUMBER_FORMATS": {
                "CURRENCY_SYM": "\u20ac",
                "DECIMAL_SEP": ",",
                "GROUP_SEP": "\u00a0",
                "PATTERNS": [
                    {
                        "gSize": 3,
                        "lgSize": 3,
                        "macFrac": 0,
                        "maxFrac": 3,
                        "minFrac": 0,
                        "minInt": 1,
                        "negPre": "-",
                        "negSuf": "",
                        "posPre": "",
                        "posSuf": ""
                    },
                    {
                        "gSize": 3,
                        "lgSize": 3,
                        "macFrac": 0,
                        "maxFrac": 2,
                        "minFrac": 2,
                        "minInt": 1,
                        "negPre": "-",
                        "negSuf": "\u00a0\u00a4",
                        "posPre": "",
                        "posSuf": "\u00a0\u00a4"
                    }
                ]
            },
            "id": "fi-fi",
            "pluralCat": function (n) {
                if (n == 1) {
                    return PLURAL_CATEGORY.ONE;
                }
                return PLURAL_CATEGORY.OTHER;
            }
        },
        sv: {
            "DATETIME_FORMATS": {
                "AMPMS": [
                    "fm",
                    "em"
                ],
                "DAY": [
                    "s\u00f6ndag",
                    "m\u00e5ndag",
                    "tisdag",
                    "onsdag",
                    "torsdag",
                    "fredag",
                    "l\u00f6rdag"
                ],
                "MONTH": [
                    "januari",
                    "februari",
                    "mars",
                    "april",
                    "maj",
                    "juni",
                    "juli",
                    "augusti",
                    "september",
                    "oktober",
                    "november",
                    "december"
                ],
                "SHORTDAY": [
                    "s\u00f6n",
                    "m\u00e5n",
                    "tis",
                    "ons",
                    "tors",
                    "fre",
                    "l\u00f6r"
                ],
                "SHORTMONTH": [
                    "jan",
                    "feb",
                    "mar",
                    "apr",
                    "maj",
                    "jun",
                    "jul",
                    "aug",
                    "sep",
                    "okt",
                    "nov",
                    "dec"
                ],
                "fullDate": "EEEE'en' 'den' d:'e' MMMM y",
                "longDate": "d MMMM y",
                "medium": "d MMM y HH:mm:ss",
                "mediumDate": "d MMM y",
                "mediumTime": "HH:mm:ss",
                "short": "yyyy-MM-dd HH:mm",
                "shortDate": "yyyy-MM-dd",
                "shortTime": "HH:mm"
            },
            "NUMBER_FORMATS": {
                "CURRENCY_SYM": "kr",
                "DECIMAL_SEP": ",",
                "GROUP_SEP": "\u00a0",
                "PATTERNS": [
                    {
                        "gSize": 3,
                        "lgSize": 3,
                        "macFrac": 0,
                        "maxFrac": 3,
                        "minFrac": 0,
                        "minInt": 1,
                        "negPre": "-",
                        "negSuf": "",
                        "posPre": "",
                        "posSuf": ""
                    },
                    {
                        "gSize": 3,
                        "lgSize": 3,
                        "macFrac": 0,
                        "maxFrac": 2,
                        "minFrac": 2,
                        "minInt": 1,
                        "negPre": "-",
                        "negSuf": "\u00a0\u00a4",
                        "posPre": "",
                        "posSuf": "\u00a0\u00a4"
                    }
                ]
            },
            "id": "sv-se",
            "pluralCat": function (n) {
                if (n == 1) {
                    return PLURAL_CATEGORY.ONE;
                }
                return PLURAL_CATEGORY.OTHER;
            }
        },
        en: {
            "DATETIME_FORMATS": {
                "AMPMS": [
                    "AM",
                    "PM"
                ],
                "DAY": [
                    "Sunday",
                    "Monday",
                    "Tuesday",
                    "Wednesday",
                    "Thursday",
                    "Friday",
                    "Saturday"
                ],
                "MONTH": [
                    "January",
                    "February",
                    "March",
                    "April",
                    "May",
                    "June",
                    "July",
                    "August",
                    "September",
                    "October",
                    "November",
                    "December"
                ],
                "SHORTDAY": [
                    "Sun",
                    "Mon",
                    "Tue",
                    "Wed",
                    "Thu",
                    "Fri",
                    "Sat"
                ],
                "SHORTMONTH": [
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec"
                ],
                "fullDate": "EEEE, d MMMM y",
                "longDate": "d MMMM y",
                "medium": "d MMM y HH:mm:ss",
                "mediumDate": "d MMM y",
                "mediumTime": "HH:mm:ss",
                "short": "dd/MM/yyyy HH:mm",
                "shortDate": "dd/MM/yyyy",
                "shortTime": "HH:mm"
            },
            "NUMBER_FORMATS": {
                "CURRENCY_SYM": "\u00a3",
                "DECIMAL_SEP": ".",
                "GROUP_SEP": ",",
                "PATTERNS": [
                    {
                        "gSize": 3,
                        "lgSize": 3,
                        "macFrac": 0,
                        "maxFrac": 3,
                        "minFrac": 0,
                        "minInt": 1,
                        "negPre": "-",
                        "negSuf": "",
                        "posPre": "",
                        "posSuf": ""
                    },
                    {
                        "gSize": 3,
                        "lgSize": 3,
                        "macFrac": 0,
                        "maxFrac": 2,
                        "minFrac": 2,
                        "minInt": 1,
                        "negPre": "\u00a4-",
                        "negSuf": "",
                        "posPre": "\u00a4",
                        "posSuf": ""
                    }
                ]
            },
            "id": "en-gb",
            "pluralCat": function (n) {
                if (n == 1) {
                    return PLURAL_CATEGORY.ONE;
                }
                return PLURAL_CATEGORY.OTHER;
            }
        }
    })

.config(['$provide', 'locales',
        function($provide, locales) {
    var PLURAL_CATEGORY = {ZERO: "zero", ONE: "one", TWO: "two", FEW: "few", MANY: "many", OTHER: "other"};
    // Default fi-fi locale
    $provide.value("$locale", locales.fi);
}])

.service('AngularLocaleManager', ['locales', '$filter', '$locale',
        function(locales, $filter, $locale) {
    var PLURAL_CATEGORY = {ZERO: "zero", ONE: "one", TWO: "two", FEW: "few", MANY: "many", OTHER: "other"};

    // Set locale from locales list
    this.setAngularLocale = function(lang) {
        if(angular.isDefined(lang)) {
            var language = lang.toLowerCase();
            if(angular.isDefined(locales[language]) && locales[language] !== $locale) {
                angular.copy(locales[lang], $locale);
            }
        }
    }
}]);
