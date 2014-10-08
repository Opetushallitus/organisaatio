app.factory('YritysValintaModel', function($filter, $log, Alert,
                                           YTJYritysTiedot, YTJYritystenTiedot) {

    $log = $log.getInstance("YritysValintaModel");

    var model = {
        hakuString: "",
        yritykset: [],

        isYTunnus: function(str) {
            // Y-Tunnus on muotoa NNNNNNN-T
            if (str.length !== 9) {
                return false;
            }
            // Tarkistetaan että numeropositioissa on vain numeroita.
            if (!/\d{7}.\d/.test(str)) {
                return false;
            }
            // Tarkistetaan että välimerkki on väliviiva.
            if (str.charAt(7) !== "-") {
                return false;
            }
            return true;
        },


        refresh: function() {
            $log.log('refresh()');
            var start = +new Date();

            if (this.isYTunnus(model.hakuString)) {
                YTJYritysTiedot.get({ytunnus: model.hakuString}, function(result) {
                    var end = +new Date();  // log end timestamp
                    var diff = end - start;
                    $log.log("Haku kesti: " +diff);
                    model.yritykset.length = 0;
                    model.yritykset.push(result);

                    if (model.yritykset.length === 0) {
                        Alert.add("warnign", $filter('i18n')("YritysValinta.eiHakutuloksia", ""), true, true);
                    }
                },
                // Error case
                function(response) {
                    $log.error("YTJYritysTiedot response: " + response.status);
                    Alert.add("error", $filter('i18n')("YritysValinta.virheViesti", ""), true, true);
                });
            }
            else {
                YTJYritystenTiedot.get({nimi: model.hakuString}, function(result) {
                    var end = +new Date();  // log end timestamp
                    var diff = end - start;
                    $log.log("Haku kesti: " +diff);
                    model.yritykset = result;

                    if (model.hakuString.length>0 && model.yritykset.length === 0) {
                        Alert.add("warnign", $filter('i18n')("YritysValinta.eiHakutuloksia", ""), true, true);
                    }
                },
                // Error case
                function(response) {
                    $log.error("YTJYritystenTiedot response: " + response.status);
                    Alert.add("error", $filter('i18n')("YritysValinta.virheViesti", ""), true, true);
                });
            }
        }
    };

    return model;
});



