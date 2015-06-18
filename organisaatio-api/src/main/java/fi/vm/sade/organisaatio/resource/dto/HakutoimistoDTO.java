package fi.vm.sade.organisaatio.resource.dto;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class HakutoimistoDTO {
    private final String yhteystietoOid;
    private final String katuosoite;
    private final String postinumero;
    private final String postitoimipaikka;

    public HakutoimistoDTO(String yhteystietoOid, String katuosoite, String postinumero, String postitoimipaikka) {
        this.yhteystietoOid = yhteystietoOid;
        this.katuosoite = katuosoite;
        this.postinumero = postinumero;
        this.postitoimipaikka = postitoimipaikka;

    }

    public String getYhteystietoOid() {
        return yhteystietoOid;
    }

    public String getKatuosoite() {
        return katuosoite;
    }

    public String getPostinumero() {
        return postinumero;
    }

    public String getPostitoimipaikka() {
        return postitoimipaikka;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HakutoimistoDTO that = (HakutoimistoDTO) o;

        if (yhteystietoOid != null ? !yhteystietoOid.equals(that.yhteystietoOid) : that.yhteystietoOid != null)
            return false;
        if (katuosoite != null ? !katuosoite.equals(that.katuosoite) : that.katuosoite != null) return false;
        if (postinumero != null ? !postinumero.equals(that.postinumero) : that.postinumero != null) return false;
        return !(postitoimipaikka != null ? !postitoimipaikka.equals(that.postitoimipaikka) : that.postitoimipaikka != null);

    }

    @Override
    public int hashCode() {
        int result = yhteystietoOid != null ? yhteystietoOid.hashCode() : 0;
        result = 31 * result + (katuosoite != null ? katuosoite.hashCode() : 0);
        result = 31 * result + (postinumero != null ? postinumero.hashCode() : 0);
        result = 31 * result + (postitoimipaikka != null ? postitoimipaikka.hashCode() : 0);
        return result;
    }
}
