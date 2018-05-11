package fi.vm.sade.organisaatio.business;

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
}
