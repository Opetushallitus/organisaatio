package fi.vm.sade.organisaatio.revised.ui.component.mainview;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.blackboard.BlackboardContext;
import fi.vm.sade.koodisto.service.types.SearchKoodisCriteriaType;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.koodisto.util.CachingKoodistoClient;
import fi.vm.sade.koodisto.util.KoodiServiceSearchCriteriaBuilder;
import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.*;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.revised.ui.component.mainview.OrganisaatioModelWrapper.ImportState;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioViewButtonEvent;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoHelper;
import fi.vm.sade.organisaatio.revised.ui.helper.YtjToOrganisaatioMapper;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;
import fi.vm.sade.organisaatio.ui.listener.event.YtjSelectedEventImpl;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import fi.vm.sade.rajapinnat.ytj.api.YTJKieli;
import fi.vm.sade.rajapinnat.ytj.api.YTJService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */
/**
 *
 * @author Tuomas Katva
 */
@Configurable(preConstruction = false) 
class OrganisaatioViewPresenterImpl implements OrganisaatioViewPresenter {

    private OrganisaatioMainView organisaatioMainView;
    private OrganisaatioDTO selectedOrganisaatio;
    @Autowired(required = true)
    private OrganisaatioService organisaatioService;
    @Autowired
    private YTJService ytjService;
    @Autowired
    private OrganisaatioSearchService organisaatioSearchService;
    
    private CachingKoodistoClient koodistoRestClient = new CachingKoodistoClient();
    
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());
    
    //private Map<String, List<OrganisaatioPerustietoType>> parentOrgansaatioMap = new HashMap<String, List<OrganisaatioPerustietoType>>();

    @Override
    public void setOrganisaatioView(OrganisaatioMainView organisaatioView) {
        setOrganisaatioMainView(organisaatioView);

    }

    @Override
    public void loadOrganisaatioWithOid(OrganisaatioModelWrapper orgm) {
    	orgm.load(organisaatioService);
    	selectedOrganisaatio = orgm.get();
        KoodistoHelper helper = new KoodistoHelper();
        
        getOrganisaatioMainView().bindOrganisaatioTiedot(orgm);

        getOrganisaatioMainView().setKieliValues(getKoodiValues(selectedOrganisaatio.getKielet(), helper));
        getOrganisaatioMainView().setVuosiluokatValues(getKoodiValues(selectedOrganisaatio.getVuosiluokat(), helper));
        getOrganisaatioMainView().setMaaValue(helper.tryGetArvoByKoodi(selectedOrganisaatio.getMaa()));
        getOrganisaatioMainView().setOppilaitosTyyppiValue(selectedOrganisaatio.getOppilaitosTyyppi() != null ? helper.tryGetArvoByKoodi(selectedOrganisaatio.getOppilaitosTyyppi()) : "");

        if (selectedOrganisaatio.getKotipaikka() != null) {
            //Try catch so that it wont break when testing
            //TODO check why this don't work... Why koodisto won't return koodi for kunta
            try {
                getOrganisaatioMainView().setKotipaikka(helper.tryGetArvoByKoodi(selectedOrganisaatio.getKotipaikka()));
            } catch (Exception e) {
            	log.debug("Kotipaikka fail", e);
            }

        }
        
        organisaatioMainView.togglePaivitaYtjButtonVisibility(isYtjButtonVisible());
    }

    private boolean isYtjButtonVisible()  {
    	return selectedOrganisaatio.getYtjPaivitysPvm() == null
    			&& selectedOrganisaatio.getTyypit().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA);
    }

    private List<String> getKoodiValues(List<String> koodis, KoodistoHelper helper) {
        List<String> values = new ArrayList<String>();
        for (String koodi : koodis) {
            values.add(helper.tryGetArvoByKoodi(koodi));
        }
        return values;
    }

    @Override
    public void refreshOrganization() {
        organisaatioMainView.showYtjDialog();
    }

    @Override
    public String formatImportInfo(Date date, ImportState source) {
    	if (date==null) {
    		return "";
    	}
        SimpleDateFormat sdf = new SimpleDateFormat(I18N.getMessage("OrganisaatioMainView.dateFormat"));
    	StringBuilder ret = new StringBuilder(" (");
    	
    	switch (source) {
    	case YTJ:
    		ret.append(I18N.getMessage("OrganisaatioMainView.haettuYtjLabel"));
    		break;
    	case KOULUTA:
    		ret.append(I18N.getMessage("OrganisaatioMainView.haettuKoulutaLabel"));
    		break;
		default:
	    	return "";
    	}
    	
    	ret.append(' ')
    		.append(sdf.format(date))
    		.append(")");
    	return ret.toString();
    }

    @Override
    public String formatOsoite(OsoiteDTO osoite) {
        String postinumero = null;
        String osoiteMaa = null;
        if (osoite.getMaa() != null && !osoite.getMaa().isEmpty()) {
            osoiteMaa = new KoodistoHelper().tryGetArvoByKoodi(osoite.getMaa());
        }
        //Try to get postinumerokoodi "arvo"
        if (osoite.getPostinumero() != null && !osoite.getPostinumero().isEmpty()) {
        	postinumero = osoite.getPostinumero(); // vakioarvo jos haku epäonnistuu
            try {

                SearchKoodisCriteriaType searchCriteria = KoodiServiceSearchCriteriaBuilder.latestKoodisByUris(osoite.getPostinumero());//latestValidAcceptedKoodiByUri(osoite.getPostinumero());
                List<KoodiType> result = koodistoRestClient.searchKoodis(searchCriteria);
                
                if (result.size() == 1) {
                    postinumero = result.get(0).getKoodiArvo();
                } else {
                	log.warn("No koodi was found for postinumero URI {} (result = {})", osoite.getPostinumero(), result);
                }
            } catch (Exception e) {
            	log.debug("Failed to get postinumero from koodisto",e);
            }
        }
        
        
        List<String> os = new ArrayList<String>();
        
        //StringBuilder os = new StringBuilder();
        if (!Strings.isNullOrEmpty(osoite.getOsoite())) {
        	//os.append(osoite.getOsoite());
        	os.add(osoite.getOsoite());
        }
        if (postinumero!=null) {
        	os.add(postinumero);
        }
        
        if (osoite.getPostitoimipaikka()!=null) {
        	os.add(osoite.getPostitoimipaikka());
        }

        if (osoiteMaa!=null) {
        	os.add(osoiteMaa);
        }

        return Joiner.on(", ").join(os) + formatImportInfo(osoite.getYtjPaivitysPvm(), ImportState.YTJ);
    }

    @Override
    public void processYtjSelectEvent(YtjSelectedEventImpl ytjEvent) {
        try {
            YTJDTO value = ytjEvent.getYtjDto();
            if (ytjEvent.isCancelled() || value == null) {
                organisaatioMainView.closeYtjDialog();
            }
            if (value != null) {
                YTJDTO ytj = ytjService.findByYTunnus(value.getYtunnus(), YTJKieli.FI);
                String oldOid  = null;
                if (ytjEvent.getOldOrgOid() != null) {
                    oldOid = ytjEvent.getOldOrgOid();
                }
                if (oldOid == null) {
                    oldOid = selectedOrganisaatio.getOid();
                }
                selectedOrganisaatio = YtjToOrganisaatioMapper.mapYtjToOrganisaatio(ytj, selectedOrganisaatio);
                selectedOrganisaatio.setOid(oldOid);
                organisaatioMainView.closeYtjDialog();
                fireYtjUpdate();
            }

        } catch (Exception exp) {
              log.error("Exception connection to YTJ-service : " + exp.toString());

//                    getWindow().showNotification("Exception in YTJ-service", Window.Notification.TYPE_TRAY_NOTIFICATION);
        }
    }

    private void fireYtjUpdate() {

        BlackboardContext.getBlackboard().fire(new OrganisaatioViewButtonEvent(selectedOrganisaatio, OrganisaatioViewButtonEvent.MUOKKAA_YLEISTIEDOT));
    }

    /*@Override
    public void deleteOrganization() {
        organisaatioMainView.showDeleteConfirmation();
    }*/

    @Override
    public void deleteOrganizationConfirmed() {
        try {
            RemoveByOidType removeParam = new RemoveByOidType();
            removeParam.setOid(selectedOrganisaatio.getOid().trim());
            organisaatioService.removeOrganisaatioByOid(removeParam);
            organisaatioMainView.clearView();
            organisaatioMainView.removeDialog();
        } catch (Throwable ex) {
            Logger.getLogger(OrganisaatioViewPresenterImpl.class.getName()).log(Level.SEVERE, null, ex);
            organisaatioMainView.closeDialog();
            if (ex instanceof GenericFault) {
                organisaatioMainView.setErrorMessage(I18N.getMessage(((GenericFault) ex).getFaultInfo().getErrorCode()));
            } else {
                organisaatioMainView.setErrorMessage(I18N.getMessage(ex.getMessage()));
            }

        }
    }

    /**
     * @return the organisaatioMainView
     */
    @Override
    public OrganisaatioMainView getOrganisaatioMainView() {
        return organisaatioMainView;
    }

    /**
     * @param organisaatioMainView the organisaatioMainView to set
     */
    @Override
    public void setOrganisaatioMainView(OrganisaatioMainView organisaatioMainView) {
        this.organisaatioMainView = organisaatioMainView;
    }

    /**
     * @return the selectedOrganisaatio
     */
    @Override
    public OrganisaatioDTO getSelectedOrganisaatio() {
        return selectedOrganisaatio;
    }

    /**
     * @param selectedOrganisaatio the selectedOrganisaatio to set
     */
    public void setSelectedOrganisaatio(OrganisaatioDTO selectedOrganisaatio) {
        this.selectedOrganisaatio = selectedOrganisaatio;
    }

    @Override
    public OrganisaatioDTO findFullOrganisaatio(String oid) {
        return this.organisaatioService.findByOid(oid);
    }
    
    public List<OrganisaatioPerustieto> fetchChildOrganisaatios(String oid) {
        OrganisaatioSearchCriteria criteria = new OrganisaatioSearchCriteria();
        criteria.getOidRestrictionList().add(oid);

        List<OrganisaatioPerustieto> childOrganisaatios = organisaatioSearchService.searchBasicOrganisaatios(criteria);
        return childOrganisaatios;
    }
}
