package fi.vm.sade.organisaatio.resource.dto;

import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDate;

public class RyhmaCriteriaDtoV3 {

    private String q;
    private Boolean aktiivinen;
    private LocalDate lakkautusPvm;
    @Parameter(description = "Koodisto 'ryhmatyypit'")
    private String ryhmatyyppi;
    @Parameter(description = "Koodisto 'kayttoryhmat'")
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
