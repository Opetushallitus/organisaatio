package fi.vm.sade.rajapinnat.ytj.api;

public class YTJOsoiteDTO {
    
    private String katu;
    
    private String postinumero;
    private String toimipaikka;
    private String maa;
    private String maakoodi;
    private String lisatieto;
    private String coNimi;
    private int kieli;
    /**
     * @return the katu
     */
    public String getKatu() {
        return katu;
    }

    /**
     * @param katu the katu to set
     */
    public void setKatu(String katu) {
        this.katu = katu;
    }

    /**
     * @return the postinumero
     */
    public String getPostinumero() {
        return postinumero;
    }

    /**
     * @param postinumero the postinumero to set
     */
    public void setPostinumero(String postinumero) {
        this.postinumero = postinumero;
    }

    /**
     * @return the toimipaikka
     */
    public String getToimipaikka() {
        return toimipaikka;
    }

    /**
     * @param toimipaikka the toimipaikka to set
     */
    public void setToimipaikka(String toimipaikka) {
        this.toimipaikka = toimipaikka;
    }

    /**
     * @return the maa
     */
    public String getMaa() {
        return maa;
    }

    /**
     * @param maa the maa to set
     */
    public void setMaa(String maa) {
        this.maa = maa;
    }

    /**
     * @return the maakoodi
     */
    public String getMaakoodi() {
        return maakoodi;
    }

    /**
     * @param maakoodi the maakoodi to set
     */
    public void setMaakoodi(String maakoodi) {
        this.maakoodi = maakoodi;
    }

    /**
     * @return the lisatieto
     */
    public String getLisatieto() {
        return lisatieto;
    }

    /**
     * @param lisatieto the lisatieto to set
     */
    public void setLisatieto(String lisatieto) {
        this.lisatieto = lisatieto;
    }

    /**
     * @return the coNimi
     */
    public String getCoNimi() {
        return coNimi;
    }

    /**
     * @param coNimi the coNimi to set
     */
    public void setCoNimi(String coNimi) {
        this.coNimi = coNimi;
    }


    public int getKieli() {
        return kieli;
    }

    public void setKieli(int kieli) {
        this.kieli = kieli;
    }
}
