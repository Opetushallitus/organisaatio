package fi.vm.sade.organisaatio.dto;

import java.util.HashSet;
import java.util.Set;

public class VarhaiskasvatuksenToimipaikkaTiedotDto {
    private String toimintamuoto;

    private String kasvatusopillinenJarjestelma;

    private Set<String> varhaiskasvatuksenJarjestamismuodot = new HashSet<>();

    private Long paikkojenLukumaara;

    private Set<VarhaiskasvatuksenKielipainotusDto> varhaiskasvatuksenKielipainotukset = new HashSet<>();

    private Set<VarhaiskasvatuksenToiminnallinepainotusDto> varhaiskasvatuksenToiminnallinenpainotukset = new HashSet<>();

    public String getToimintamuoto() {
        return toimintamuoto;
    }

    public void setToimintamuoto(String toimintamuoto) {
        this.toimintamuoto = toimintamuoto;
    }

    public String getKasvatusopillinenJarjestelma() {
        return kasvatusopillinenJarjestelma;
    }

    public void setKasvatusopillinenJarjestelma(String kasvatusopillinenJarjestelma) {
        this.kasvatusopillinenJarjestelma = kasvatusopillinenJarjestelma;
    }

    public Long getPaikkojenLukumaara() {
        return paikkojenLukumaara;
    }

    public void setPaikkojenLukumaara(Long paikkojenLukumaara) {
        this.paikkojenLukumaara = paikkojenLukumaara;
    }

    public Set<VarhaiskasvatuksenKielipainotusDto> getVarhaiskasvatuksenKielipainotukset() {
        return varhaiskasvatuksenKielipainotukset;
    }

    public void setVarhaiskasvatuksenKielipainotukset(Set<VarhaiskasvatuksenKielipainotusDto> varhaiskasvatuksenKielipainotukset) {
        this.varhaiskasvatuksenKielipainotukset = varhaiskasvatuksenKielipainotukset;
    }

    public Set<VarhaiskasvatuksenToiminnallinepainotusDto> getVarhaiskasvatuksenToiminnallinenpainotukset() {
        return varhaiskasvatuksenToiminnallinenpainotukset;
    }

    public void setVarhaiskasvatuksenToiminnallinenpainotukset(Set<VarhaiskasvatuksenToiminnallinepainotusDto> varhaiskasvatuksenToiminnallinenpainotukset) {
        this.varhaiskasvatuksenToiminnallinenpainotukset = varhaiskasvatuksenToiminnallinenpainotukset;
    }

    public Set<String> getVarhaiskasvatuksenJarjestamismuodot() {
        return varhaiskasvatuksenJarjestamismuodot;
    }

    public void setVarhaiskasvatuksenJarjestamismuodot(Set<String> varhaiskasvatuksenJarjestamismuodot) {
        this.varhaiskasvatuksenJarjestamismuodot = varhaiskasvatuksenJarjestamismuodot;
    }
}
