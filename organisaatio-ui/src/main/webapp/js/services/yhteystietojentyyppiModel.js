app.factory('YhteystietojentyyppiModel', function(
        KoodistoOrganisaatiotyypit, KoodistoOppilaitostyypit, KoodistoKoodi,
        Yhteystietojentyyppi)  {

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
                versio: 0,
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

    };

    return model;
});
