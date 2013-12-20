package fi.vm.sade.organisaatio.revised.ui.component.mainview;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.base.Joiner;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.blackboard.BlackboardContext;
import fi.vm.sade.generic.ui.validation.ErrorMessage;
import fi.vm.sade.organisaatio.api.model.types.EmailDTO;
import fi.vm.sade.organisaatio.api.model.types.KuvailevaTietoTyyppi;
import fi.vm.sade.organisaatio.api.model.types.KuvailevaTietoTyyppiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioKuvailevatTiedotTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinnumeroDTO;
import fi.vm.sade.organisaatio.api.model.types.SoMeLinkkiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.WwwDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteyshenkiloTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoArvoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;
import fi.vm.sade.organisaatio.revised.ui.component.mainview.OrganisaatioModelWrapper.ImportState;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioRowMenuEvent;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioViewButtonEvent;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoHelper;
import fi.vm.sade.organisaatio.ui.component.OrganisaatioTable;
import fi.vm.sade.organisaatio.ui.listener.YtjSelectListener;
import fi.vm.sade.organisaatio.ui.listener.event.YtjSelectedEventImpl;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import fi.vm.sade.vaadin.Oph;
import fi.vm.sade.vaadin.util.UiUtil;

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
 * @author Timo Santasalo / Teknokala Ky
 */
public class OrganisaatioMainViewImpl extends AbstractOrganisaatioMainView implements OrganisaatioMainView {

	private static final long serialVersionUID = 1L;
	
	private static final float GRID_RATIO = 0.2f;

	//private final Logger log = LoggerFactory.getLogger(getClass());

    //Layouts
	private VerticalLayout mainLayout;
    private VerticalLayout organisaatioChildTreeLayout;
    private VerticalLayout organisaatioHdrLayout;
    private final Label organisaatioNimiLbl = new Label();
    private final Label orgnisaatioVoimassaOloLbl = new Label();
    private final Label organisaatioKotikuntaLbl = new Label();
    private final Label organisaatioYritysMuotoLbl = new Label();
    private final Label organisaatioYtunnus = new Label();
    private final Label organisaatioVirastoTunnus = new Label();
    private final Label organisaatioMaaLbl = new Label();
    private final Label organisaatioKieliLbl = new Label();
    private final Label organisaatioKayntiOsoiteLbl = new Label();
    private final Label organisaatioPostiOsoiteLbl = new Label();
    private final Label organisaatioDynaaminenOsoiteLbl = new Label();
    private final Label organisaatioTyyppiLbl = new Label();
    
    private final GridLayout dynaamisetYhteystiedot = buildFormGrid(GRID_RATIO);

    private final Label tkKoodiLbl = new Label();
    private final Label oppilaitosTyyppiLbl = new Label();

    private final Label vuosiluokatLbl = new Label();

    private ImageViewer oktKuva;
    private final Label oktHakutoimistonNimi = new Label();
    private final Label oktKayntiOsoite = new Label();
    private final Label oktPostiOsoite = new Label();
    private final Label oktMuuOsoite = new Label();
    private final Label oktPuhelin = new Label();
    private final Label oktFax = new Label();
    private final Label oktEmail = new Label();
    private final Label oktWww = new Label();
    private final Label oktKoordinaattori = new Label();
    private final Label oktYleisKuvaus = new Label();
    private final Label oktVuosikello = new Label();
    private final Label oktVastuuhenkilot = new Label();
    private final Label oktValintaMenettely = new Label();
    private final Label oktAhotKaytannot = new Label();
    private final Label oktEvTukipalvelut = new Label();
    private final Label oktOppimisYmparisto = new Label();
    private final Label oktKieliOpinnot = new Label();
    private final Label oktTyoHarjoittelu = new Label();
    private final Label oktLiikkuvuus = new Label();
    private final Label oktKvOhjelmat = new Label();
    
    private final Label someFacebook = new Label();
    private final Label someLinkedIn = new Label();
    private final Label someTwitter = new Label();
    private final Label someGoogle = new Label();
    private final Label someMuu = new Label();

    private final Label poKustannukset = new Label();
    private final Label poAsuminen = new Label();
    private final Label poRahoitus = new Label();
    private final Label poRuokailu = new Label();
    private final Label poTerveydenHuolto = new Label();
    private final Label poVakuutukset = new Label();
    private final Label poOpiskelijaLiikunta = new Label();
    private final Label poVapaaAika = new Label();
    private final Label poOpiskelijaJarjestot = new Label();

    //Buttons
    //private Button buttonTakaisin;
    //Organisaatio child tree
    private TreeTable organisaatioChildTree;
    //Presenter autowired by Spring
    private OrganisaatioViewPresenter presenter;
    //YtjSearchDialog
    private Window orgSelectPopup;
    //Confirmation dialog shown when deleting organization
    private Window confirmationDialogWindow;
    private ErrorMessage errorView;
    private static final String CHILD_TREE_PROPERTY = "childOrganisaatioButton";

    private final String lang;
    private final boolean mutable;
    private final boolean standalone;
    private final String oid;

    /**
     * @param oid
     * @param lang Kielikoodi
     * @param organisaatios
     */
    public OrganisaatioMainViewImpl(String oid, String lang, boolean mutable, boolean standalone, OrganisaatioViewPresenter presenter) {
        this.presenter = presenter;
        this.lang = lang;
        this.mutable = mutable;
        this.standalone = standalone;
        this.oid = oid;
        init();
    }

    public OrganisaatioMainViewImpl() {
        presenter = new OrganisaatioViewPresenterImpl();
        mutable = false;
        standalone = true;
        lang = null; // ?
        oid = null;
        init();
    }

    @Override
    public Component getComponent() {
    	return this;
    }
    
    public OrganisaatioViewPresenter getPresenter() {
		return presenter;
	}

    private void init() {
        buildMainLayout();
        setCompositionRoot(mainLayout);
    }

    private VerticalLayout buildMainLayout() {
        // common part: create layout
        mainLayout = new VerticalLayout();

        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");
        mainLayout.setMargin(false);

        // top-level component properties
        setWidth("100.0%");
        setHeight("100.0%");

        //Add child layouts to form
        mainLayout.addComponent(buildOrganisaatioHdrLayout(standalone));

        errorView = new ErrorMessage();

        mainLayout.addComponent(errorView);

        mainLayout.addComponent(buildHeaderForComponent(
        		"OrganisaatioMainView.lblOrganisaationTiedot",
        		OrganisaatioViewButtonEvent.MUOKKAA_YLEISTIEDOT,
        		buildOrganisaationTiedotLayout(),
        		dynaamisetYhteystiedot)/*.open()*/);
        mainLayout.addComponent(buildHeaderForComponent(
        		"OrganisaatioMainView.lblOrganisaationKuvailevatTiedot",
        		OrganisaatioViewButtonEvent.MUOKKAA_KOULUTUSTARJOAJATIEDOT,
        		buildKoulutusTiedotLayout(),
        		buildKuvailevatTiedotLayout()));
        mainLayout.addComponent(buildHeaderForComponent(
        		"OrganisaatioMainView.lblPalvelutOppijalle",
        		OrganisaatioViewButtonEvent.MUOKKAA_PALVELUT_OPPIJALLE,
        		buildPalvelutOppijalleLayout()));
        mainLayout.addComponent(buildHeaderForComponent(
        		"OrganisaatioMainView.lblSisaltyvatOrganisaatiot", null,
        		buildChildOrganisaatioTreeLayout()));

        return mainLayout;
    }

    private Button getEditButton(final String ev) {
    	Button ret = UiUtils.buttonSmallSecodary(null, I18N.getMessage("OrganisaatioMainView.buttonMuokkaa"), new Button.ClickListener() {
    		private static final long serialVersionUID = 5019806363620874205L;

            @Override
            public void buttonClick(ClickEvent event) {
                BlackboardContext.getBlackboard().fire(new OrganisaatioViewButtonEvent(presenter.getSelectedOrganisaatio(), ev));
            }
        });
    	return ret;
	}

    @Override
    public void setErrorMessage(String errorMessage) {
        errorView.addError(errorMessage);
    }

    @Override
    public void setKotipaikka(String kotipaikka) {
        if (organisaatioKotikuntaLbl != null) {
            organisaatioKotikuntaLbl.setValue(kotipaikka);
        }
    }

    @Override
    public void setOppilaitosTyyppiValue(String value) {
        if (oppilaitosTyyppiLbl != null) {
            oppilaitosTyyppiLbl.setValue(value);
        }
    }

    @Override
    public void setMaaValue(String value) {
        if (organisaatioMaaLbl != null) {
            organisaatioMaaLbl.setValue(value);
        }
    }

    @Override
    public void setKieliValues(List<String> values) {
        if (organisaatioKieliLbl != null) {
            Collections.sort(values);
            organisaatioKieliLbl.setValue(Joiner.on(", ").join(values).toString());
        }
    }

    @Override
    public void setVuosiluokatValues(List<String> values) {
        if (vuosiluokatLbl != null) {
            Collections.sort(values);
            vuosiluokatLbl.setValue(Joiner.on("\n").join(values).toString());
        }
    }

    private String aggregateOrgNames(OrganisaatioDTO organisaatioParam, ImportState state, Date importDate) {
    	StringBuffer ret = new StringBuffer();

    	for (Teksti txt : organisaatioParam.getNimi().getTeksti()) {
    		if (ret.length()>0) {
    			ret.append('\n');
    		}
    		
    		ret.append(txt.getValue())
    			.append(presenter.formatImportInfo(importDate, state))
    			.append(" (")
				.append(new KoodistoHelper().tryGetArvoByKoodi(txt.getKieliKoodi()))
				.append(')');
	 	}

    	return ret.toString();

    }

    public String formatImportInfo(String formatStr, Date ytjDate) {
        StringBuilder builder = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat(I18N.getMessage("OrganisaatioMainView.dateFormat"));
        return builder.append(formatStr)
                .append(" (")
                .append(I18N.getMessage("OrganisaatioMainView.haettuYtjLabel"))
                .append(sdf.format(ytjDate))
                .append(")")
                .toString();
    }

	@Override
    public void bindOrganisaatioTiedot(OrganisaatioModelWrapper orgm) {

    	//log.info("Bind: {} tiedot={}", organisaatioParam, organisaatioPerustiedot);
		
		OrganisaatioDTO organisaatioParam = orgm.get();
		
		refreshButtons(organisaatioParam);

        organisaatioNimiLbl.setContentMode(Label.CONTENT_PREFORMATTED);
        organisaatioNimiLbl.setValue(aggregateOrgNames(organisaatioParam, orgm.getImported(), orgm.getImportedDate()));

        setVoimassaOlo(organisaatioParam.getAlkuPvm(), organisaatioParam.getLakkautusPvm());
        organisaatioYritysMuotoLbl.setValue(organisaatioParam.getYritysmuoto() != null ? organisaatioParam.getYritysmuoto() : "");
        organisaatioYtunnus.setValue(organisaatioParam.getYtunnus());
        organisaatioVirastoTunnus.setValue(organisaatioParam.getVirastoTunnus());

        dynaamisetYhteystiedot.removeAllComponents();
        dynaamisetYhteystiedot.setRows(1);
        for (Entry<MonikielinenTekstiTyyppi,  List<Entry<MonikielinenTekstiTyyppi, Object>>> e : orgm.getDynaamisetYhteystiedot()) {
        	
        	dynaamisetYhteystiedot.setRows(dynaamisetYhteystiedot.getRows()+1);
        	dynaamisetYhteystiedot.addComponent(new Label(getTeksti(e.getKey())), 0, dynaamisetYhteystiedot.getCursorY(), 1, dynaamisetYhteystiedot.getCursorY());

        	for (Entry<MonikielinenTekstiTyyppi, Object> v : e.getValue()) {
            	Label title = new Label(getTeksti(v.getKey()));
            	title.addStyleName(Oph.TEXT_ALIGN_RIGHT);

            	Label value = new Label();
            	if (v.getValue() instanceof OsoiteDTO) {
            		value.setValue(formatOsoite((OsoiteDTO) v.getValue()));
            		value.setContentMode(Label.CONTENT_PREFORMATTED);
            	} else if (v.getValue() instanceof WwwDTO) {
            		value.setValue(((WwwDTO) v.getValue()).getWwwOsoite());
            	} else if (v.getValue() instanceof EmailDTO) {
            		value.setValue(((EmailDTO) v.getValue()).getEmail());
            	} else if (v.getValue() instanceof PuhelinnumeroDTO) {
            		value.setValue(((PuhelinnumeroDTO) v.getValue()).getPuhelinnumero());
            	} else {
            		value.setValue(String.valueOf(v.getValue()));
            	}
            	
            	dynaamisetYhteystiedot.addComponent(title);
            	dynaamisetYhteystiedot.addComponent(value);
        	}
        }
        
        StringBuffer oss = new StringBuffer();
        for (OsoiteDTO tr : organisaatioParam.getMuutOsoitteet()) {
        	oss.append(formatOsoite(tr)).append('\n');
        }
        
        for (YhteystietoArvoDTO ya : organisaatioParam.getYhteystietoArvos()) {
        	if (ya.getArvo() instanceof OsoiteDTO) {
            	oss.append(formatOsoite(((OsoiteDTO) ya.getArvo())))
            		.append('\n');
        	}
        }
        
        organisaatioDynaaminenOsoiteLbl.setContentMode(Label.CONTENT_PREFORMATTED);
        organisaatioDynaaminenOsoiteLbl.setValue(oss.toString().trim());
        
        organisaatioPostiOsoiteLbl.setContentMode(Label.CONTENT_PREFORMATTED);
        organisaatioPostiOsoiteLbl.setValue(formatOsoitteet(
        		orgm.getOsoite(OsoiteTyyppi.POSTI),
        		orgm.getOsoite(OsoiteTyyppi.RUOTSI_POSTI)
        		));
        
        organisaatioKayntiOsoiteLbl.setContentMode(Label.CONTENT_PREFORMATTED);
        organisaatioKayntiOsoiteLbl.setValue(formatOsoitteet(
        		orgm.getOsoite(OsoiteTyyppi.KAYNTI),
        		orgm.getOsoite(OsoiteTyyppi.RUOTSI_KAYNTI)
        		));
        
        if (organisaatioParam.getTyypit() != null) {
            organisaatioTyyppiLbl.setValue(formatOrganisaatioTyypit(organisaatioParam));
        }

        tkKoodiLbl.setValue(organisaatioParam.getOppilaitosKoodi() != null ? organisaatioParam.getOppilaitosKoodi() : "");

        bindKuvailevatTiedot(organisaatioParam.getKuvailevatTiedot());
        
        orgm.setDescendants(presenter.fetchChildOrganisaatios(organisaatioParam.getOid()));
        
        addElementsToTree(orgm.getChildren());
    }

    private String getTeksti(MonikielinenTekstiTyyppi mtt) {
		for (Teksti txt : mtt.getTeksti()) {
			if (lang==null || lang.equals(txt.getKieliKoodi())) {
				return txt.getValue();
			}
		}
    	return null;
    }

    private String getVapaaTieto(OrganisaatioKuvailevatTiedotTyyppi kt, KuvailevaTietoTyyppiTyyppi tt) {
    	for (KuvailevaTietoTyyppi ktt : kt.getVapaatKuvaukset()) {
    		if (ktt.getTyyppi().equals(tt)) {
    			return getTeksti(ktt.getSisalto());
    		}
    	}
    	return null;
    }

    private String getEctsKoordinaattori(OrganisaatioKuvailevatTiedotTyyppi kt) {
    	YhteyshenkiloTyyppi yh = kt.getHakutoimisto().getEctsYhteyshenkilo();
    	if (yh == null) {
    		return "";
    	}
    	
    	ArrayList<String> parts = new ArrayList<String>();
    	
    	addIfNotEmpty(parts, yh.getKokoNimi());
                addIfNotEmpty(parts, yh.getTitteli());
                
                String ects = Joiner.on(", ").join(parts);
                ects+="\n";
                parts.clear();
                
                addIfNotEmpty(parts, yh.getPuhelin());
                addIfNotEmpty(parts, yh.getEmail());
                ects+=Joiner.on(", ").join(parts);
                
                return ects;
    }

    private void addIfNotEmpty(ArrayList<String> parts, String value) {
        if (value != null && !value.trim().isEmpty()) {
            parts.add(value);
        }
    }

    private void bindKuvailevatTiedot(OrganisaatioKuvailevatTiedotTyyppi kt) {

    	if (kt==null) {
    		return;
    	}
    	
    	StringBuffer muusome = new StringBuffer();
    	for (SoMeLinkkiTyyppi st : kt.getSoMeLinkit()) {
    		switch (st.getTyyppi()) {
    		case FACEBOOK:
    			someFacebook.setValue(st.getSisalto());
    			break;
    		case LINKED_IN:
    			someLinkedIn.setValue(st.getSisalto());
    			break;
    		case TWITTER:
    			someTwitter.setValue(st.getSisalto());
    			break;
    		case GOOGLE_PLUS:
    			someGoogle.setValue(st.getSisalto());
    			break;
    		case MUU:
    			muusome.append(st.getSisalto()).append('\n');
    			break;
    		}
    	}
    	someMuu.setValue(muusome.toString().trim());

    	oktKuva.setImage(kt.getKuva());

        oktHakutoimistonNimi.setValue(kt.getHakutoimisto()==null || kt.getHakutoimisto().getOpintotoimistoNimi()==null
        		? null
    			: getTeksti(kt.getHakutoimisto().getOpintotoimistoNimi()));

        for (YhteystietoDTO yt : kt.getHakutoimisto().getOpintotoimistoYhteystiedot()) {
        	if (yt instanceof EmailDTO) {
        		oktEmail.setValue(((EmailDTO) yt).getEmail());
        	} else if (yt instanceof OsoiteDTO) {
        		OsoiteDTO od = (OsoiteDTO) yt;
        		switch (od.getOsoiteTyyppi()) {
        		case KAYNTI:
        			oktKayntiOsoite.setCaption(OrganisaatioDisplayHelper.formatOsoiteAsString(od));
        			break;
        		case POSTI:
        			oktPostiOsoite.setCaption(OrganisaatioDisplayHelper.formatOsoiteAsString(od));
        			break;
        		case MUU:
        			oktMuuOsoite.setCaption(OrganisaatioDisplayHelper.formatOsoiteAsString(od));
        			break;
    			default:
    				throw new NotImplementedException("Ei tuettu: "+od.getOsoiteTyyppi());
        		}
        	} else if (yt instanceof PuhelinnumeroDTO) {
        		PuhelinnumeroDTO pd = (PuhelinnumeroDTO) yt;
        		switch (pd.getTyyppi()) {
        		case FAKSI:
        			oktFax.setValue(pd.getPuhelinnumero());
        			break;
        		case PUHELIN:
        			oktPuhelin.setValue(pd.getPuhelinnumero());
        			break;
    			default:
    				throw new NotImplementedException("Ei tuettu: "+pd.getTyyppi());
        		}
        	} else if (yt instanceof WwwDTO) {
        		WwwDTO wd = (WwwDTO) yt;
				oktWww.setValue(wd.getWwwOsoite());
        	} else {
        		throw new NotImplementedException("Ei tuettu: "+yt);
        	}
        }
        oktKoordinaattori.setValue(getEctsKoordinaattori(kt));

        oktYleisKuvaus.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.YLEISKUVAUS));
        oktVuosikello.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.VUOSIKELLO));
        oktVastuuhenkilot.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.VASTUUHENKILOT));
        oktValintaMenettely.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.VALINTAMENETTELY));
        oktAhotKaytannot.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.AIEMMIN_HANKITTU_OSAAMINEN));
        oktEvTukipalvelut.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.ESTEETOMYYS));
        oktOppimisYmparisto.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.OPPIMISYMPARISTO));
        oktKieliOpinnot.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.KIELIOPINNOT));
        oktTyoHarjoittelu.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.TYOHARJOITTELU));
        oktLiikkuvuus.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.OPISKELIJALIIKKUVUUS));
        oktKvOhjelmat.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.KANSAINVALISET_KOULUTUSOHJELMAT));

        poKustannukset.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.KUSTANNUKSET));
        poAsuminen.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.TIETOA_ASUMISESTA));
        poRahoitus.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.RAHOITUS));
        poRuokailu.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.OPISKELIJARUOKAILU));
        poTerveydenHuolto.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.TERVEYDENHUOLTOPALVELUT));
        poVakuutukset.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.VAKUUTUKSET));
        poOpiskelijaLiikunta.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.OPISKELIJALIIKUNTA));
        poVapaaAika.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.VAPAA_AIKA));
        poOpiskelijaJarjestot.setValue(getVapaaTieto(kt, KuvailevaTietoTyyppiTyyppi.OPISKELIJA_JARJESTOT));
    }

    private String formatDate(Date date) {
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            return formatter.format(date);
        } else {
            return "";
        }
    }

    private void setVoimassaOlo(Date from, Date to) {
        orgnisaatioVoimassaOloLbl.setValue(formatDate(from) + " - " + formatDate(to));
    }

    private String formatOrganisaatioTyypit(OrganisaatioDTO organisaatio) {
        StringBuilder tyypit = new StringBuilder();
        for (OrganisaatioTyyppi tyyppi : organisaatio.getTyypit()) {
            if (tyypit.length() == 0) {
                tyypit.append(I18N.getMessage(tyyppi.name()));
            } else {
                tyypit.append(", ").append(I18N.getMessage(tyyppi.name()));
            }
        }
        return tyypit.toString();
    }

    @Override
    public void showYtjDialog() {
        List<YTJDTO> ytjDtos = new ArrayList<YTJDTO>();//ytjService.findByYNimi(organisationName, true, YTJKieli.FI);
        orgSelectPopup = new Window(I18N.getMessage("c_yritysValintaHdr"));
        orgSelectPopup.center();
        orgSelectPopup.setResizable(false);
        getWindow().addWindow(orgSelectPopup);
        final OrganisaatioTable ot = new OrganisaatioTable(ytjDtos, presenter.getSelectedOrganisaatio().getYtunnus());
        ot.addListener(new YtjSelectListener() {
            @Override
            public void organizationSelected(YtjSelectedEventImpl event) {
                 presenter.processYtjSelectEvent(event);
            }
        });

        ot.setOldOrgOid(this.oid);
        orgSelectPopup.setContent(ot);

        orgSelectPopup.setModal(true);
        ot.setSizeUndefined();

    }

    @Override
    public void closeYtjDialog() {
        getWindow().removeWindow(orgSelectPopup);
    }

    private String formatOsoite(OsoiteDTO osoite) {
        return presenter.formatOsoite(osoite);
    }

    private String formatOsoitteet(OsoiteDTO... osoitteet) {
    	StringBuilder ret = new StringBuilder();
    	for (OsoiteDTO o : osoitteet) {
    		if (o==null) {
    			continue;
    		}
    		
    		if (ret.length()>0) {
    			ret.append("\n");
    		}

    		ret.append(formatOsoite(o));
    	}
    	return ret.toString();
    }

    
    private void clearLabels(Label... lbls) {
    	for (Label l : lbls) {
    		l.setValue(null);
    	}
    }

    @Override
    public void clearView() {

    	oktKuva.setImage(null);
        clearLabels(
	        organisaatioNimiLbl,
	        orgnisaatioVoimassaOloLbl,
	        organisaatioKotikuntaLbl,
	        organisaatioKotikuntaLbl,
	        organisaatioYritysMuotoLbl,
	        organisaatioMaaLbl,
	        organisaatioKieliLbl,
	        organisaatioKayntiOsoiteLbl,
	        organisaatioPostiOsoiteLbl,
	        organisaatioDynaaminenOsoiteLbl,
	        organisaatioTyyppiLbl,
	        tkKoodiLbl,
	        oppilaitosTyyppiLbl,
            vuosiluokatLbl,

	        oktHakutoimistonNimi,
	        oktKayntiOsoite,
	        oktPostiOsoite,
	        oktMuuOsoite,
	        oktPuhelin,
	        oktFax,
	        oktEmail,
	        oktWww,
	        oktKoordinaattori,
	        oktYleisKuvaus,
	        oktVuosikello,
         	oktVastuuhenkilot,
         	oktValintaMenettely,
         	oktAhotKaytannot,
         	oktEvTukipalvelut,
         	oktOppimisYmparisto,
         	oktKieliOpinnot,
         	oktTyoHarjoittelu,
         	oktLiikkuvuus,
         	oktKvOhjelmat,

         	poKustannukset,
         	poAsuminen,
         	poRahoitus,
         	poRuokailu,
         	poTerveydenHuolto,
         	poVakuutukset,
         	poOpiskelijaLiikunta,
         	poVapaaAika,
         	poOpiskelijaJarjestot

        );

        if (organisaatioChildTree != null) {
            organisaatioChildTree.removeAllItems();
        }
    }
    //Set up organisaatio tieto labels

    private GridLayout buildOrganisaationTiedotLayout() {
    	
    	GridLayout layout = new GridLayout(2,1);
    	layout.setWidth("100%");
    	layout.setHeight("100%");
    	layout.setColumnExpandRatio(0, 1 - GRID_RATIO);
    	layout.setColumnExpandRatio(1, GRID_RATIO);
    	
    	oktKuva = new ImageViewer(layout);
    	oktKuva.addStyleName(Oph.TEXT_ALIGN_RIGHT);
    	
    	GridLayout organisaationTiedotFormLayout = buildFormGrid(GRID_RATIO / (1-GRID_RATIO) ,
        		new FieldInfo("OrganisaatioMainView.organisaatioNimiLbl", organisaatioNimiLbl),
        		new FieldInfo("OrganisaatioMainView.orgnisaatioVoimassaOloLbl", orgnisaatioVoimassaOloLbl),
        		new FieldInfo("OrganisaatioMainView.organisaatioKotikuntaLbl", organisaatioKotikuntaLbl),
        		new FieldInfo("OrganisaatioMainView.organisaatioYritysMuotoLbl", organisaatioYritysMuotoLbl),
        		new FieldInfo("OrganisaatioMainView.organisaatioYtunnus", organisaatioYtunnus),
        		new FieldInfo("OrganisaatioMainView.organisaatioVirastoTunnus", organisaatioVirastoTunnus),
        		new FieldInfo("OrganisaatioMainView.organisaatioMaaLbl", organisaatioMaaLbl),
        		new FieldInfo("OrganisaatioMainView.organisaatioKieliLbl", organisaatioKieliLbl),
        		new FieldInfo("OrganisaatioMainView.organisaatioKayntiOsoiteLbl", organisaatioKayntiOsoiteLbl),
        		new FieldInfo("OrganisaatioMainView.organisaatioPostiOsoiteLbl", organisaatioPostiOsoiteLbl),
        		new FieldInfo("OrganisaatioMainView.organisaatioMuuOsoiteLbl", organisaatioDynaaminenOsoiteLbl),
        		new FieldInfo("OrganisaatioMainView.organisaatioTyyppiLbl", organisaatioTyyppiLbl),
        		new FieldInfo("OrganisaatioMainView.tkKoodiLbl", tkKoodiLbl),
        		new FieldInfo("OrganisaatioMainView.oppilaitosTyyppiLbl", oppilaitosTyyppiLbl),
                new FieldInfo("OrganisaatioMainView.vuosiluokatLbl", vuosiluokatLbl)
        		);

        vuosiluokatLbl.setContentMode(Label.CONTENT_PREFORMATTED);

        layout.addComponent(organisaationTiedotFormLayout);
        layout.addComponent(oktKuva);

    	return layout;
    }

    private GridLayout buildKoulutusTiedotLayout() {
    	GridLayout layout = new GridLayout(2,1);
    	layout.setWidth("100%");

    	GridLayout info = buildFormGrid(GRID_RATIO*2,
    			new FieldInfo("OrganisaatioKuvailevatTiedotFormView.hakutoimistoNimi", oktHakutoimistonNimi),
				new FieldInfo("OrganisaatioKuvailevatTiedotFormView.otKayntiOsoite", oktKayntiOsoite),
				new FieldInfo("OrganisaatioKuvailevatTiedotFormView.otPostiOsoite", oktPostiOsoite),
				new FieldInfo("OrganisaatioKuvailevatTiedotFormView.otMuuOsoite", oktMuuOsoite),
				new FieldInfo("OrganisaatioKuvailevatTiedotFormView.otPuhelin", oktPuhelin),
				new FieldInfo("OrganisaatioKuvailevatTiedotFormView.otFax", oktFax),
				new FieldInfo("OrganisaatioKuvailevatTiedotFormView.otEmail", oktEmail),
				new FieldInfo("OrganisaatioKuvailevatTiedotFormView.otWww", oktWww),
				new FieldInfo("OrganisaatioKuvailevatTiedotFormView.koordLabel", oktKoordinaattori, Label.CONTENT_PREFORMATTED)
    	);
    	
      	GridLayout some = buildFormGrid(GRID_RATIO*2,
    			new FieldInfo("OrganisaatioKuvailevatTiedotFormView.facebook", someFacebook),
    			new FieldInfo("OrganisaatioKuvailevatTiedotFormView.linkedIn", someLinkedIn),
    			new FieldInfo("OrganisaatioKuvailevatTiedotFormView.twitter", someTwitter),
    			new FieldInfo("OrganisaatioKuvailevatTiedotFormView.googlePlus", someGoogle),
    			new FieldInfo("OrganisaatioKuvailevatTiedotFormView.muu", someMuu, Label.CONTENT_PREFORMATTED)
		);
        layout.addComponent(info);
        layout.addComponent(some);

    	return layout;
    }

    private GridLayout buildKuvailevatTiedotLayout() {
    	
    	return buildFormGrid(GRID_RATIO,
			new FieldInfo("LOPKuvauksetFormView.yleiskuvaLabel", oktYleisKuvaus, Label.CONTENT_XHTML),
			new FieldInfo("LOPKuvauksetFormView.esteettomyysLabel", oktEvTukipalvelut, Label.CONTENT_XHTML),
			new FieldInfo("LOPKuvauksetFormView.oppimisymparistoLabel", oktOppimisYmparisto, Label.CONTENT_XHTML),
			new FieldInfo("LOPKuvauksetFormView.opetukseVuosikelloLabel", oktVuosikello, Label.CONTENT_XHTML),
			new FieldInfo("LOPKuvauksetFormView.vastuuhenkilotLabel", oktVastuuhenkilot, Label.CONTENT_XHTML),
			new FieldInfo("LOPKuvauksetFormView.valintamenettelyLabel", oktValintaMenettely, Label.CONTENT_XHTML),
			new FieldInfo("LOPKuvauksetFormView.aiemminHankittuLabel", oktAhotKaytannot, Label.CONTENT_XHTML),
			new FieldInfo("LOPKuvauksetFormView.kieliopinnotLabel", oktKieliOpinnot, Label.CONTENT_XHTML),
			new FieldInfo("LOPKuvauksetFormView.tyoharjoitteluLabel", oktTyoHarjoittelu, Label.CONTENT_XHTML),
			new FieldInfo("LOPKuvauksetFormView.opiskelijaliikkuvuusLabel", oktLiikkuvuus, Label.CONTENT_XHTML),
			new FieldInfo("LOPKuvauksetFormView.kansainvalisetOhjelmatLabel", oktKvOhjelmat, Label.CONTENT_XHTML)
			);
    }

    private GridLayout buildPalvelutOppijalleLayout() {
    	return buildFormGrid(GRID_RATIO,
			new FieldInfo("PalvelutOppijalleKuvauksetFormView.kustannuksetLabel", poKustannukset, Label.CONTENT_XHTML),
			new FieldInfo("PalvelutOppijalleKuvauksetFormView.tietoAsumisestaLabel", poAsuminen, Label.CONTENT_XHTML),
			new FieldInfo("PalvelutOppijalleKuvauksetFormView.rahoitusLabel", poRahoitus, Label.CONTENT_XHTML),
			new FieldInfo("PalvelutOppijalleKuvauksetFormView.ruokailuLabel", poRuokailu, Label.CONTENT_XHTML),
			new FieldInfo("PalvelutOppijalleKuvauksetFormView.terveydenhuoltoLabel", poTerveydenHuolto, Label.CONTENT_XHTML),
			new FieldInfo("PalvelutOppijalleKuvauksetFormView.vakuutuksetLabel", poVakuutukset, Label.CONTENT_XHTML),
			new FieldInfo("PalvelutOppijalleKuvauksetFormView.opiskelijaliikuntaLabel", poOpiskelijaLiikunta, Label.CONTENT_XHTML),
			new FieldInfo("PalvelutOppijalleKuvauksetFormView.vapaaAikaLabel", poVapaaAika, Label.CONTENT_XHTML),
			new FieldInfo("PalvelutOppijalleKuvauksetFormView.opiskelijajarjestotLabel", poOpiskelijaJarjestot, Label.CONTENT_XHTML)
			);
    }

    private VerticalLayout buildOrganisaatioHdrLayout(boolean showButtons) {
        // common part: create layout
        organisaatioHdrLayout = new VerticalLayout();
        organisaatioHdrLayout.setImmediate(false);
        organisaatioHdrLayout.setWidth("100.0%");
        organisaatioHdrLayout.setHeight("100.0%");
        organisaatioHdrLayout.setMargin(false);

        // buttonLayout
        if (showButtons) {
        	organisaatioHdrLayout.addComponent(buildButtonLayout());
        }


        return organisaatioHdrLayout;
    }

    private VerticalLayout buildChildOrganisaatioTreeLayout() {
        organisaatioChildTreeLayout = new VerticalLayout();
        organisaatioChildTreeLayout.setWidth("100%");
        organisaatioChildTreeLayout.addComponent(constructChildOrganisaatioTree());
        return organisaatioChildTreeLayout;
    }

    private TreeTable constructChildOrganisaatioTree() {
        organisaatioChildTree = new TreeTable();
        organisaatioChildTree.setColumnHeaderMode(TreeTable.COLUMN_HEADER_MODE_HIDDEN);
        organisaatioChildTree.addContainerProperty(CHILD_TREE_PROPERTY, Button.class, null);
        organisaatioChildTree.setWidth("100%");

        //TODO add recursive call for childrens children

        return organisaatioChildTree;
    }

    private void addElementsToTree(
           final List<OrganisaatioPerustieto> organisaatios) {
    	
        for (final OrganisaatioPerustieto curOrg : organisaatios) {
            if (!curOrg.getOid().equals(oid)) {
                Button buttonOrganisaatio = UiUtil.buttonLink(null, OrganisaatioDisplayHelper.getClosestBasic(I18N.getLocale(), curOrg), new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
                    public void buttonClick(ClickEvent event) {
                        OrganisaatioDTO fullOrg = presenter.findFullOrganisaatio(curOrg.getOid());
                        BlackboardContext.getBlackboard().fire(new OrganisaatioRowMenuEvent(fullOrg, I18N.getMessage("OrganisaatioView.btnTarkastele"), organisaatios));
                    }
                });

                organisaatioChildTree.addItem(curOrg);

                Property prop = organisaatioChildTree.getContainerProperty(curOrg, CHILD_TREE_PROPERTY);
                if (prop != null) {
                    prop.setValue(buttonOrganisaatio);
                }

            }
        }
        createHierarchy(organisaatios);

    }


    private void createHierarchy(List<OrganisaatioPerustieto> organisaatios) {
        HashMap<String, String> childParent = new HashMap<String, String>();
        HashMap<String, OrganisaatioPerustieto> oidOrg = new HashMap<String, OrganisaatioPerustieto>();
        HashSet<String> doesNotHaveChildren = new HashSet<String>();
        for (OrganisaatioPerustieto curOrg : organisaatios) {
            childParent.put(curOrg.getOid(), curOrg.getParentOid());
            oidOrg.put(curOrg.getOid(), curOrg);
            doesNotHaveChildren.add(curOrg.getOid());
        }

        for (OrganisaatioPerustieto curOrg : organisaatios) {
            final OrganisaatioPerustieto parent = oidOrg.get(curOrg.getParentOid());
            if (parent!=null) {
                // has parent!
                organisaatioChildTree.setParent(curOrg, parent);
                organisaatioChildTree.setChildrenAllowed(parent, true);
                doesNotHaveChildren.remove(parent.getOid());
            }
        }

        for(String oid: doesNotHaveChildren) {
            organisaatioChildTree.setChildrenAllowed(oidOrg.get(oid), false);
        }
    }

    private Component buildHeaderForComponent(String message, String editEvent, Component... cmp) {
        VerticalLayout rt = new VerticalLayout();
        rt.setMargin(true, true, true, true);
        rt.setSpacing(true);
        
        VerticalSplitPanel rtSplit = new VerticalSplitPanel();
        rtSplit.setWidth("100%");
        rtSplit.setHeight("2px");
        rtSplit.setLocked(true);
        rt.addComponent(rtSplit);

        GridLayout hdr = new GridLayout(2, 1);
        hdr.setColumnExpandRatio(0, 1);
        hdr.setColumnExpandRatio(1, 0);
        hdr.setWidth("100%");

        Label label = new Label(I18N.getMessage(message));
        label.addStyleName(Oph.LABEL_H2);
        hdr.addComponent(label, 0,0);

        if (editEvent!=null && mutable) {
        	hdr.addComponent(getEditButton(editEvent), 1, 0);
        }

        rt.addComponent(hdr);
        for (Component c : cmp) {
        	rt.addComponent(c);
        }

        //Disclosure dp = new Disclosure(I18N.getMessage(message), rt);
        //dp.setWidth("100%");

        return rt;
    }
    
    private static class FieldInfo {
    	
    	private final Label titleLabel;
    	private final Label valueLabel;

		public FieldInfo(String titleKey, Label valueLabel) {
			this(titleKey, valueLabel, Label.CONTENT_TEXT);
		}
		
		public FieldInfo(String titleKey, Label valueLabel, int cmode) {
			super();			
			this.valueLabel = valueLabel==null ? new Label() : valueLabel;
			this.valueLabel.setContentMode(cmode);
			
			titleLabel = new Label();
			titleLabel.setValue(I18N.getMessage(titleKey));
		}
    	
    }
    
    /**
     * @param ratio
     * @param kvpairs Otsikko-avain/arvo -pareja
     * @return
     */
    private GridLayout buildFormGrid(float ratio, FieldInfo... fields) {
    	GridLayout ret = new GridLayout(2, Math.max(1, fields.length));
    	ret.setSizeFull();
    	ret.setColumnExpandRatio(0, ratio);
    	ret.setColumnExpandRatio(1, 1-ratio);
    	ret.addStyleName(Oph.SPACING_BOTTOM_30);
    	ret.setSpacing(true);
    	
    	for (FieldInfo fi : fields) {
    		ret.addComponent(fi.titleLabel);
    		ret.addComponent(fi.valueLabel);
    		fi.titleLabel.addStyleName(Oph.TEXT_ALIGN_RIGHT);
    		//fi.titleLabel.addStyleName(Oph.SPACING_RIGHT_20);
    	}
    	
    	return ret;
    }



    @Override
    public void closeDialog() {
        getWindow().removeWindow(confirmationDialogWindow);
    }

    
    public Window getConfirmationDialogWindow() {
    	return confirmationDialogWindow;
    }

}
