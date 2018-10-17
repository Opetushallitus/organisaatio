package fi.vm.sade.organisaatio.dto;

import java.time.LocalDate;

public class VarhaiskasvatuksenToimintamuotoDto {
    private String toimintamuoto;

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

    public String getToimintamuoto() {
        return toimintamuoto;
    }

    public void setToimintamuoto(String toimintamuoto) {
        this.toimintamuoto = toimintamuoto;
    }
}
