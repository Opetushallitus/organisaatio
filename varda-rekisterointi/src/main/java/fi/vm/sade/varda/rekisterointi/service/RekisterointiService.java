package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.repository.PaatosRepository;
import fi.vm.sade.varda.rekisterointi.repository.RekisterointiRepository;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RekisterointiService {

    private final RekisterointiRepository rekisterointiRepository;
    private final PaatosRepository paatosRepository;

    public RekisterointiService(RekisterointiRepository rekisterointiRepository, PaatosRepository paatosRepository) {
        this.rekisterointiRepository = rekisterointiRepository;
        this.paatosRepository = paatosRepository;
    }

    public Iterable<Rekisterointi> listByTila(Rekisterointi.Tila tila) {
        return rekisterointiRepository.findByTila(tila.toString()); // TODO: rajaus kunnan/päättäjän perusteella? KJHH-1709
    }

    public long create(Rekisterointi rekisterointi) {
        return rekisterointiRepository.save(rekisterointi).id;
    }

    public Rekisterointi resolve(Paatos paatos) {
        Rekisterointi rekisterointi = rekisterointiRepository.findById(paatos.rekisterointi).orElseThrow(
                () -> new InvalidInputException("Rekisteröintiä ei löydy, id: " + paatos.rekisterointi));
        paatosRepository.save(paatos);
        return rekisterointiRepository.save(
                rekisterointi.withTila(paatos.hyvaksytty ? Rekisterointi.Tila.HYVAKSYTTY : Rekisterointi.Tila.HYLATTY));
    }

}
