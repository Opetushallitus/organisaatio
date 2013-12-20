package fi.vm.sade.organisaatio.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import fi.vm.sade.organisaatio.api.model.types.EmailDTO;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinNumeroTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinnumeroDTO;
import fi.vm.sade.organisaatio.api.model.types.WwwDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
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
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

/**
 *
 * @author Tuomas Katva
 */
public class OrganisaatioDisplayHelper {

    public static String getClosest(Locale locale, OrganisaatioDTO org) {
        String lang = locale != null && locale.getLanguage() != null ? locale.getLanguage().toLowerCase() : "";
        if (org.getNimi() == null) {
        	return "";
        }
        
        for (Teksti curTeksti : org.getNimi().getTeksti()) {
        	if (curTeksti.getKieliKoodi().equals(lang)) {
        		return curTeksti.getValue();
        	}
        }
        
        return getAvailableName(org);
    }
    
    public static String getAvailableName(OrganisaatioDTO org) {
    	if (org.getNimi() == null) {
    		return "";
    	}
    	for (Teksti curTeksti : org.getNimi().getTeksti()) {
    		if (curTeksti.getValue() != null && !curTeksti.getValue().isEmpty()) {
    			return curTeksti.getValue();
    		}
    	}
         return "";
    }
    
    public static String getClosestBasic(Locale locale, OrganisaatioPerustieto org) {
    	
        final String lang = locale.getLanguage().toLowerCase();
        if(org.getNimi(lang)!=null) {
            return org.getNimi(lang);
        }
        return getAvailableNameBasic(org);
    }
    
    public static String getAvailableNameBasic(OrganisaatioPerustieto org) {
        for(String lang:new String[]{"fi","sv","en"}){
            if(org.getNimi(lang)!=null) {
                return org.getNimi(lang);
            }
        }
        return ""; //no name??
    }

    public static String getCaption(OrganisaatioDTO org, Locale locale) {
        String caption = "";

        if(org == null) {
            return "N/A";
        }

        List<String> orgTyypit = new ArrayList<String>();

        for (OrganisaatioTyyppi orgTyyppi : org.getTyypit()) {
            orgTyypit.add(orgTyyppi.value());
        }

        if (orgTyypit.contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value())) {
            caption = getClosest(locale, org) + " ( " + org.getYtunnus() + " )" + " " + OrganisaatioTyyppi.KOULUTUSTOIMIJA.value();
        } else if (orgTyypit.contains(OrganisaatioTyyppi.OPPILAITOS.value())) {
            caption = getClosest(locale, org) + " ( " + org.getOppilaitosKoodi() + " ) " + OrganisaatioTyyppi.OPPILAITOS.value();
        } else {
            caption = getClosest(locale, org) + " " + orgTyypit.get(0);
        }

        return caption;
    }

    public static OsoiteDTO getOsoiteByType(OrganisaatioDTO org, OsoiteTyyppi tyyppi) {
        OsoiteDTO foundOsoite = new OsoiteDTO();
        foundOsoite.setOsoiteTyyppi(tyyppi);

        if(org == null || org.getYhteystiedot() == null) {
            return null;
        }

        for (YhteystietoDTO yhteysTieto : org.getYhteystiedot()) {
            if (yhteysTieto instanceof OsoiteDTO) {

                if (((OsoiteDTO) yhteysTieto).getOsoiteTyyppi().equals(tyyppi)) {
                    foundOsoite = (OsoiteDTO) yhteysTieto;
                    break;
                }
            }
        }

        return foundOsoite;
    }

    public static List<YhteystietoDTO> getYhteystietoByType(Class type, OrganisaatioDTO org) {
        List<YhteystietoDTO> yhteysTieto = new ArrayList<YhteystietoDTO>();
        for (YhteystietoDTO tieto : org.getYhteystiedot()) {
            if (tieto.getClass().isAssignableFrom(type)) {
                yhteysTieto.add(tieto);
            }
        }
        return yhteysTieto;
    }

    public static PuhelinnumeroDTO getPuhelinNumeroByType(List<YhteystietoDTO> puhelins, PuhelinNumeroTyyppi tyyppi) {
        PuhelinnumeroDTO puhelinNro = null;
        for (YhteystietoDTO puhelin : puhelins) {
            if (puhelin instanceof PuhelinnumeroDTO) {
                if (((PuhelinnumeroDTO) puhelin).getTyyppi().equals(tyyppi)) {
                    puhelinNro = (PuhelinnumeroDTO) puhelin;
                    break;
                }
            }
        }
        return puhelinNro;
    }

    public static WwwDTO getOrganisaatioWww(OrganisaatioDTO org) {
        WwwDTO www = null;

        List<YhteystietoDTO> tiedot = getYhteystietoByType(WwwDTO.class, org);
        if (tiedot.size() > 0) {
            www = (WwwDTO) tiedot.get(0);
        }

        return www;
    }

    public static EmailDTO getOrganisaatioEmail(OrganisaatioDTO org) {
        EmailDTO email = null;

        List<YhteystietoDTO> tiedot = getYhteystietoByType(EmailDTO.class, org);
        if (tiedot.size() > 0) {
            email = (EmailDTO) tiedot.get(0);
        }

        return email;
    }

    public static List<YhteystietoElementtiDTO> getPuhelinnumeros(YhteystietojenTyyppiDTO model) {
        return getElementtienLisatietokenttas(model.getAllLisatietokenttas(), YhteystietoElementtiTyyppi.PUHELIN, YhteystietoElementtiTyyppi.FAKSI);
    }

    public static List<YhteystietoElementtiDTO> getLisatietokenttas(YhteystietojenTyyppiDTO model) {
        return getElementtienLisatietokenttas(model.getAllLisatietokenttas(), YhteystietoElementtiTyyppi.OSOITE, YhteystietoElementtiTyyppi.OSOITE_ULKOMAA);
    }

    public static YhteystietoElementtiDTO getNimiTieto(YhteystietojenTyyppiDTO model) {
        List<YhteystietoElementtiDTO> elementit = getElementtienLisatietokenttas(model.getAllLisatietokenttas(), YhteystietoElementtiTyyppi.NIMI);
        if (elementit.size() > 0) {
            return elementit.get(0);
        } else {
            return null;
        }
    }

    public static YhteystietoElementtiDTO getNimike(YhteystietojenTyyppiDTO model) {
        List<YhteystietoElementtiDTO> elementit = getElementtienLisatietokenttas(model.getAllLisatietokenttas(), YhteystietoElementtiTyyppi.NIMIKE);
        if (elementit.size() > 0) {
            return elementit.get(0);
        } else {
            return null;
        }
    }

    public static List<YhteystietoElementtiDTO> getSahkoinenYhteystietos(YhteystietojenTyyppiDTO model) {
        return getElementtienLisatietokenttas(model.getAllLisatietokenttas(), YhteystietoElementtiTyyppi.WWW, YhteystietoElementtiTyyppi.EMAIL);
    }

    public static List<YhteystietoDTO> getMuutYhteystiedot(OrganisaatioDTO organisaatio) {
        List<YhteystietoDTO> result = new ArrayList<YhteystietoDTO>(organisaatio.getMuutOsoitteet());
        remove(result, getOsoiteByType(organisaatio, OsoiteTyyppi.POSTI));
        remove(result, getOsoiteByType(organisaatio, OsoiteTyyppi.KAYNTI));
        remove(result, getPuhelinNumeroByType(organisaatio.getYhteystiedot(), PuhelinNumeroTyyppi.PUHELIN));
        remove(result, getPuhelinNumeroByType(organisaatio.getYhteystiedot(), PuhelinNumeroTyyppi.FAKSI));
        remove(result, getOrganisaatioEmail(organisaatio));
        remove(result, getOrganisaatioWww(organisaatio));
        return result;
    }

    private static void remove(List<YhteystietoDTO> list, YhteystietoDTO yhteystieto) {
        if (yhteystieto != null) {
            list.remove(yhteystieto);
        }
    }

    public static List<YhteystietoElementtiDTO> getElementtienLisatietokenttas(List<YhteystietoElementtiDTO> elementit, YhteystietoElementtiTyyppi... tyyppis) {
    	final Set<YhteystietoElementtiTyyppi> allowedTyypit = new HashSet<YhteystietoElementtiTyyppi>(Arrays.asList(tyyppis));
        final List<YhteystietoElementtiDTO> yhtElementit = new ArrayList<YhteystietoElementtiDTO>();
        for (YhteystietoElementtiDTO elem : elementit) {

            if (elem != null && allowedTyypit.contains(elem.getTyyppi())) {
                yhtElementit.add(elem);
            }
        }

        return yhtElementit;
    }

    public static List<String> getTyyppisStrForOrganisaatio(OrganisaatioDTO org) {
        List<String> tyyppisStr = new ArrayList<String>();
        for (OrganisaatioTyyppi ot : org.getTyypit()) {
            tyyppisStr.add(ot.value());
        }
        return tyyppisStr;
    }

    public static String formatOsoiteAsString(OsoiteDTO osoite) {
        return new OsoiteCaptionBuilder(osoite).build();
    }

    // TODO: what is the correct order or fields and format of such caption
    private static class OsoiteCaptionBuilder {

        private OsoiteDTO osoite;
        private StringBuilder sb;

        public OsoiteCaptionBuilder(OsoiteDTO osoite) {
            this.osoite = osoite;
            this.sb = new StringBuilder();
        }

        public String build() {

            sb.setLength(0);
            append(osoite.getOsoite()).
                    append(osoite.getPostinumero()).
                    append(osoite.getPostitoimipaikka()).
                    append(osoite.getOsavaltio()).
                    append(osoite.getMaa());

            return sb.toString();

        }

        private OsoiteCaptionBuilder append(String value) {
            if (value == null || "".equals(value.trim())) {
                return this;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(value);
            return this;
        }
    }
    
    public static String getYttCaption(Locale locale, YhteystietojenTyyppiDTO ytt) {
    	 String lang = locale.getLanguage().toLowerCase();
    	 if (ytt.getNimi() == null) {
    		 return "";
    	 }
    	 
    	 for (Teksti curTeksti : ytt.getNimi().getTeksti()) {
    		 if (curTeksti.getKieliKoodi().equals(lang)) {//getNimiFi() != null) {
    		         	
    	             return curTeksti.getValue();
    	     }
    	 }
         return getAvailableYttName(ytt);
    }
	
    
    private static String getAvailableYttName(YhteystietojenTyyppiDTO ytt) {
    	if (ytt.getNimi() == null) {
    		return "";
    	}
    	 for (Teksti curTeksti : ytt.getNimi().getTeksti()) {
    		 if (curTeksti.getValue() != null && !curTeksti.getValue().isEmpty()) {
    			 return curTeksti.getValue();
    		 }
    	 }
         return "";
    }
}
