package fi.vm.sade.tarjonta.service.resources.v1.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HakukohdeHakutulosV1RDTO extends KoulutusHakutulosV1RDTO {

    private static final long serialVersionUID = 1L;

    private Map<String, String> hakutapa;
    private Map<String, String> aloituspaikatKuvaukset;
    private String hakuaikaString;
    private String hakuaikaRyhma;
    private Integer aloituspaikat;
    private Integer valintojenAloituspaikat;
    private String hakuOid;
    private String koodistoNimi;
    private List<RyhmaliitosV1RDTO> ryhmaliitokset = new ArrayList<RyhmaliitosV1RDTO>();
    private Integer ensikertalaistenAloituspaikat;

    public String getKoodistoNimi() {
        return koodistoNimi;
    }

    public void setKoodistoNimi(String koodistoNimi) {
        this.koodistoNimi = koodistoNimi;
    }

    public Map<String, String> getHakutapa() {
        return hakutapa;
    }

    public void setHakutapa(Map<String, String> hakutapa) {
        this.hakutapa = hakutapa;
    }

    public Integer getAloituspaikat() {
        return aloituspaikat;
    }

    public void setAloituspaikat(Integer aloituspaikat) {
        this.aloituspaikat = aloituspaikat;
    }

    public String getHakuOid() {
        return hakuOid;
    }

    public void setHakuOid(String hakuOid) {
        this.hakuOid = hakuOid;
    }

    public Map<String, String> getAloituspaikatKuvaukset() {
        return aloituspaikatKuvaukset;
    }

    public void setAloituspaikatKuvaukset(Map<String, String> aloituspaikatKuvaukset) {
        this.aloituspaikatKuvaukset = aloituspaikatKuvaukset;
    }

    public List<RyhmaliitosV1RDTO> getRyhmaliitokset() {
        return ryhmaliitokset;
    }

    public void setRyhmaliitokset(List<RyhmaliitosV1RDTO> ryhmaliitokset) {
        this.ryhmaliitokset = ryhmaliitokset;
    }

    public String getHakuaikaRyhma() {
        return hakuaikaRyhma;
    }

    public void setHakuaikaRyhma(String hakuaikaRyhma) {
        this.hakuaikaRyhma = hakuaikaRyhma;
    }

    public String getHakuaikaString() {
        return hakuaikaString;
    }

    public void setHakuaikaString(String hakuaikaString) {
        this.hakuaikaString = hakuaikaString;
    }

    public Integer getEnsikertalaistenAloituspaikat() {
        return ensikertalaistenAloituspaikat;
    }

    public void setEnsikertalaistenAloituspaikat(Integer ensikertalaistenAloituspaikat) {
        this.ensikertalaistenAloituspaikat = ensikertalaistenAloituspaikat;
    }

    public Integer getValintojenAloituspaikat() {
        return valintojenAloituspaikat;
    }

    public void setValintojenAloituspaikat(Integer valintojenAloituspaikat) {
        this.valintojenAloituspaikat = valintojenAloituspaikat;
    }
}