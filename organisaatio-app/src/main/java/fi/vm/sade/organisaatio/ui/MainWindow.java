package fi.vm.sade.organisaatio.ui;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.context.i18n.LocaleContextHolder;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;
import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.StyleNames;
import fi.vm.sade.generic.ui.component.SearchableTree;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.revised.ui.OrganisaatioMainContainer;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioFormButtonEvent;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioRowMenuEvent;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioSearchStartEvent;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioViewButtonEvent;
import fi.vm.sade.organisaatio.ui.component.DoubleCheckbox;
import fi.vm.sade.organisaatio.ui.factory.YhteystietojenTyyppiTreeAdapter;
import fi.vm.sade.organisaatio.ui.listener.MaaChangedListener;
import fi.vm.sade.organisaatio.ui.listener.event.MaaChangedEvent;
import fi.vm.sade.organisaatio.ui.organisaatio.YhteystietojenTyyppiForm;
import fi.vm.sade.vaadin.Oph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWindow extends PortletRole implements HttpServletRequestListener, ApplicationContext.TransactionListener {

    private static final Logger LOG = LoggerFactory.getLogger(MainWindow.class);

    private static final long serialVersionUID = -3886850193406940369L;
    //A quick fix: language code 'fi_FI' is not supported by KoodistoHelper.getKieliForLocale method.
    private static final String DEFAULT_LOCALE = "FI";
    private final Blackboard blackboardInstance = new Blackboard();
    private static ThreadLocal<Blackboard> blackboard = new ThreadLocal<Blackboard>();
    private Locale sessionLocale;
    private Window window;
    private HorizontalLayout lisatietoLayout;
    private SearchableTree<YhteystietojenTyyppiDTO> lisatietoTree;
    private YhteystietojenTyyppiDTO oldSelectedLtd;
    private YhteystietojenTyyppiDTO selectedLtd;
    private boolean dataChanged = false;
    
    public void setDataChanged(boolean dataChanged) {
    	this.dataChanged = dataChanged;
    }
    
    public MainWindow() {
    }

    @Override
    public void init() {
        super.init();

        //sessionLocale = new Locale(DEFAULT_LOCALE);
        //log.info("init(), current locale: " + I18N.getLocale() + ", reset to session locale: " + sessionLocale);
        //setLocale(sessionLocale);

        //Init blackboard
        blackboard.set(blackboardInstance);
        //blackboardInstance.register(TableSingleValueListener.class, YtjSelectedEventImpl.class);
        blackboardInstance.register(MaaChangedListener.class, MaaChangedEvent.class);
        blackboardInstance.register(OrganisaatioSearchStartEvent.OrganisaatioSearchStartEventListener.class, OrganisaatioSearchStartEvent.class);
        blackboardInstance.register(OrganisaatioRowMenuEvent.OrganisaatioRowMenuEventListener.class, OrganisaatioRowMenuEvent.class);
        blackboardInstance.register(OrganisaatioFormButtonEvent.OrganisaatioFormButtonEventListener.class, OrganisaatioFormButtonEvent.class);
        blackboardInstance.register(OrganisaatioViewButtonEvent.OrganisaatioViewButtonEventListener.class, OrganisaatioViewButtonEvent.class);
        // blackboardInstance.enableLogging();
        getContext().addTransactionListener(this);

        //         //init application components
        window = new Window(getWindowCaption());
        setTheme(Oph.THEME_NAME);
        window.addComponent(createRootComponent());

        // the Refresher polls automatically every 30 seconds, this allows really short session times to minimize session size / memory usage
        window.addComponent(createRefersh("created in MainWindow.init()"));

        setMainWindow(window);
    }

    /**
     *  Application calls this method in init method.
     */
    protected Component createRootComponent() {
        throw new NotImplementedException("This method needs to be overridden.");
    }

    /**
     * @return the window caption
     */
    private String getWindowCaption() {
        return I18N.getMessage("organisaatio.title");
    }

    protected Component createOrganisaatioMainContainer() {
        return new OrganisaatioMainContainer();
    }

    protected Component createYhteystietoView() {
        // final HorizontalLayout layout = new HorizontalLayout();
        lisatietoLayout = new HorizontalLayout();
        lisatietoLayout.setSizeFull();

        // Create refresher to keep session alive, NOTE! don't add this, seems thay init() gets called anyway... somhow...
        // lisatietoLayout.addComponent(createRefersh("MainWindow.createYhteystietoView()"));

        //Creating the yhteystietojen tyyppien määrittely form, with and emtpy model.
        final YhteystietojenTyyppiForm oltForm = new YhteystietojenTyyppiForm(new YhteystietojenTyyppiDTO(), null, this);
        oltForm.init();
        oltForm.setWidth("100%");
        addListeners(oltForm);        
        /*
         * The tree for searching and displaying yhteystietojen tyyppis. By
         * clicking on an entry in the tree the form Yhteystietojen tyyppien
         * määrittely form opens and displays the metadata of the selected
         * yhteystietojen tyyppi.
         */
        lisatietoTree = new SearchableTree<YhteystietojenTyyppiDTO>("olt_",
                new YhteystietojenTyyppiTreeAdapter(organisaatioService));
        lisatietoTree.getTree().setCaption(I18N.getMessage("Hakupuu.lblPuu"));
        lisatietoTree.getSearchBox().setCaption(I18N.getMessage("Hakupuu.lblHaku"));
        lisatietoTree.addStyleName(StyleNames.SECONDARY_CONTAINER);
        lisatietoTree.init();
        lisatietoTree.getSearchBox().setInputPrompt(I18N.getMessage("Hakupuu.lblHakuPrompt"));
        lisatietoTree.reload();
        oltForm.setSearchableTree(lisatietoTree);

        lisatietoTree.getTree().addListener(new ItemClickEvent.ItemClickListener() {
            private static final long serialVersionUID = -2318797984292753676L;

            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
            	oldSelectedLtd = selectedLtd;
            	selectedLtd = (YhteystietojenTyyppiDTO) itemClickEvent.getItemId();
            	
            	if (dataChanged) {
	                final Window confirmDialog = new Window(I18N.getMessage("Hakupuu.saveConfirmDialog.header"));
	                confirmDialog.setHeight(260, Sizeable.UNITS_PIXELS);
	                confirmDialog.setWidth(380, Sizeable.UNITS_PIXELS);
	                confirmDialog.setModal(true);
	                
	                Label label = new Label(I18N.getMessage("Hakupuu.saveConfirmDialog.bodyText"));
	                confirmDialog.addComponent(label);
	
	                Label spacing1 = new Label("");
	                spacing1.setHeight(20, Sizeable.UNITS_PIXELS);
	                confirmDialog.addComponent(spacing1);
	
	                HorizontalLayout buttonLayout = new HorizontalLayout();
	
	                Button yesButton = new Button(I18N.getMessage("Hakupuu.saveConfirmDialog.yesButton"), new Button.ClickListener() {
	                    public void buttonClick(Button.ClickEvent event) {
	                    	dataChanged = false;
	                    	window.removeWindow(confirmDialog);
	                        reInitializeLisatietoForm(selectedLtd);
	                    }
	                });
	                buttonLayout.addComponent(yesButton);
	
	                Label spacing2 = new Label("");
	                spacing2.setWidth(20, Sizeable.UNITS_PIXELS);
	                buttonLayout.addComponent(spacing2);
	
	                Button noButton = new Button(I18N.getMessage("Hakupuu.saveConfirmDialog.noButton"), new Button.ClickListener() {
	                    public void buttonClick(Button.ClickEvent event) {
	                    	window.removeWindow(confirmDialog);
	                    	lisatietoTree.getTree().select(oldSelectedLtd);
	                    	selectedLtd = oldSelectedLtd;
	                    }
	                });
	                buttonLayout.addComponent(noButton);
	
	                confirmDialog.addComponent(buttonLayout);
	
	                window.addWindow(confirmDialog);
            	} else {
            		dataChanged = false;
            		reInitializeLisatietoForm(selectedLtd);
            	}
            }
        });

        //Adding the search tree to the custom layout
        lisatietoLayout.addComponent(lisatietoTree);

        //Adding the yhteystietojen tyyppien määrittely form to the custom layout.
        lisatietoLayout.addComponent(oltForm);
        lisatietoLayout.setExpandRatio(lisatietoTree, 1.0f);
        lisatietoLayout.setExpandRatio(oltForm, 3.0f);

        return lisatietoLayout;
    }

	public void addListeners(final YhteystietojenTyyppiForm oltForm) {
		oltForm.getNimiFi().addListener(new TextChangeListener() {
            public void textChange(TextChangeEvent event) {
            	dataChanged = true;
            }
        });
        oltForm.getNimiSv().addListener(new TextChangeListener() {
            public void textChange(TextChangeEvent event) {
            	dataChanged = true;
            }
        });
        
        oltForm.getNimitiedotOptions().getUsed().addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				dataChanged = true;
			}
        });
        oltForm.getNimitiedotOptions().getObligatory().addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				dataChanged = true;
			}
        }); 
        
        addDoubleCheckboxListeners(oltForm.getOsoiteOptions().getSelectOptions());
        addDoubleCheckboxListeners(oltForm.getPuhelinOptions().getSelectOptions());
        addDoubleCheckboxListeners(oltForm.getSahkoisetYtOptions().getSelectOptions());
        addDoubleCheckboxListeners(oltForm.getMuutOsoitteet().getMuutYhteystiedot());
        addDoubleCheckboxListeners(oltForm.getMuutPuhelimet().getMuutYhteystiedot());
        addDoubleCheckboxListeners(oltForm.getMuutSahkoiset().getMuutYhteystiedot());
        oltForm.getSovellettavatOrganisaatiotyypitOptions().getSovellettavatOrganisaatiotyypitStart().addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dataChanged = true;
			}
        }); 
        oltForm.getSovellettavatOrganisaatiotyypitOptions().getSovellettavatOrganisaatiotyypitEnd().addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dataChanged = true;
			}
        });       
        oltForm.getSovellettavatOrganisaatiotyypitOptions().getKoodistoOppilaitostyypit().addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dataChanged = true;
			}
        });    
	}

	private void addDoubleCheckboxListeners(
			List<DoubleCheckbox> doubleCheckboxes) {
		for (DoubleCheckbox curDC : doubleCheckboxes) {
            curDC.getUsed().addListener(new Button.ClickListener() {
    			@Override
    			public void buttonClick(ClickEvent event) {
    				dataChanged = true;
    			}
            });
            curDC.getObligatory().addListener(new Button.ClickListener() {
    			@Override
    			public void buttonClick(ClickEvent event) {
    				dataChanged = true;
    			}
            });            
        }
	}

    public void reInitializeLisatietoForm(YhteystietojenTyyppiDTO modelDTO) {
        final YhteystietojenTyyppiForm oltForm1 = new YhteystietojenTyyppiForm(modelDTO, lisatietoTree, this);
        oltForm1.init();
        dataChanged = false;
        addListeners(oltForm1);
        oltForm1.setWidth("100%");
        lisatietoLayout.removeComponent(lisatietoLayout.getComponent(1));
        lisatietoLayout.addComponent(oltForm1);
        lisatietoLayout.setExpandRatio(lisatietoTree, 1.0f);
        lisatietoLayout.setExpandRatio(oltForm1, 3.0f);
    }

    protected String getParameter(Object req, String name) {
        HttpServletRequest request = (HttpServletRequest) req;
        return request.getParameter(name);
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
    }

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        super.onRequestStart(request, response);

        String langParam = getParameter(request, "lang");
        if (langParam != null) {
            sessionLocale = new Locale(langParam);
            setLocale(sessionLocale);
        }
    }

    @Override
    public void transactionStart(Application application, Object transactionData) {
        super.transactionStart(application, transactionData);

        if (application == this) {
            blackboard.set(blackboardInstance);
        }
    }

    @Override
    public void transactionEnd(Application application, Object transactionData) {
        super.transactionEnd(application, transactionData);

        if (application == this) {
            blackboard.remove();
        }
    }

    public static Blackboard getBlackboard() {
        return blackboard.get();
    }

    @Override
    protected void registerListeners(Blackboard blackboard) {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() {
        super.close();
        getContext().removeTransactionListener(this);
    }


    /**
     * Helper to create simple refresher component to keep session alive.
     *
     * @return
     */
    private Component createRefersh(final String id) {
        LOG.debug("createRefresh()");
        final Refresher refresher = new Refresher();
        refresher.setRefreshInterval(1000L * 30L);
        refresher.addListener(new RefreshListener() {
            public void refresh(Refresher source) {
                LOG.debug("refresher() - id = {}", id);
            }
        });

        return refresher;
    }
}
