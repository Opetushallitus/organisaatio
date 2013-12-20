
package fi.vm.sade.organisaatio.revised.ui.component.mainview;

import java.util.List;

import com.vaadin.ui.Component;

/**
 *
 * @author Tuomas Katva
 */
public interface OrganisaatioMainView {
    
	Component getComponent();
    
    void bindOrganisaatioTiedot(OrganisaatioModelWrapper orgm);
    
    void clearView();
    
    //void showDeleteConfirmation();
    
    void showYtjDialog();
    
    void closeYtjDialog();
    
    void togglePaivitaYtjButtonVisibility(boolean visible);
    
    void setKotipaikka(String kotipaikka);
    
    void closeDialog(); 
    
    void removeDialog();
    
    void setOppilaitosTyyppiValue(String value);
    
    void setMaaValue(String value);
    
    void setKieliValues(List<String> value);

    void setVuosiluokatValues(List<String> value);
    
    void setErrorMessage(String errorMessage);

}
