package fi.vm.sade.varda.rekisterointi.model;

public class YhteystietoDto extends BaseDto {

    public static final String OSOITETYYPPI_POSTI = "posti";
    public static final String OSOITETYYPPI_KAYNTI = "kaynti";

    public String numero;
    public String email;
    public String osoiteTyyppi;
    public String osoite;
    public String postinumeroUri;
    public String postitoimipaikka;
    public String kieli;

}
