package fi.vm.sade.organisaatio.business;

import fi.vm.sade.organisaatio.model.Organisaatio;

import java.util.Date;
import java.util.Set;

public interface OrganisaatioKoodisto {

    @Deprecated
    default void paivitaKoodistoAsync(Organisaatio entity, boolean reauthorize) {
        paivitaKoodistoAsync(entity);
    }

    void paivitaKoodistoAsync(Organisaatio entity);

    @Deprecated
    default String paivitaKoodisto(Organisaatio entity, boolean reauthorize) {
        return paivitaKoodisto(entity);
    }

    String paivitaKoodisto(Organisaatio entity);

    @Deprecated
    default String lakkautaKoodi(String uri, String tunniste, Date lakkautusPvm, boolean reauthorize) {
        return lakkautaKoodi(uri, tunniste, lakkautusPvm);
    }

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
