package fi.vm.sade.varda.rekisterointi;

import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import org.springframework.data.repository.CrudRepository;

public interface RekisterointiRepository extends CrudRepository<Rekisterointi, Long> {
}
