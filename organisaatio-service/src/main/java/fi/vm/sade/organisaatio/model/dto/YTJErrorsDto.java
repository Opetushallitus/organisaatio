package fi.vm.sade.organisaatio.model.dto;

public class YTJErrorsDto {
    public boolean organisaatioValid;
    public boolean nimiValid;
    public boolean nimisvValid;
    public boolean osoiteValid;
    public boolean wwwValid;
    public boolean puhelinnumeroValid;

    public YTJErrorsDto() {
        organisaatioValid = true;
        nimiValid = true;
        nimisvValid = true;
        osoiteValid = true;
        wwwValid = true;
        puhelinnumeroValid = true;
    }
}
