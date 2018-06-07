package fi.vm.sade.organisaatio.dto;

import java.util.HashSet;
import java.util.Set;

public class LisatietotyyppiDto {
    private String nimi;

    private Set<RajoiteDto> rajoitteet = new HashSet<>();


    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public Set<RajoiteDto> getRajoitteet() {
        return rajoitteet;
    }

    public void setRajoitteet(Set<RajoiteDto> rajoitteet) {
        this.rajoitteet = rajoitteet;
    }
}
