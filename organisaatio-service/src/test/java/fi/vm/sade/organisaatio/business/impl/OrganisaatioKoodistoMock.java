package fi.vm.sade.organisaatio.business.impl;


import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.dto.Koodi;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Primary
@Component
public class OrganisaatioKoodistoMock implements OrganisaatioKoodisto {

    @Override
    public void paivitaKoodisto(String oid) {
        // nop
    }

    @Override
    public void paivitaKoodisto(Organisaatio entity) {
        // nop
    }

    @Override
    public String lakkautaKoodi(String uri, String tunniste, Date lakkautusPvm) {
        return null;
    }

    @Override
    public List<Koodi> haeKoodit(KoodistoUri koodisto, Optional<Integer> versio, Optional<Boolean> onlyValid) {
        switch (koodisto) {
            case KIELI:
                return Stream.of("fi", "sv", "en")
                        .map(koodi -> new Koodi(koodi.toUpperCase(), String.format("kieli_%s", koodi), versio.orElse(1)))
                        .collect(toList());
            default:
                throw new IllegalArgumentException(String.format("Koodisto %s ei ole tuettu mockissa", koodisto));
        }
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
