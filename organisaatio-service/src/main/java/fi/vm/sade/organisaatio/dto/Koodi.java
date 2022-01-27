package fi.vm.sade.organisaatio.dto;

import fi.vm.sade.koodisto.service.types.common.TilaType;

import java.util.Map;

public class Koodi {

    private String arvo;
    private String uri;
    private int versio;
    public Map<String, String> nimi;
    private TilaType tila;

    public Koodi() {
    }

    public Koodi(String arvo, String uri, int versio) {
        this.arvo = arvo;
        this.uri = uri;
        this.versio = versio;
    }
    public Koodi(String arvo, String uri, int versio, Map<String, String> nimi) {
        this.arvo = arvo;
        this.uri = uri;
        this.versio = versio;
        this.nimi = nimi;
    }
    public Koodi(String arvo, String uri, int versio, Map<String, String> nimi, TilaType tila) {
        this.arvo = arvo;
        this.uri = uri;
        this.versio = versio;
        this.nimi = nimi;
        this.tila = tila;
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

    public Map<String, String> getNimi() {
        return nimi;
    }

    public void setNimi(Map<String, String> nimi) {
        this.nimi = nimi;
    }

    public TilaType getTila() {
        return tila;
    }

    public void setTila(TilaType tila) {
        this.tila = tila;
    }
}
