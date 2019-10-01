package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.repository.RekisterointiRepository;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RekisterointiService {

    private final RekisterointiRepository repository;

    public RekisterointiService(RekisterointiRepository repository) {
        this.repository = repository;
    }

    public Iterable<Rekisterointi> list() {
        return repository.findAll(); // TODO: rajaus kunnan/päättäjän perusteella? KJHH-1709
    }

    public long create(Rekisterointi rekisterointi) {
        return repository.save(rekisterointi).id;
    }

    public Rekisterointi resolve(Paatos paatos) {
        Rekisterointi rekisterointi = repository.findById(paatos.rekisterointi).orElseThrow(
                () -> new InvalidInputException("Rekisteröintiä ei löydy, id: " + paatos.rekisterointi));
        return repository.save(rekisterointi.withPaatos(paatos));
    }

}
