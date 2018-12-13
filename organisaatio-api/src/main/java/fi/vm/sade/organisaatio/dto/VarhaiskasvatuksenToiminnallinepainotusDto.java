package fi.vm.sade.organisaatio.dto;

import java.time.LocalDate;

public class VarhaiskasvatuksenToiminnallinepainotusDto {
    private String toiminnallinenpainotus;

    private LocalDate alkupvm;

    private LocalDate loppupvm;

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

    public String getToiminnallinenpainotus() {
        return toiminnallinenpainotus;
    }

    public void setToiminnallinenpainotus(String toiminnallinenpainotus) {
        this.toiminnallinenpainotus = toiminnallinenpainotus;
    }
}
