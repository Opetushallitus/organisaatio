package fi.vm.sade.organisaatio;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;

public class KoulutustoimijaBuilder extends OrganisaatioBuilder<KoulutustoimijaBuilder> {

    private String ytunnus;

    public KoulutustoimijaBuilder(String oid) {
        super(oid);
        this.tyyppi(OrganisaatioTyyppi.KOULUTUSTOIMIJA);
    }

    public KoulutustoimijaBuilder ytunnus(String ytunnus) {
        this.ytunnus = ytunnus;
        return this;
    }

    public Organisaatio build() {
        Organisaatio organisaatio = super.build();
        organisaatio.setYtunnus(ytunnus);
        return organisaatio;
    }

}
