package fi.vm.sade.organisaatio.dao;

import fi.vm.sade.generic.dao.JpaDAO;
import fi.vm.sade.organisaatio.model.Lisatietotyyppi;

import java.util.Optional;
import java.util.Set;

public interface LisatietoTyyppiDao extends JpaDAO<Lisatietotyyppi, Long> {
    Set<String> findValidByOrganisaatiotyyppiAndOppilaitostyyppi(String organisaatioOid);

    Optional<Lisatietotyyppi> findByNimi(String nimi);
}
