package fi.vm.sade.organisaatio.resource.dto;

import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDate;

public class RyhmaCriteriaDtoV2 {

    private String q;
    private Boolean aktiivinen;
    private LocalDate lakkautusPvm;
    @Parameter()//allowableValues = "organisaatio, hakukohde, perustetyoryhma, koulutus")
    private String ryhmatyyppi;
    @Parameter()//allowableValues = "yleinen, hakukohde_rajaava, hakukohde_priorisoiva, hakukohde_liiteosoite, perusteiden_laadinta, kayttooikeus")
    private String kayttoryhma;

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public Boolean getAktiivinen() {
        return aktiivinen;
    }

    public void setAktiivinen(Boolean aktiivinen) {
        this.aktiivinen = aktiivinen;
    }

    public LocalDate getLakkautusPvm() {
        return lakkautusPvm;
    }

    public void setLakkautusPvm(LocalDate lakkautusPvm) {
        this.lakkautusPvm = lakkautusPvm;
    }

    public String getRyhmatyyppi() {
        return ryhmatyyppi;
    }

    public void setRyhmatyyppi(String ryhmatyyppi) {
        this.ryhmatyyppi = ryhmatyyppi;
    }

    public String getKayttoryhma() {
        return kayttoryhma;
    }

    public void setKayttoryhma(String kayttoryhma) {
        this.kayttoryhma = kayttoryhma;
    }

}
