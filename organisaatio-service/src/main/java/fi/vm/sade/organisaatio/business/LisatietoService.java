package fi.vm.sade.organisaatio.business;

import fi.vm.sade.organisaatio.dto.LisatietotyyppiCreateDto;
import fi.vm.sade.organisaatio.dto.LisatietotyyppiDto;

import java.util.Set;

public interface LisatietoService {
    /**
     * Metodi kaikkien organisaation lisätietotyyppien hakemiseen
     * @return lokalisointipalvelusta löytyvät uniikit avaimet lisätietotyypeille
     */
    Set<String> getLisatietotyypit();

    /**
     * Palauttaa kaikki lisätietotyyppien sallimat lisätiedot tietylle organisaatiolle
     * @param oid organisaatio oid
     * @return sallittujen lisätietojen lokalisointipalvelun avaimet
     */
    Set<String> getSallitutByOid(String oid);

    /**
     * Luo uuden lisätietotyypin vapaaehtoisine rajoitteineen
     * @param lisatietotyyppiCreateDto lisätietotyypin nimi rajoitteineen
     * @return Luodun lisätietotyypin nimi
     */
    String create(LisatietotyyppiCreateDto lisatietotyyppiCreateDto);

    /**
     * Poistaa lisätietotyypin nimen perusteella.
     * @param nimi Poistettavan lisätietotyypin nimi
     */
    void delete(String nimi);

    /**
     * Hakee lisätietotyypin tiedot nimen perusteella
     * @param nimi lisätietotyypin nimi
     * @return Lisätietotyypin tiedot
     */
    LisatietotyyppiDto findByName(String nimi);
}
