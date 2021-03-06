package fi.vm.sade.organisaatio.business;

import fi.vm.sade.organisaatio.dto.Koodi;
import fi.vm.sade.organisaatio.model.Organisaatio;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface OrganisaatioKoodisto {

    void paivitaKoodisto(String oid);

    void paivitaKoodisto(Organisaatio entity);

    String lakkautaKoodi(String uri, String tunniste, Date lakkautusPvm);

    List<Koodi> haeKoodit(KoodistoUri koodisto, int versio);

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
        KIELI("kieli"),
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
