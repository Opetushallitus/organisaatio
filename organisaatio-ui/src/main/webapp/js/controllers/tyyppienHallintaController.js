
app.controller('TyyppienHallintaController', function () {
    this.templates = [
        {nimi: 'tyyppienhallinta.yhteystietotyyppi', url: TEMPLATE_URL_BASE + 'yhteystietojentyyppi.html'},
        {nimi: 'tyyppienhallinta.lisatietotyyppi', url: TEMPLATE_URL_BASE + 'lisatietotyyppi.html'}
    ];
    this.template = this.templates[0];

    this.setTemplate = function (template) {
        this.template = template;
    }
});
