package fi.vm.sade.rekisterointi.model;

public class YhteystietoDto extends BaseDto {

    public String numero;
    public String email; // ei osoiteTyyppiä eikä tyyppiä?
    public String osoiteTyyppi; // käynti- ja postiosoitteilla
    public String tyyppi; // puhelinnumerolla "puhelin"
    public String osoite;
    public String postinumeroUri;
    public String postitoimipaikka;
    public String kieli;

}
