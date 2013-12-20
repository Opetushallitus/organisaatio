package fi.vm.sade.organisaatio.ui.widgets.simple;

import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.ui.*;
import fi.vm.sade.generic.common.DateHelper;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.api.model.types.OrganizationStructureType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jukais
 * Date: 19.3.2013
 * Time: 16.30
 * To change this template use File | Settings | File Templates.
 */
public class OrganisaatioPopup extends Window {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioPopup.class);

    private Panel root;
    private OrganisaatioProxy organisaatioProxy;
    private HierarchicalContainer dataSource;
    private Tree organizationTree = new Tree();
    private Set<OrgStructure> organizations;

    private TextField searchField;


    public OrganisaatioPopup(OrganisaatioProxy organisaatioProxy) {
        this.organisaatioProxy = organisaatioProxy;
        initializeComponents();
    }

    private void initializeComponents() {

        root = new Panel();
        Layout horizontalLayout = new HorizontalLayout();

        searchField = new TextField();
        horizontalLayout.addComponent(searchField);
        searchField.setImmediate(true);
        searchField.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                searchTextChanged((String) searchField.getValue());
            }
        });

        root.addComponent(horizontalLayout);
        root.addComponent(organizationTree);

        organizationTree.setItemCaptionPropertyId("caption");
        organizationTree.setItemCaptionMode(Tree.ITEM_CAPTION_MODE_PROPERTY);
        organizationTree.setImmediate(true);

        addComponent(root);

    }

    private static boolean organisaatioAktiivinen(OrgStructure o) {
        return (o.getPoistettu() == null || !o.getPoistettu()) &&
                (o.getLakkautusPvm() == null || o.getLakkautusPvm().after(new Date()));
    }

    public void refreshData(Set<String> oids) {
        if (oids == null || oids.size() == 0) {
            return;
        }

        if (organizations == null || organizations.size() == 0) {
            List<String> oidList = new ArrayList<String>();
            oidList.addAll(oids);
            List<OrganizationStructureType> organizationStructure;
            try {
                organizationStructure = organisaatioProxy.getOrganisaatios(oidList);
            } catch (Exception e) {
                LOG.warn("Couldn't find organisation", e);
                return;
            }


            Map<String, OrgStructure> flatList = new HashMap<String, OrgStructure>();
            organizations = new TreeSet<OrgStructure>(new OrgStructureComparator());

            //create flat map
            for (OrganizationStructureType t : organizationStructure) {
                OrgStructure o = new OrgStructure();
                o.setNameFi(t.getNameFi());
                o.setNameSv(t.getNameSv());
                o.setNameEn(t.getNameEn());
                o.setOid(t.getOid());
                o.setParentOid(t.getParentOid());
                o.setLakkautusPvm(t.getLakkautusPvm() != null ? DateHelper.xmlCalToDate(t.getLakkautusPvm()) : null);
                o.setPoistettu(t.isPoistettu());
                flatList.put(o.getOid(), o);
            }

            //make structure
            for (String s : flatList.keySet()) {
                OrgStructure child = flatList.get(s);
                if (organisaatioAktiivinen(child)) {
                    if (StringUtils.isNotBlank(child.getParentoid())) {
                        OrgStructure parent = flatList.get(child.getParentoid());
                        if (parent != null) {
                            parent.addChild(child);
                            child.setParent(parent);
                        } else {
                            organizations.add(child);
                        }
                    } else {
                        organizations.add(child);
                    }
                }
            }
        }

        Set<OrgStructure> filtered = new TreeSet<OrgStructure>(new OrgStructureComparator());
        filterOrganizations(filtered, organizations, oids);
        organizations = filtered;

        dataSource = new HierarchicalContainer();
        dataSource.addContainerProperty("caption", String.class, null);
        organizationTree.setContainerDataSource(dataSource);
        dataSource.removeAllItems();
        addOrganizations(dataSource, null, filtered);

        searchTextChanged((String) searchField.getValue());
    }

    private void filterOrganizations(Set<OrgStructure> filtered, Set<OrgStructure> organizations, Set<String> oids) {
        for (OrgStructure organization : organizations) {
            if (oids.contains(organization.getOid())) {
                filtered.add(organization);
            } else {
                filterOrganizations(filtered, organization.getChild(), oids);
            }
        }

    }


    private void addOrganizations(HierarchicalContainer organizations, OrgStructure parent, Set<OrgStructure> childs) {
        for (OrgStructure orgStructure : childs) {
            organizations.addItem(orgStructure);

            organizations.getContainerProperty(orgStructure, "caption").setValue(
                    OrganisaatioNameUtil.getPreferredOrganisaatioNameForLanguage(orgStructure, I18N.getLocale().getLanguage()));

            if (parent != null) {
                organizations.setParent(orgStructure, parent);
            }

            if (orgStructure.getChild().size() > 0) {
                addOrganizations(organizations, orgStructure, orgStructure.getChild());
            } else {
                organizations.setChildrenAllowed(orgStructure, false);
            }
        }
    }

    public Tree getTree() {
        return organizationTree;
    }

    public void closeWindow() {
        close();
    }

    public void searchTextChanged(final String searchText) {
        // apply search terms to tree item filter
        dataSource.removeAllContainerFilters();

        if (searchText.length() > 0) {
            SimpleStringFilter filter = new SimpleStringFilter("caption", searchText, true, false);
            dataSource.addContainerFilter(filter);
            // expand all visible items
            Collection visibleItems = organizationTree.getVisibleItemIds();
            for (Object visibleItem : visibleItems) {
                organizationTree.expandItemsRecursively(visibleItem);
            }
        }
    }
}
