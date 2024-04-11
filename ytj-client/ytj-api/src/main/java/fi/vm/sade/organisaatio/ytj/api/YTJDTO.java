package fi.vm.sade.organisaatio.ytj.api;

import fi.ytj.ArrayOfYritysTunnusHistoriaDTO;
import fi.ytj.YTunnusDTO;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "YTJ")
@XmlAccessorType(XmlAccessType.FIELD)
public class YTJDTO {

    @XmlElement(required = true)
    private Integer versio = 1;

    private String nimi;
    private String svNimi;
    private String ytunnus;
    private YTunnusDTO yritysTunnus;
    private ArrayOfYritysTunnusHistoriaDTO yritystunnusHistoria;
    private String yritysmuoto;
    private String yritysmuotoKoodi;
    private String toimiala;
    private String toimialaKoodi;
    private String yrityksenKieli;
    private YTJOsoiteDTO postiOsoite;
    private YTJOsoiteDTO kayntiOsoite;
    private String sahkoposti;
    private String www;
    private String puhelin;
    private String faksi;
    private String kotiPaikka;
    private String kotiPaikkaKoodi;
    private String aloitusPvm;

    public Integer getVersio() {
        return versio;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public String getYtunnus() {
        return ytunnus;
    }

    public void setYtunnus(String ytunnus) {
        this.ytunnus = ytunnus;
    }

    public YTunnusDTO getYritysTunnus() {
        return yritysTunnus;
    }

    public void setYritysTunnus(YTunnusDTO yritysTunnus) {
        this.yritysTunnus = yritysTunnus;
    }

    public ArrayOfYritysTunnusHistoriaDTO getYritystunnusHistoria() {
        return yritystunnusHistoria;
    }

    public void setYritystunnusHistoria(ArrayOfYritysTunnusHistoriaDTO yritystunnusHistoria) {
        this.yritystunnusHistoria = yritystunnusHistoria;
    }

    /**
     * @return the yritysmuoto
     */
    public String getYritysmuoto() {
        return yritysmuoto;
    }

    /**
     * @param yritysmuoto the yritysmuoto to set
     */
    public void setYritysmuoto(String yritysmuoto) {
        this.yritysmuoto = yritysmuoto;
    }

    /**
     * @return the yrityksenKieli
     */
    public String getYrityksenKieli() {
        return yrityksenKieli;
    }

    /**
     * @param yrityksenKieli the yrityksenKieli to set
     */
    public void setYrityksenKieli(String yrityksenKieli) {
        this.yrityksenKieli = yrityksenKieli;
    }

    /**
     * @return the postiOsoite
     */
    public YTJOsoiteDTO getPostiOsoite() {
        return postiOsoite;
    }

    /**
     * @param postiOsoite the postiOsoite to set
     */
    public void setPostiOsoite(YTJOsoiteDTO postiOsoite) {
        this.postiOsoite = postiOsoite;
    }

    /**
     * @return the kayntiOsoite
     */
    public YTJOsoiteDTO getKayntiOsoite() {
        return kayntiOsoite;
    }

    /**
     * @param kayntiOsoite the kayntiOsoite to set
     */
    public void setKayntiOsoite(YTJOsoiteDTO kayntiOsoite) {
        this.kayntiOsoite = kayntiOsoite;
    }

    /**
     * @return the sahkoposti
     */
    public String getSahkoposti() {
        return sahkoposti;
    }

    /**
     * @param sahkoposti the sahkoposti to set
     */
    public void setSahkoposti(String sahkoposti) {
        this.sahkoposti = sahkoposti;
    }

    /**
     * @return the www
     */
    public String getWww() {
        return www;
    }

    /**
     * @param www the www to set
     */
    public void setWww(String www) {
        this.www = www;
    }

    /**
     * @return the puhelin
     */
    public String getPuhelin() {
        return puhelin;
    }

    /**
     * @param puhelin the puhelin to set
     */
    public void setPuhelin(String puhelin) {
        this.puhelin = puhelin;
    }

    /**
     * @return the faksi
     */
    public String getFaksi() {
        return faksi;
    }

    /**
     * @param faksi the faksi to set
     */
    public void setFaksi(String faksi) {
        this.faksi = faksi;
    }

    /**
     * @return the toimiala
     */
    public String getToimiala() {
        return toimiala;
    }

    /**
     * @param toimiala the toimiala to set
     */
    public void setToimiala(String toimiala) {
        this.toimiala = toimiala;
    }

    /**
     * @return the toimialaKoodi
     */
    public String getToimialaKoodi() {
        return toimialaKoodi;
    }

    /**
     * @param toimialaKoodi the toimialaKoodi to set
     */
    public void setToimialaKoodi(String toimialaKoodi) {
        this.toimialaKoodi = toimialaKoodi;
    }

    /**
     * @return the yritysmuotoKoodi
     */
    public String getYritysmuotoKoodi() {
        return yritysmuotoKoodi;
    }

    /**
     * @param yritysmuotoKoodi the yritysmuotoKoodi to set
     */
    public void setYritysmuotoKoodi(String yritysmuotoKoodi) {
        this.yritysmuotoKoodi = yritysmuotoKoodi;
    }

    /**
     * @return the kotiPaikka
     */
    public String getKotiPaikka() {
        return kotiPaikka;
    }

    /**
     * @param kotiPaikka the kotiPaikka to set
     */
    public void setKotiPaikka(String kotiPaikka) {
        this.kotiPaikka = kotiPaikka;
    }

    public String getSvNimi() {
        return svNimi;
    }

    public void setSvNimi(String svNimi) {
        this.svNimi = svNimi;
    }

    public String getKotiPaikkaKoodi() {
        return kotiPaikkaKoodi;
    }

    public void setKotiPaikkaKoodi(String kotiPaikkaKoodi) {
        this.kotiPaikkaKoodi = kotiPaikkaKoodi;
    }

    public String getAloitusPvm() {
        return aloitusPvm;
    }

    public void setAloitusPvm(String aloitusPvm) {
        this.aloitusPvm = aloitusPvm;
    }
}
