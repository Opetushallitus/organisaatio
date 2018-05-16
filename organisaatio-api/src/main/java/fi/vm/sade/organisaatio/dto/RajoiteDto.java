package fi.vm.sade.organisaatio.dto;

public class RajoiteDto {
    private Rajoitetyyppi rajoitetyyppi;
    private String arvo;

    public Rajoitetyyppi getRajoitetyyppi() {
        return rajoitetyyppi;
    }

    public void setRajoitetyyppi(Rajoitetyyppi rajoitetyyppi) {
        this.rajoitetyyppi = rajoitetyyppi;
    }

    public String getArvo() {
        return arvo;
    }

    public void setArvo(String arvo) {
        this.arvo = arvo;
    }
}
