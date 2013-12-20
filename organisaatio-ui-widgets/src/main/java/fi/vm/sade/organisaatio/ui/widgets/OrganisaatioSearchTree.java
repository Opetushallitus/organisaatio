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
package fi.vm.sade.organisaatio.ui.widgets;

import com.vaadin.ui.Tree;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.StyleNames;
import fi.vm.sade.generic.ui.component.DebugId;
import fi.vm.sade.generic.ui.component.SearchableTree;
import fi.vm.sade.generic.ui.component.TreeAdapter;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioSearchCriteriaDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;
import fi.vm.sade.organisaatio.ui.widgets.factory.OrganisaatioTreeAdapter;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Search tree component for organisaatios. Contains the search components specific to organisaatios.
 *
 * @author markus
 *
 */
public class OrganisaatioSearchTree extends SearchableTree<OrganisaatioDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioSearchTree.class);

    @DebugId(id = "org_tree_advanced_search")
    private AdvancedTreeSearchComponents advancedSearch;
    private OrganisaatioSearchType type;
    private List<String> oidRestrictions = null;

    public OrganisaatioSearchTree(String debugIdPrefix, TreeAdapter<OrganisaatioDTO> treeAdapter) {
        super(debugIdPrefix, treeAdapter);

        advancedSearch = new AdvancedTreeSearchComponents(new OrganisaatioSearchCriteriaDTO(), this);
        type = OrganisaatioSearchType.ALL_FIELDS;
    }

    public OrganisaatioSearchTree(String debugIdPrefix, TreeAdapter<OrganisaatioDTO> treeAdapter, OrganisaatioSearchType type) {
        super(debugIdPrefix, treeAdapter);
        advancedSearch = new AdvancedTreeSearchComponents(new OrganisaatioSearchCriteriaDTO(), this, type);
        advancedSearch.addStyleName(StyleNames.GRID_16);
        this.type = type;
    }

    @Override
    public void init() {
        switch (type) {
            case ADVANCED:
                super.init();
                super.hideTab();
                basicSearch.addComponent(advancedSearch, 1);
                break;

            case ALL_FIELDS:
                super.init();
                basicSearch.addComponent(advancedSearch, 1);
                break;

            case BASIC:
                super.init();
                super.hideTab();
                break;

            default:
                super.init();
                basicSearch.addComponent(advancedSearch, 1);
        }

    }

    public void setOidRestrictions(List<String> oidRestrictionsParam) {
        if (oidRestrictions != null) {
            for (String oid : oidRestrictionsParam) {
                if (!oidRestrictions.contains(oid)) {
                    oidRestrictions.add(oid);
                }
            }

        } else {
            oidRestrictions = oidRestrictionsParam;
        }
    }

    public void reloadWithSearchData(final OrganisaatioSearchCriteriaDTO search) {
        //if Oid restriction list is provided, use it...
        if (oidRestrictions != null) {
            search.getOidResctrictionList().clear();

            search.getOidResctrictionList().addAll(oidRestrictions);

        }
        // clear data
        dataSource.removeAllItems();
        // load new data
        Collection<OrganisaatioDTO> all = treeAdapter.find(search);
        // add data to tree
        addTreeNodes(all);
        // set result count
        setResultCount(all.size());
        // expand all visible items
        Collection visibleItems = tree.getVisibleItemIds();
        for (Object visibleItem : visibleItems) {
            tree.expandItemsRecursively(visibleItem);
        }
        final String searchStr = search.getSearchStr();
        tree.setItemStyleGenerator(new Tree.ItemStyleGenerator() {
            public String getStyle(Object itemId) {

                if (itemMatchesSearch(search, (OrganisaatioDTO) itemId)) {

                    return "highlight";
                }

                return null;
            }
        });

    }

    public void reloadWithOids(List<String> oids) {
        if (treeAdapter instanceof OrganisaatioTreeAdapter && oids != null && !oids.isEmpty()) {
            // clear data
            dataSource.removeAllItems();
            setOidRestrictions(oids);
            // load new data
            Collection<OrganisaatioDTO> all = ((OrganisaatioTreeAdapter) treeAdapter).findByParentOids(oids);
            // add data to tree
            addTreeNodes(all);
            // set result count
            setResultCount(all.size());

        }
    }

    public boolean itemMatchesSearch(OrganisaatioSearchCriteriaDTO search, OrganisaatioDTO organisaatio) {
        return !initialState(search)
                && organisaatioTyyppiMatches(organisaatio, search)
                && oppilaitosTyyppiMatches(organisaatio, search)
                && textMatches(organisaatio, search)
                && voimassaoloMatches(organisaatio, search);
    }

    @Override
    protected void searchTextChanged(final String searchText) {
        // apply search terms to tree item filter
        advancedSearch.getModel().setSearchStr(searchText);
        reloadWithSearchData(advancedSearch.getModel());
        //super.searchTextChanged(this.searchBox.getValue().toString());
    }

    @Override
    public void reload() {
        super.reload();
        //advancedSearch.getModel().setSearchStr("");
        advancedSearch.getModel().setOppilaitosTyyppi(null);
        advancedSearch.getModel().setOrganisaatioTyyppi(null);
        advancedSearch.getModel().setLakkautetut(true);
        advancedSearch.getModel().setSuunnitellut(true);
    }

    private boolean initialState(OrganisaatioSearchCriteriaDTO search) {

        return (search.getOppilaitosTyyppi() == null)
                && (search.getOrganisaatioTyyppi() == null)
                && (search.isLakkautetut())
                && (search.isSuunnitellut())
                && ((search.getSearchStr() == null) || (search.getSearchStr().length() < 1));
    }

    private boolean organisaatioTyyppiMatches(OrganisaatioDTO organisaatio, OrganisaatioSearchCriteriaDTO searchSpec) {

        String orgTyypSpec = searchSpec.getOrganisaatioTyyppi();

        if (orgTyypSpec == null) {
            return true;
        } else {
            List<OrganisaatioTyyppi> orgTypes = organisaatio.getTyypit();
            boolean isFound = false;
            for (OrganisaatioTyyppi tyyppi : orgTypes) {
                if (tyyppi.value().equals(orgTyypSpec)) {
                    isFound = true;
                }
            }

            return isFound;
        }
    }

    private boolean oppilaitosTyyppiMatches(OrganisaatioDTO organisaatio, OrganisaatioSearchCriteriaDTO searchSpec) {

        String oppilaitosTyyppi = searchSpec.getOppilaitosTyyppi();

        return (oppilaitosTyyppi == null)
                || oppilaitosTyyppi.equalsIgnoreCase(organisaatio.getOppilaitosTyyppi());
    }

    private boolean textMatches(OrganisaatioDTO organisaatio, OrganisaatioSearchCriteriaDTO searchSpec) {
        //DEBUGSAWAY://log.debug("Search string is: " + searchSpec.getSearchStr());

        String searchStr = (searchSpec.getSearchStr() != null) ? searchSpec.getSearchStr() : "";
        if (searchStr.length() <= 0) {
            return true;
        }
        return isPropertyMatch(OrganisaatioDisplayHelper.getClosest(I18N.getLocale(), organisaatio), searchStr);

    }

    private boolean isPropertyMatch(String val, String searchStr) {
        return (val != null) && val.toLowerCase().contains(searchStr.toLowerCase());
    }

    private boolean voimassaoloMatches(OrganisaatioDTO organisaatio, OrganisaatioSearchCriteriaDTO searchSpec) {
        if ((organisaatio.getAlkuPvm() == null) && (organisaatio.getLakkautusPvm() == null)) {
            return true;
        }
        if (searchSpec.isLakkautetut() && searchSpec.isSuunnitellut()) {
            return true;
        }
        if (searchSpec.isLakkautetut() && !searchSpec.isSuunnitellut()
                && ((organisaatio.getAlkuPvm() == null)
                || (organisaatio.getAlkuPvm().getTime() <= System.currentTimeMillis()))) {
            return true;
        }
        if (!searchSpec.isLakkautetut() && searchSpec.isSuunnitellut()
                && ((organisaatio.getLakkautusPvm() == null)
                || (organisaatio.getLakkautusPvm().getTime() >= System.currentTimeMillis()))) {
            return true;
        }
        if (!searchSpec.isLakkautetut() && !searchSpec.isSuunnitellut()
                && ((organisaatio.getAlkuPvm() == null)
                || (organisaatio.getAlkuPvm().getTime() <= System.currentTimeMillis()))
                && ((organisaatio.getLakkautusPvm() == null)
                || (organisaatio.getLakkautusPvm().getTime() >= System.currentTimeMillis()))) {
            return true;
        }
        return false;
    }
}
