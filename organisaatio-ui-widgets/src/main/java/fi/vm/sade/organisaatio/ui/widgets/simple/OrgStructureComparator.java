package fi.vm.sade.organisaatio.ui.widgets.simple;

import fi.vm.sade.generic.common.I18N;

import java.util.Comparator;

/**
 * User: wuoti
 * Date: 25.7.2013
 * Time: 16.21
 */
public class OrgStructureComparator implements Comparator<OrgStructure> {
    @Override
    public int compare(OrgStructure o1, OrgStructure o2) {
        final String language = I18N.getLocale().getLanguage();

        return OrganisaatioNameUtil.getPreferredOrganisaatioNameForLanguage(o1, language)
                .compareTo(OrganisaatioNameUtil.getPreferredOrganisaatioNameForLanguage(o2, language));
    }

}
