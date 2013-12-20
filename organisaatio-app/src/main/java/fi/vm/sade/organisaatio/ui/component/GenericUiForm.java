package fi.vm.sade.organisaatio.ui.component;

import org.vaadin.addon.formbinder.ViewBoundForm;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Layout;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.component.GenericForm;
import fi.vm.sade.generic.ui.component.PreCreatedFieldsHelper;
import fi.vm.sade.generic.ui.validation.ValidatingViewBoundForm;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.organisaatio.ui.util.enums.RequiredRole;

/**
 * @author Antti Salonen
 */
public abstract class GenericUiForm<MODELCLASS> extends GenericForm<MODELCLASS> {

    public GenericUiForm(MODELCLASS model) {
        super(model);
    }

    @Override
    protected void initFields() {
        // luodaan vaadin Field oliot @PropertyId:llä annotoituihin kenttiin
        UiUtils.createFieldsBasedOnAnnotations(this, beanItem, getModelClass());
    }
    
    @Override
    protected void initForm(MODELCLASS model, Layout formLayout) {
        this.model = model;
        beanItem = new BeanItem<MODELCLASS>(model); // note that this will be re-created when re-binding with bind-method

        // luodaan form
        form = new ValidatingViewBoundForm(this);
        form.setValidationVisible(false);
        form.setValidationVisibleOnCommit(false);
        // setataan formin layout
        // NOTE! ViewBoundFormin layout pitää asettaa heti formin luonnin jälkeen, ja ennenkuin siihen laitetaan mitään komponentteja
        // NOTE! layout pitää setata ennen setFormFieldFactory, muuten fieldit ei bindaudu oikein
        if (formLayout != null) {
            form.setLayout(formLayout);
        }
        // asetetaan formille overridattu PreCreatedFieldsHelper, koska alkuperäinen ei tue perintähierarkiaa
        form.setFormFieldFactory(new PreCreatedFieldsHelper((new Object[]{
                this, form, ((ViewBoundForm) form).getCustomFieldSouces()
        })));

        // luodaan fieldit
        initFields();

        // bindataan model <-> form
        form.setItemDataSource(beanItem);

        // luodaan save nappi
        buttonSave = UiUtils.buttonSmallPrimary(null,I18N.getMessage("save"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                processSave();
            }
        });
        form.getFooter().addComponent(buttonSave);

        // add serverMessage label to ease automatic testing - TODO: pääleiskaan? piilotetuksi? ja pois tuotannosta..
        form.getFooter().addComponent(serverMessage);
    }
    
    

}
