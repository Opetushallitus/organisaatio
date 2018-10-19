package fi.vm.sade.organisaatio.dto;

import java.util.HashSet;
import java.util.Set;

public class VarhaiskasvatuksenToimipaikkaTiedotDto {
    private String jarjestamismuoto;

    private String kasvatusopillinenJarjestelma;

    private String toiminnallinenPainotus;

    private Long paikkojenLukumaara;

    private Set<VarhaiskasvatuksenKielipainotusDto> varhaiskasvatuksenKielipainotukset = new HashSet<>();

    private Set<VarhaiskasvatuksenToimintamuotoDto> varhaiskasvatuksenToimintamuodot = new HashSet<>();

    public String getJarjestamismuoto() {
        return jarjestamismuoto;
    }

    public void setJarjestamismuoto(String jarjestamismuoto) {
        this.jarjestamismuoto = jarjestamismuoto;
    }

    public String getKasvatusopillinenJarjestelma() {
        return kasvatusopillinenJarjestelma;
    }

    public void setKasvatusopillinenJarjestelma(String kasvatusopillinenJarjestelma) {
        this.kasvatusopillinenJarjestelma = kasvatusopillinenJarjestelma;
    }

    public String getToiminnallinenPainotus() {
        return toiminnallinenPainotus;
    }

    public void setToiminnallinenPainotus(String toiminnallinenPainotus) {
        this.toiminnallinenPainotus = toiminnallinenPainotus;
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

    public Set<VarhaiskasvatuksenToimintamuotoDto> getVarhaiskasvatuksenToimintamuodot() {
        return varhaiskasvatuksenToimintamuodot;
    }

    public void setVarhaiskasvatuksenToimintamuodot(Set<VarhaiskasvatuksenToimintamuotoDto> varhaiskasvatuksenToimintamuodot) {
        this.varhaiskasvatuksenToimintamuodot = varhaiskasvatuksenToimintamuodot;
    }
}
