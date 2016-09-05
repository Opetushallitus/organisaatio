package fi.vm.sade.organisaatio.helper;

import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

import java.util.Locale;

public class OrganisaatioDisplayHelper {
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
}
