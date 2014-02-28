app.factory('YhteystietojentyyppiModel', function(
        KoodistoOrganisaatiotyypit, KoodistoOppilaitostyypit, KoodistoKoodi,
        Yhteystietojentyyppi, YhteystietojentyypinPoisto)  {

    var model = new function() {
        var organisaatiotyypit = {},
                oppilaitostyypit = [],
                yhteystietotyypit = [],
                oppilaitostyypitMap = {};

        this.organisaatiotyypit = organisaatiotyypit;
        this.oppilaitostyypit = oppilaitostyypit;
        this.yhteystietotyypit = yhteystietotyypit;
        this.oppilaitostyypitMap = oppilaitostyypitMap;

        function loadYhteystietotyypit() {
            Yhteystietojentyyppi.get({}, function(tyypit) {
                tyypit.forEach(function(t) {
                    yhteystietotyypit.push(t);
                });
            });
        }

        function loadOppilaitostyypit() {
            KoodistoOppilaitostyypit.get({}, function(tyypit) {
                tyypit.forEach(function(t) {
                    oppilaitostyypit.push({id: t.koodiUri + '#' + t.versio, nimi: KoodistoKoodi.getLocalizedName(t)});
                    oppilaitostyypitMap[t.koodiUri + '#' + t.versio] = KoodistoKoodi.getLocalizedName(t);
                });
                loadYhteystietotyypit();
            });
        }

        loadOppilaitostyypit();

        this.uusiYtt = function() {
            var obj = {
                version: 0,
                oid: null,
                nimi: {
                    teksti: [{
                        value: '',
                        kieliKoodi: 'fi'
                    }, {
                       value: '',
                       kieliKoodi: 'sv'
                    }]
                },
                sovellettavatOrganisaatios: [],
                sovellettavatOppilaitostyyppis: [],
                allLisatietokenttas: []
            };
            yhteystietotyypit.push(obj);
            return obj;
        };

        this.save = function(ytt, callback, virheCallback) {
            var fn;
            if (ytt.oid === null) {
                fn = Yhteystietojentyyppi.put;
            } else {
                fn = Yhteystietojentyyppi.post;
            }
            fn(ytt, function(tyyppi) {
                var ind = yhteystietotyypit.indexOf(ytt);
                if (ind !== -1) {
                    yhteystietotyypit.splice(ind, 1);
                }
                yhteystietotyypit.push(tyyppi);
                callback(tyyppi);
            }, virheCallback);
        };

        this.delete = function(ytt, callback, virheCallback) {
            YhteystietojentyypinPoisto.delete({oid: ytt.oid}, callback, virheCallback);
        };

        this.reload = function() {
            loadOppilaitostyypit();
        };

    };

    return model;
});
