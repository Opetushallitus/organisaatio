package fi.vm.sade.organisaatio.business;

import java.util.Set;

public interface LisatietoService {
    /**
     * Metodi kaikkien organisaation lisätietotyyppien hakemiseen
     * @return lokalisointipalvelusta löytyvät uniikit avaimet lisätietotyypeille
     */
    Set<String> getLisatietotyypit();
}
