package fi.vm.sade.organisaatio.resource.dto;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Map;

public class HakutoimistoDTO {

    public final Map<String, String> nimi;
    public final Map<String, HakutoimistonYhteystiedotDTO> yhteystiedot;

    public HakutoimistoDTO(Map<String, String> nimi, Map<String, HakutoimistonYhteystiedotDTO> yhteystiedot) {
        this.nimi = nimi;
        this.yhteystiedot = yhteystiedot;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 23).append(nimi).append(yhteystiedot).toHashCode();
    }

    public static class OsoiteDTO {

        public final String yhteystietoOid;
        public final String katuosoite;
        public final String postinumero;
        public final String postitoimipaikka;

        public OsoiteDTO(String yhteystietoOid, String katuosoite, String postinumero, String postitoimipaikka) {
            this.yhteystietoOid = yhteystietoOid;
            this.katuosoite = katuosoite;
            this.postinumero = postinumero;
            this.postitoimipaikka = postitoimipaikka;
        }

        @Override
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        public String toString() {
            return ReflectionToStringBuilder.toString(this);
        }

    }

    public static class HakutoimistonYhteystiedotDTO {

        public final OsoiteDTO kaynti;
        public final OsoiteDTO posti;
        public final String www;
        public final String email;
        public final String puhelin;

        public HakutoimistonYhteystiedotDTO(OsoiteDTO kaynti, OsoiteDTO posti, String www, String email, String puhelin) {
            this.kaynti = kaynti;
            this.posti = posti;
            this.www = www;
            this.email = email;
            this.puhelin = puhelin;
        }

        @Override
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("kaynti", kaynti)
                    .append("posti", posti)
                    .append("www", www)
                    .append("email", email)
                    .toString();
        }
    }
}
