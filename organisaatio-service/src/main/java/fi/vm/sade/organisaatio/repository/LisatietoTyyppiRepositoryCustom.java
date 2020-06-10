package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.organisaatio.model.Lisatietotyyppi;

import java.util.Optional;
import java.util.Set;

public interface LisatietoTyyppiRepositoryCustom {
    Set<String> findValidByOrganisaatiotyyppiAndOppilaitostyyppi(String organisaatioOid);

    Optional<Lisatietotyyppi> findByNimi(String nimi);

}
