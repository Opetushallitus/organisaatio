package fi.vm.sade.organisaatio.business.impl;


import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.model.Organisaatio;

import java.util.Collections;
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
        return Collections.singleton("vardajarjestamismuoto_jm03");
    }

    @Override
    public Set<String> haeVardaKasvatusopillinenJarjestelma() {
        return Collections.singleton("vardakasvatusopillinenjarjestelma_kj99");
    }

    @Override
    public Set<String> haeVardaToiminnallinenPainotus() {
        return Collections.singleton("vardatoiminnallinenpainotus_tp99");
    }

    @Override
    public Set<String> haeVardaToimintamuoto() {
        return Collections.singleton("vardatoimintamuoto_tm02");
    }

    @Override
    public Set<String> haeKielikoodit() {
        return Collections.singleton("kieli_bh");
    }
}
