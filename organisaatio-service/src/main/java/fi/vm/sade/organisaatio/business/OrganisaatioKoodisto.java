package fi.vm.sade.organisaatio.business;

import fi.vm.sade.organisaatio.model.Organisaatio;

import java.util.Date;
import java.util.Set;

public interface OrganisaatioKoodisto {

    void paivitaKoodistoAsync(Organisaatio entity);

    String paivitaKoodisto(Organisaatio entity);

    String lakkautaKoodi(String uri, String tunniste, Date lakkautusPvm);

    /**
     * Hakee kaikki oppilaitoskoodit
     * @return Setti oppilaitoskoodeja
     */
    Set<String> haeOppilaitoskoodit();

    Set<String> haeVardaJarjestamismuoto();

    Set<String> haeVardaKasvatusopillinenJarjestelma();

    Set<String> haeVardaToiminnallinenPainotus();

    Set<String> haeVardaToimintamuoto();

    Set<String> haeKielikoodit();

    enum KoodistoUri {
        TOIMIPISTE("opetuspisteet"),
        OPPILAITOS("oppilaitosnumero"),
        KOULUTUSTOIMIJA("koulutustoimija"),
        YHTEISHAUNKOULUKOODI("yhteishaunkoulukoodi");
        private final String uri;

        KoodistoUri(String koodistoUri) {
            uri = koodistoUri;
        }

        public String uri() {
            return uri;
        }
    }
}
