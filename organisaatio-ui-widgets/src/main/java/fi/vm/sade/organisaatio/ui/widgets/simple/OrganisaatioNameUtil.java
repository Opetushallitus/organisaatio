package fi.vm.sade.organisaatio.ui.widgets.simple;

import fi.vm.sade.organisaatio.api.model.types.OrganizationStructureType;
import org.apache.commons.lang.StringUtils;

/**
 * User: wuoti
 * Date: 25.7.2013
 * Time: 16.08
 */
public abstract class OrganisaatioNameUtil {

    private final static String[] LANG_PREFERRED_ORDER = {"fi", "sv", "en"};

    public static String getNameForLanguage(OrgStructure o, String lang) {
        String name = null;

        if ("fi".equalsIgnoreCase(lang)) {
            name = o.getNameFi();
        } else if ("sv".equalsIgnoreCase(lang)) {
            name = o.getNameSv();
        } else if ("en".equalsIgnoreCase(lang)) {
            name = o.getNameEn();
        } else {
            name = o.getNameFi();
        }

        return name;
    }

    public static String getNameForLanguage(OrganizationStructureType o, String lang) {
        String name = null;

        if ("fi".equalsIgnoreCase(lang)) {
            name = o.getNameFi();
        } else if ("sv".equalsIgnoreCase(lang)) {
            name = o.getNameSv();
        } else if ("en".equalsIgnoreCase(lang)) {
            name = o.getNameEn();
        } else {
            name = o.getNameFi();
        }

        return name;
    }

    public static String getPreferredOrganisaatioNameForLanguage(OrgStructure o, String lang) {
        String name = getNameForLanguage(o, lang);

        for (String l : LANG_PREFERRED_ORDER) {
            if (StringUtils.isNotBlank(name)) {
                break;
            }

            name = getNameForLanguage(o, l);
        }

        if (StringUtils.isBlank(name)) {
            name = "N/A " + o.getOid();
        }

        return name;
    }

    public static String getPreferredOrganisaatioNameForLanguage(OrganizationStructureType o, String lang) {
        String name = getNameForLanguage(o, lang);

        for (String l : LANG_PREFERRED_ORDER) {
            if (StringUtils.isNotBlank(name)) {
                break;
            }

            name = getNameForLanguage(o, l);
        }

        if (StringUtils.isBlank(name)) {
            name = "N/A " + o.getOid();
        }

        return name;
    }
}
