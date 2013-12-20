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
package fi.vm.sade.organisaatio.revised.ui.component.mainview;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.organisaatio.api.model.types.KuvailevaTietoTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.auth.OrganisaatioContext;
import fi.vm.sade.organisaatio.auth.OrganisaatioPermissionServiceImpl;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoHelper;
import fi.vm.sade.organisaatio.ui.PortletRole;

import fi.vm.sade.generic.common.I18N;

/**
 * 
 * @author Timo Santasalo / Teknokala Ky
 */
public class OrganisaatioMainViewTabs extends AbstractOrganisaatioMainView implements
		OrganisaatioMainView {

	private static final long serialVersionUID = 1L;

	private final String oid;
	private final OrganisaatioViewPresenter presenter;

	private final TabSheet tabs = new TabSheet();

	private final Map<String, OrganisaatioMainViewImpl> langTabs = new HashMap<String, OrganisaatioMainViewImpl>();

	public OrganisaatioMainViewTabs(OrganisaatioModelWrapper orgm) {
		this.oid = orgm==null ? null : orgm.get().getOid();
		presenter = new OrganisaatioViewPresenterImpl();
		presenter.setOrganisaatioView(this);
		if (oid != null) {
			presenter.loadOrganisaatioWithOid(orgm);
		}
	}

	public OrganisaatioMainViewTabs() {
		this(null);
	}

	public OrganisaatioViewPresenter getPresenter() {
		return presenter;
	}

	@Override
	public Component getComponent() {
		return this;
	}

	private boolean isOrganisaatioMutable() {
		final OrganisaatioContext context = OrganisaatioContext.get(presenter
				.getSelectedOrganisaatio());
		final OrganisaatioPermissionServiceImpl permissionService = PortletRole
				.getInstance().getPermissionService();

		return permissionService.userCanUpdateOrganisation(context);
	}

	private void appendLangs(Set<String> ret, MonikielinenTekstiTyyppi txts) {
		if (txts == null) {
			return;
		}
		for (Teksti txt : txts.getTeksti()) {
			ret.add(txt.getKieliKoodi());
		}
	}

	private Set<String> aggregateLangs(OrganisaatioDTO org) {
		if (org.getKuvailevatTiedot() == null) {
			return Collections.emptySet();
		}
		Set<String> ret = new HashSet<String>();
		if (org.getKuvailevatTiedot().getHakutoimisto() != null) {
			appendLangs(ret, org.getKuvailevatTiedot().getHakutoimisto()
					.getOpintotoimistoNimi());
		}
		if (org.getKuvailevatTiedot().getVapaatKuvaukset() != null) {
			for (KuvailevaTietoTyyppi kt : org.getKuvailevatTiedot()
					.getVapaatKuvaukset()) {
				appendLangs(ret, kt.getSisalto());
			}
		}
		return ret;
	}

	@Override
	public void bindOrganisaatioTiedot(OrganisaatioModelWrapper orgm) {

		Set<String> lcs = aggregateLangs(orgm.get());
		if (lcs.isEmpty()) {
			OrganisaatioMainViewImpl mv = new OrganisaatioMainViewImpl(oid,
					null, isOrganisaatioMutable(), true, presenter);
			setCompositionRoot(mv);
			langTabs.put(null, mv);
		} else {			
			for (String lc : lcs) {
				OrganisaatioMainViewImpl mv = new OrganisaatioMainViewImpl(oid,
						lc, isOrganisaatioMutable(), lcs.size()==1, presenter);
				langTabs.put(lc, mv);

				String lang = new KoodistoHelper().tryGetArvoByKoodi(lc);
				if (lang == null) {
					lang = lc;
				}

				tabs.addTab(mv, lang);
			}
			if (lcs.size()>1) {
				VerticalLayout cl = new VerticalLayout();
				cl.addComponent(buildButtonLayout());
				cl.addComponent(tabs);
				setCompositionRoot(cl);
				refreshButtons(orgm.get());
			} else {
				setCompositionRoot(tabs);
			}
		}

		for (OrganisaatioMainViewImpl ov : langTabs.values()) {
			ov.bindOrganisaatioTiedot(orgm);
		}
		selectDefaultTab();

	}
	
	private void selectDefaultTab() {
	    String lang = I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.fiUri");
	    if (langTabs.get(lang) != null) {
	        tabs.setSelectedTab(langTabs.get(lang));
	    }
	}

	@Override
	public void clearView() {
		langTabs.clear();
		tabs.removeAllComponents();
	}

	private OrganisaatioMainViewImpl selectedTab() {
		OrganisaatioMainViewImpl tab = (OrganisaatioMainViewImpl) tabs.getSelectedTab();
		return (tab != null) ? tab : langTabs.get(null);
	}


	@Override
	public void showYtjDialog() {
		selectedTab().showYtjDialog();
	}

	@Override
	public void closeYtjDialog() {
		selectedTab().closeYtjDialog();
	}

	@Override
	public void togglePaivitaYtjButtonVisibility(boolean visible) {
		for (OrganisaatioMainViewImpl ov : langTabs.values()) {
			ov.togglePaivitaYtjButtonVisibility(visible);
		}
	}

	@Override
	public void setKotipaikka(String kotipaikka) {
		for (OrganisaatioMainViewImpl ov : langTabs.values()) {
			ov.setKotipaikka(kotipaikka);
		}
	}

	@Override
	public void closeDialog() {
		for (OrganisaatioMainViewImpl ov : langTabs.values()) {
			ov.closeDialog();
		}
	}

	@Override
	public void setOppilaitosTyyppiValue(String value) {
		for (OrganisaatioMainViewImpl ov : langTabs.values()) {
			ov.setOppilaitosTyyppiValue(value);
		}
	}

	@Override
	public void setMaaValue(String value) {
		for (OrganisaatioMainViewImpl ov : langTabs.values()) {
			ov.setMaaValue(value);
		}
	}

	@Override
	public void setKieliValues(List<String> value) {
		for (OrganisaatioMainViewImpl ov : langTabs.values()) {
			ov.setKieliValues(value);
		}
	}

    @Override
    public void setVuosiluokatValues(List<String> value) {
        for (OrganisaatioMainViewImpl ov : langTabs.values()) {
            ov.setVuosiluokatValues(value);
        }
    }

	@Override
	public void setErrorMessage(String errorMessage) {
		for (OrganisaatioMainViewImpl ov : langTabs.values()) {
			ov.setErrorMessage(errorMessage);
		}
	}

}
