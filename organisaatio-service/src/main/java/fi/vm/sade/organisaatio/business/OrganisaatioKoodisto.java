package fi.vm.sade.organisaatio.business;

import fi.vm.sade.organisaatio.model.Organisaatio;

import java.util.Date;

public interface OrganisaatioKoodisto {

    void paivitaKoodistoAsync(Organisaatio entity, boolean reauthorize);

    String paivitaKoodisto(Organisaatio entity, boolean reauthorize);

    String lakkautaKoodi(String uri, String tunniste, Date lakkautusPvm, boolean reauthorize);

    public enum KoodistoUri {
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
