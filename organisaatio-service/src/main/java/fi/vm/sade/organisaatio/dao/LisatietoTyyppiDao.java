package fi.vm.sade.organisaatio.dao;

import fi.vm.sade.generic.dao.JpaDAO;
import fi.vm.sade.organisaatio.model.Lisatietotyyppi;

import java.util.List;
import java.util.Set;

public interface LisatietoTyyppiDao extends JpaDAO<Lisatietotyyppi, Long> {
    Set<String> findValidByOrganisaatiotyyppiAndOppilaitostyyppi(List<String> organisaatiotyyppis, String oppilaitostyyppi);
}
