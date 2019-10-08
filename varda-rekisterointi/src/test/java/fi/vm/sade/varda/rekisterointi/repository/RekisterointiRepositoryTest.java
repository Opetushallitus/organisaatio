package fi.vm.sade.varda.rekisterointi.repository;

import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.model.TestiRekisterointi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class RekisterointiRepositoryTest {

    @Autowired
    private RekisterointiRepository rekisterointiRepository;

    @Test
    public void findByTilaQueryIsValid() {
        assertNotNull(rekisterointiRepository);
        Iterable<Rekisterointi> results = rekisterointiRepository.findByTila(Rekisterointi.Tila.KASITTELYSSA.toString());
        assertNotNull(results); // heittää tätä ennen poikkeuksen, jos ei skulaa
    }

    @Test
    public void savesRekisterointi() {
        Rekisterointi rekisterointi = TestiRekisterointi.validiRekisterointi();
        rekisterointi = rekisterointiRepository.save(rekisterointi);
        assertNotNull(rekisterointi.id);
    }

}
