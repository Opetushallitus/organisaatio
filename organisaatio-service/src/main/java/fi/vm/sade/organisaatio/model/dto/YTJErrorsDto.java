package fi.vm.sade.organisaatio.model.dto;

public class YTJErrorsDto {
    public boolean organisaatio;
    public boolean nimi;
    public boolean nimisv;
    public boolean osoite;
    public boolean www;
    public boolean puhelinnumero;

    public YTJErrorsDto() {
        organisaatio = true;
        nimi = true;
        nimisv = true;
        osoite = true;
        www = true;
        puhelinnumero = true;
    }
}
