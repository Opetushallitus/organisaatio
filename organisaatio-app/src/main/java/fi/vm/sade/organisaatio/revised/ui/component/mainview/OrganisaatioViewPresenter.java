package fi.vm.sade.organisaatio.revised.ui.component.mainview;
import java.util.Date;
import java.util.List;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioPerustietoType;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.revised.ui.component.mainview.OrganisaatioModelWrapper.ImportState;
import fi.vm.sade.organisaatio.ui.listener.event.YtjSelectedEventImpl;

/**
 *
 * @author Tuomas Katva
 */
interface OrganisaatioViewPresenter {
    
    void loadOrganisaatioWithOid(OrganisaatioModelWrapper orgm);
    
    void setOrganisaatioView(OrganisaatioMainView organisaatioView);
    
    void setOrganisaatioMainView(OrganisaatioMainView organisaatioMainView);
    
    OrganisaatioMainView getOrganisaatioMainView();
    
    //void deleteOrganization();
    
    void deleteOrganizationConfirmed();
    
    OrganisaatioDTO getSelectedOrganisaatio();
    
    void refreshOrganization();
    
    String formatOsoite(OsoiteDTO osoite);
        
    String formatImportInfo(Date date, ImportState source);

    OrganisaatioDTO findFullOrganisaatio(String oid);

    void processYtjSelectEvent(YtjSelectedEventImpl ytjEvent);
    
    List<OrganisaatioPerustieto> fetchChildOrganisaatios(String oid); 
    
}
