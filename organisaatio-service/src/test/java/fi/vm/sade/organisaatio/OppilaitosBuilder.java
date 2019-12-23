package fi.vm.sade.organisaatio;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;

public class OppilaitosBuilder extends OrganisaatioBuilder<OppilaitosBuilder> {

    private String oppilaitoskoodi;
    private String oppilaitostyyppi;

    public OppilaitosBuilder(String oid) {
        super(oid);
        this.tyyppi(OrganisaatioTyyppi.OPPILAITOS);
    }

    public OppilaitosBuilder oppilaitoskoodi(String oppilaitoskoodi) {
        this.oppilaitoskoodi = oppilaitoskoodi;
        return this;
    }

    public OppilaitosBuilder oppilaitostyyppi(String oppilaitostyyppi) {
        this.oppilaitostyyppi = oppilaitostyyppi;
        return this;
    }

    public Organisaatio build() {
        Organisaatio organisaatio = super.build();
        organisaatio.setOppilaitosKoodi(oppilaitoskoodi);
        organisaatio.setOppilaitosTyyppi(oppilaitostyyppi);
        return organisaatio;
    }

}
