package fi.vm.sade.organisaatio.business;

import fi.vm.sade.organisaatio.model.Organisaatio;

import java.util.List;

public interface OrganisaatioYtjService {

    /**
    * Päivittää datan YTJ:stä koulutustoimijoille, työelämäjärjestöille ja muu organisaatioille
    */
    public List<Organisaatio> updateYTJData(final boolean forceUpdate);

}
