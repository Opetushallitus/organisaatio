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
package fi.vm.sade.organisaatio.revised.ui.helper;

import static fi.vm.sade.generic.common.validation.ValidationConstants.EMAIL_PATTERN;
import static fi.vm.sade.generic.common.validation.ValidationConstants.WWW_PATTERN;

import com.vaadin.data.Validator;
import com.vaadin.ui.TextField;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.component.MultiLingualTextImpl;
import fi.vm.sade.generic.ui.validation.ErrorMessage;
import fi.vm.sade.organisaatio.api.model.types.EmailDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.PuhelinnumeroDTO;
import fi.vm.sade.organisaatio.api.model.types.WwwDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoArvoDTO;
import fi.vm.sade.organisaatio.revised.ui.component.organisaatioform.MuutOsoitteetComponent;
import fi.vm.sade.organisaatio.ui.component.OsoiteField;
import fi.vm.sade.organisaatio.ui.organisaatio.YhteystietojenTyyppiEditor;

/**
 * Validation of extra fields in OrganisaatioEditForm.
 * @author markus
 *
 */
public class ValidationHelper {
    
    private static final String PHONE_PATTERN = "(\\+|\\-| |\\(|\\)|[0-9]){3,100}";

    private static final int NIMI_MIN_LEN=3;
    private static final int NIMI_MAX_LEN=180; 

    private ErrorMessage errorView;
    private OsoiteField kayntiOsoite;
    private OsoiteField postiOsoite;
    private OsoiteField ruotsiKayntiOsoite;
    private OsoiteField ruotsiPostiOsoite;
    private YhteystietojenTyyppiEditor lisatiedotEditor;
    private MuutOsoitteetComponent otherAddressesLayout;
    private OsoiteField otKayntiOs;
    private OsoiteField otPostiOs;
    private MultiLingualTextImpl mlNimi;
    
    public ValidationHelper(ErrorMessage errorView, OsoiteField kayntiOsoite, OsoiteField postiOsoite, YhteystietojenTyyppiEditor lisatiedotEditor, 
    		MuutOsoitteetComponent otherAddressesLayout, OsoiteField otKayntiosoite, OsoiteField otPostiosoite, MultiLingualTextImpl mlNimi, OsoiteField ruotsiKayntiOsoite,
            OsoiteField ruotsiPostiOsoite) {
        this.errorView = errorView;
        this.kayntiOsoite = kayntiOsoite;
        this.postiOsoite = postiOsoite;
        this.lisatiedotEditor = lisatiedotEditor;
        this.otherAddressesLayout = otherAddressesLayout;
        this.otKayntiOs = otKayntiosoite;
        this.otPostiOs = otPostiosoite;
        this.mlNimi = mlNimi;
        this.ruotsiKayntiOsoite = ruotsiKayntiOsoite;
        this.ruotsiPostiOsoite = ruotsiPostiOsoite;
    }
    
    public boolean validateYtts(OrganisaatioDTO organisaatio) {
        
        boolean valid = true;
        for (YhteystietoArvoDTO curArvo : organisaatio.getYhteystietoArvos()) {
            String osio = this.lisatiedotEditor.getGroupByArvo(curArvo);
            if (osio != null && !osio.isEmpty()) {
                osio = String.format("%s%s", osio, ": ");
            } else {
                osio = "";
            }
            boolean tempValid = true;
            if (curArvo.getArvo() instanceof PuhelinnumeroDTO) {
                tempValid =validatePhone((PuhelinnumeroDTO)(curArvo.getArvo()), osio);
            } else if (curArvo.getArvo() instanceof EmailDTO) {
                tempValid = validateEmail((EmailDTO)(curArvo.getArvo()), osio);
            } else if (curArvo.getArvo() instanceof WwwDTO) {
                tempValid = validateWww((WwwDTO)(curArvo.getArvo()), osio);
            }
            if (!tempValid) {
                valid = false;
            }
        }
        return valid;
     }
     
     private boolean validateWww(WwwDTO wwwDTO, String osio) {
         if (wwwDTO.getWwwOsoite() != null && !(wwwDTO.getWwwOsoite().matches(WWW_PATTERN))) {
             errorView.addError(I18N.getMessage("OrganisaatioEditForm.validation.invalid.yttwww", osio));
             return false;
         }
         return true;
         
     }

     private boolean validateEmail(EmailDTO emailDTO, String osio) {
         if (emailDTO.getEmail() != null && !(emailDTO.getEmail().matches(EMAIL_PATTERN))) {
             errorView.addError(I18N.getMessage("OrganisaatioEditForm.validation.invalid.yttemail", osio));
             return false;
         }
         return true;
         
     }

     private boolean validatePhone(PuhelinnumeroDTO puhelinnumeroDTO, String osio) {
         if (puhelinnumeroDTO.getPuhelinnumero() != null && !(puhelinnumeroDTO.getPuhelinnumero().matches(PHONE_PATTERN))) {
             errorView.addError(I18N.getMessage("OrganisaatioEditForm.validation.invalid.yttphone", osio));
             return false;
         }   
         return true;
     }
    
    /**
     * Validates kayntiosoite, postiosoite and other addresses. Adds validation error messages to errorView.
     * @return whether validation was successful
     */
    public boolean validateExtraFields(OrganisaatioDTO organisaatio) {
        boolean isValid = true;
        if (isOsoiteNull(this.kayntiOsoite) && isOsoiteNull(this.ruotsiKayntiOsoite)) {
            isValid = false;
            this.errorView.addError(I18N.getMessage("validation.Organisaatio.kayntiosoiteNull"));
        } else if (this.isIncompleteOsoite(this.kayntiOsoite) || this.isIncompleteOsoite(this.ruotsiKayntiOsoite) ) {
            isValid = false;
            this.errorView.addError(I18N.getMessage("validation.Organisaatio.kayntiosoiteVirheellinen"));
        }
        if (isOsoiteNull(this.postiOsoite) && isOsoiteNull(this.ruotsiPostiOsoite)) {
            isValid = false;
            this.errorView.addError(I18N.getMessage("validation.Organisaatio.postiosoiteNull"));
        } else if (this.isIncompleteOsoite(this.postiOsoite) || this.isIncompleteOsoite(this.ruotsiPostiOsoite)) {
            isValid = false;
            this.errorView.addError(I18N.getMessage("validation.Organisaatio.postiosoiteVirheellinen"));
        }
        
        for (OsoiteField curOs : this.otherAddressesLayout.getOtherAddresses()) {
            if (!isEmptyOsoite(curOs) && osoiteContainsNulls(curOs)) {
                isValid = false;
                this.errorView.addError(I18N.getMessage("validation.Organisaatio.muuosoiteNull"));
                break;
            }
        }
        if (!isLisatiedotFieldsValid()) {
            isValid = false;
        }
        if (this.isIncompleteOsoite(this.otKayntiOs)) {
            isValid = false;
            this.errorView.addError(I18N.getMessage("validation.hakutoimisto.kayntiosoiteVirheellinen"));
        }
        if (this.isIncompleteOsoite(this.otPostiOs)) {
            isValid = false;
            this.errorView.addError(I18N.getMessage("validation.hakutoimisto.postiosoiteVirheellinen"));
        }
        
        //validoi nimi: joku kieli pitää olla non null ja kaikki arvot jotka eivät ole null pitää olla 3-180 merkkiä
        
        boolean fi = isValid(mlNimi.getTextFi());
        boolean sv = isValid(mlNimi.getTextSv());
        boolean en = isValid(mlNimi.getTextEn());
        
        if (!(fi && sv && en) || mlNimi.allAreNull()) {
            isValid = false;
            errorView.addError(I18N.getMessage(
                    "validation.Organisaatio.nimiFi",
                    NIMI_MIN_LEN,
                    NIMI_MAX_LEN));
        }       
        
        if (!this.validateYtts(organisaatio)) {
            isValid = false;
        }
        
        return isValid;

    }

    private boolean isValid(String s) {
        if (s == null || s.trim().length()==0) {
            return true;
        } else {
            int length = s.length();
            return length >= NIMI_MIN_LEN && length <= NIMI_MAX_LEN;
        }
    }

    private boolean isLisatiedotFieldsValid() {
        boolean isValid = true;
        for (com.vaadin.ui.Field curC : this.lisatiedotEditor.getFields()) { 
            //DEBUGSAWAY:log.debug("Cur component: " + curC);
            //Is text field required but empty
            String osio = this.lisatiedotEditor.getGroupByField(curC).isEmpty() ? this.lisatiedotEditor.getGroupByField(curC) : this.lisatiedotEditor.getGroupByField(curC) + ": ";
            if (curC instanceof TextField && ((TextField)curC).isRequired() && (((TextField)curC).getValue() == null || ((TextField)curC).getValue().toString().length()<1)) {
                //DEBUGSAWAY:log.debug("There is a text field with error");
                this.errorView.addError(I18N.getMessage("validation.Organisaatio.geneerinenNull", osio + ((TextField)curC).getCaption()));
                isValid = false;
            }
            //DEBUGSAWAY:log.debug("Cur component: " + curC);
            //Is text field filled unsufficiently
            if (curC instanceof TextField && ((TextField)curC).getValue() != null && ((TextField)curC).getValue().toString().length() > 0 &&  ((TextField)curC).getValue().toString().length() < 3) {
                //DEBUGSAWAY:log.debug("There is a text field with error");
                this.errorView.addError(I18N.getMessage("validation.Organisaatio.geneerinenTekstiVirhe", osio + ((TextField)curC).getCaption()));
                isValid = false;
            }
            //Is osoite field required but empty
            if (curC instanceof OsoiteField && ((OsoiteField)curC).isRequired() 
                        && isOsoiteNull((OsoiteField)curC)) { 
                //DEBUGSAWAY:log.debug("There is a required osoite field with error");
                this.errorView.addError(I18N.getMessage("validation.Organisaatio.geneerinenNull", osio + ((OsoiteField)curC).getCaption()));
                isValid = false;
            }
            //Is osoite field filled incompletely
            if (curC instanceof OsoiteField
                    && isIncompleteOsoite((OsoiteField)curC)) {
                //DEBUGSAWAY:log.debug("There is an osoite field with error");
                this.errorView.addError(I18N.getMessage("validation.Organisaatio.geneerinenVirhe", osio + ((OsoiteField)curC).getCaption()));
                isValid = false;    
            }
        }
        return isValid;
    }
    
    private boolean isIncompleteOsoite(OsoiteField osoite) {
        return osoiteContainsNulls(osoite) && isValueInOsoite(osoite);
    }
    
    private boolean isValueInOsoite(OsoiteField osoite) {
        return ((OsoiteField)osoite).getPostinumero().getValue() != null
                || (((OsoiteField)osoite).getPostitoimipaikka().getValue() != null 
                    && ((OsoiteField)osoite).getPostitoimipaikka().getValue().toString().length() > 0)
                || (((OsoiteField)osoite).getOsoite().getValue() != null
                    && ((OsoiteField)osoite).getOsoite().getValue().toString().length() > 0);
    }
    
    private boolean osoiteContainsNulls(OsoiteField osoite) {
        String val = (String)osoite.getUlkomaanOsoite().getValue();
         if (val != null && val.trim().length() > 0) {
             return (osoite.getValue() == null)
                     || (osoite.getOsoite().getValue() == null)
                     || (((String)osoite.getOsoite().getValue()).length() < 3);
         }   else {
         return (osoite.getValue() == null)
                 || (osoite.getOsoite().getValue() == null)
                 || (((String)osoite.getOsoite().getValue()).length() < 3)
                 || (osoite.getPostinumero().getValue() == null)
                 || (osoite.getPostitoimipaikka().getValue() == null)
                 || ((String)osoite.getPostitoimipaikka().getValue()).length() < 1;
         }
    }
    
    private boolean isOsoiteNull(OsoiteField osoite) {
        return osoite.getValue() == null
                || (isNullOrEmpty(osoite.getUlkomaanOsoite().getValue())
                        && isNullOrEmpty(osoite.getOsoite().getValue())
                        && isNullOrEmpty(osoite.getPostinumero().getValue())
                        && isNullOrEmpty(osoite.getPostitoimipaikka().getValue()));
    }
    
    private static boolean isNullOrEmpty(Object o) {
    	return o==null || String.valueOf(o).length()==0;
    }
    
    private boolean isEmptyOsoite(OsoiteField osoite) {
        return osoite.getValue()==null ||
        		(isNullOrEmpty(osoite.getOsoite().getValue())
				|| isNullOrEmpty(osoite.getPostinumero().getValue()) 
				|| isNullOrEmpty(osoite.getPostitoimipaikka().getValue())
				);
   }

}
