package fi.vm.sade.organisaatio.dto;

public class OrganisaatioNimiUpdateDTO {

    private OrganisaatioNimiDTO currentNimi;
    private OrganisaatioNimiDTO updatedNimi;

    public OrganisaatioNimiDTO getCurrentNimi() {
        return currentNimi;
    }

    public void setCurrentNimi(OrganisaatioNimiDTO currentNimi) {
        this.currentNimi = currentNimi;
    }

    public OrganisaatioNimiDTO getUpdatedNimi() {
        return updatedNimi;
    }

    public void setUpdatedNimi(OrganisaatioNimiDTO updatedNimi) {
        this.updatedNimi = updatedNimi;
    }
}
