package fi.vm.sade.organisaatio.dto;

public class Koodi {

    private String arvo;
    private String uri;
    private int versio;

    public Koodi() {
    }

    public Koodi(String arvo, String uri, int versio) {
        this.arvo = arvo;
        this.uri = uri;
        this.versio = versio;
    }

    public String getArvo() {
        return arvo;
    }

    public void setArvo(String arvo) {
        this.arvo = arvo;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getVersio() {
        return versio;
    }

    public void setVersio(int versio) {
        this.versio = versio;
    }

}
