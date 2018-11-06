package fi.vm.sade.organisaatio.dto;

import java.time.LocalDate;

public class VarhaiskasvatuksenKielipainotusDto {
    private String kielipainotus;

    private LocalDate alkupvm;

    private LocalDate loppupvm;

    public String getKielipainotus() {
        return kielipainotus;
    }

    public void setKielipainotus(String kielipainotus) {
        this.kielipainotus = kielipainotus;
    }

    public LocalDate getAlkupvm() {
        return alkupvm;
    }

    public void setAlkupvm(LocalDate alkupvm) {
        this.alkupvm = alkupvm;
    }

    public LocalDate getLoppupvm() {
        return loppupvm;
    }

    public void setLoppupvm(LocalDate loppupvm) {
        this.loppupvm = loppupvm;
    }
}
