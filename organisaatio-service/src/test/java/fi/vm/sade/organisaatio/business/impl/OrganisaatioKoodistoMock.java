package fi.vm.sade.organisaatio.business.impl;


import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.model.Organisaatio;

import java.util.Date;
import java.util.Set;

public class OrganisaatioKoodistoMock implements OrganisaatioKoodisto {

    @Override
    public void paivitaKoodistoAsync(Organisaatio entity, boolean reauthorize) {
        // nop
    }

    @Override
    public String paivitaKoodisto(Organisaatio entity, boolean reauthorize) {
        return null;
    }

    @Override
    public String lakkautaKoodi(String uri, String tunniste, Date lakkautusPvm, boolean reauthorize) {
        return null;
    }

    @Override
    public Set<String> haeOppilaitoskoodit() {
        return null;
    }

    @Override
    public Set<String> haeVardaJarjestamismuoto() {
        return null;
    }

    @Override
    public Set<String> haeVardaKasvatusopillinenJarjestelma() {
        return null;
    }

    @Override
    public Set<String> haeVardaToiminnallinenPainotus() {
        return null;
    }

    @Override
    public Set<String> haeVardaToimintamuoto() {
        return null;
    }

    @Override
    public Set<String> haeKielikoodit() {
        return null;
    }
}
