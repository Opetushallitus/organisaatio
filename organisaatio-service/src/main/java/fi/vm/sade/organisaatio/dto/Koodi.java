package fi.vm.sade.organisaatio.dto;

import java.util.Date;
import java.util.Map;

import fi.vm.sade.organisaatio.model.KoodiType.TilaType;

public class Koodi {

    private String arvo;
    private String uri;
    private int versio;
    public Map<String, String> nimi;
    private TilaType tila;
    private Date voimassaAlkuPvm;
    private Date voimassaLoppuPvm;

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

    public void setTila(TilaType tila) {
        this.tila = tila;
    }

    public void setVoimassaAlkuPvm(Date voimassaAlkuPvm) {
        this.voimassaAlkuPvm = voimassaAlkuPvm;
    }

    public void setVoimassaLoppuPvm(Date voimassaLoppuPvm) {
        this.voimassaLoppuPvm = voimassaLoppuPvm;
    }

    public TilaType getTila() {
        return tila;
    }

    public Date getVoimassaAlkuPvm() {
        return voimassaAlkuPvm;
    }

    public Date getVoimassaLoppuPvm() {
        return voimassaLoppuPvm;
    }
}
