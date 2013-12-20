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

package fi.vm.sade.organisaatio.ui.organisaatio;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.*;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.common.validation.ValidationConstants;
import fi.vm.sade.generic.ui.CustomBeanValidationValidator;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.organisaatio.api.model.types.*;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;
import fi.vm.sade.organisaatio.ui.component.OsoiteField;
import fi.vm.sade.organisaatio.ui.component.OsoiteUlkomaaField;
import fi.vm.sade.organisaatio.revised.ui.component.organisaatioform.OrganisaatioEditForm;

import java.util.*;

/**
 * Dynamically created fields to edit "lisatiedot" of one "organisaatio".
 *
 * <p>Usage: <br/>
 * <pre>
 * YhteystietojenTyyppiEditor e = new YhteystietojenTyyppiEditor(parentForm);
 * EditorModel model = new EditorModel(fieldMetadata);
 * model.addValues(fieldValues);
 * e.populate(model);
 * </pre> </p>
 *
 *
 * @author jsr
 */
public class YhteystietojenTyyppiEditor extends CustomComponent {

    private List<Field> fields;
    private Map<Field,String> fieldGroupMap = new HashMap<Field,String>();
    private Map<YhteystietoArvoDTO,String> arvoGroupMap = new HashMap<YhteystietoArvoDTO,String>();


    private Form form;
    private VerticalLayout mainLayout;
    private OrganisaatioEditForm organisaatioForm;

    public YhteystietojenTyyppiEditor(Form form, OrganisaatioEditForm organisaatioForm) {
        this.form = form;
        this.organisaatioForm = organisaatioForm;
        fields = new ArrayList<Field>();
        initLayout();
    }

    private void initLayout() {
        mainLayout = new VerticalLayout();
        mainLayout.setSizeUndefined();
        
        setCompositionRoot(mainLayout);
    }

    /**
     * Creates data entry widgets based on given metadata.
     *
     * @param model
     * @param listener
     */
    public void populate(EditorModel model, Property.ValueChangeListener listener) {

        clear();

        List<YhteystietojenTyyppiDTO> groups = model.groups;
        int numGroups = groups.size();

        // loop all "lisatieto" groups. each group contains a name
        // that is added as label and one or more data entry fields
        for (int groupIndex = 0; groupIndex < numGroups; groupIndex++) {
            
            FormLayout formLayout = new FormLayout();
            YhteystietojenTyyppiDTO group = groups.get(groupIndex);

            String groupCapt = OrganisaatioDisplayHelper.getYttCaption(I18N.getLocale(), group);
            // todo: locale?
            Label label = new Label(OrganisaatioDisplayHelper.getYttCaption(I18N.getLocale(), group));
            mainLayout.addComponent(label);

            List<YhteystietoElementtiDTO> list = group.getAllLisatietokenttas();
            int numFields = ((list != null)
                ? list.size()
                : 0);

            // loop all data entry fields creating input field(s) for each
            for (int fieldIndex = 0; fieldIndex < numFields; fieldIndex++) {
                
                final YhteystietoElementtiDTO field = list.get(fieldIndex);
                if (field != null) {
                    final String fieldId = field.getOid();
                
                    YhteystietoArvoDTO value = model.valuesMap.get(fieldId);

                    if (value == null) {
                        throw new IllegalStateException("no value for field: " + field.getNimi() + " of type: "
                            + field.getTyyppi());
                    }
 
                    formLayout.addComponent(createEditor(field, model.valuesMap.get(fieldId), listener, groupCapt));
                }
            }

            mainLayout.addComponent(formLayout);
        }
    }
    
    public List<Field> getFields() {
        return fields;
    }

    /**
     * Removes all fields from the form and removes other components (labels) from the main layout.
     */
    private void clear() {
        detachFields();
        mainLayout.removeAllComponents();
    }

    private void detachFields() {
        for (Field field : fields) {
            UiUtils.detachField(form, field);
        }

        fields.clear();
    }

    /**
     * Returns a map of values indexed by field id.
     *
     * @param values
     * @return
     */
    private static Map<String, YhteystietoArvoDTO> valuesAsMap(List<YhteystietoArvoDTO> values) {
        if ((values == null) || values.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        Map<String, YhteystietoArvoDTO> map = new HashMap<String, YhteystietoArvoDTO>(values.size());

        for (YhteystietoArvoDTO value : values) {
            map.put(value.getKenttaOid(), value);
        }

        return map;
    }

    private Component createEditor(YhteystietoElementtiDTO field, YhteystietoArvoDTO value, Property.ValueChangeListener listener, String groupCapt) {

        //DEBUGSAWAY:log.debug("creating editor for field: " + field + " value: " + value);

        Field editor = null;
        final YhteystietoElementtiTyyppi fieldType = field.getTyyppi();
        final String caption;

        String lang = I18N.getLocale().getLanguage().toLowerCase();
        if (lang.equals("sv") && field.getNimiSv() != null && !field.getNimiSv().isEmpty()) {
            caption = field.getNimiSv();
        } else {
            caption = field.getNimi();
        }

        this.arvoGroupMap.put(value, groupCapt);
        Property property = new NestedMethodProperty(value, "arvo");

        Validator validator = null;
        if (fieldType == YhteystietoElementtiTyyppi.EMAIL) {
            editor = new TextField();
            ((TextField)editor).setImmediate(true);
            ((TextField)editor).addListener(listener);
            ((TextField)editor).setNullRepresentation("");
            ((TextField)editor).setMaxLength(ValidationConstants.GENERIC_MAX);
            validator = new CustomBeanValidationValidator(EmailDTO.class, "email");
            property = new NestedMethodProperty((EmailDTO)(value.getArvo()), "email");
        } else if (fieldType == YhteystietoElementtiTyyppi.OSOITE || fieldType == YhteystietoElementtiTyyppi.OSOITE_ULKOMAA) {
            OsoiteField osoite;
            if (fieldType == YhteystietoElementtiTyyppi.OSOITE_ULKOMAA) {
                osoite = new OsoiteUlkomaaField();
            } else {
                osoite = new OsoiteField();
                // tätä ei saa blackboardista koska initial value on setattu jo ennenkuin tämä osoitefield luodaan
                osoite.setFieldsVisibleBasedOnMaa(organisaatioForm.getKoodistoMaa().getValue());
            }
            osoite.setImmediate(true);
            osoite.addListener(listener);
            editor = osoite;
            // note! no validator since OsoiteField already has validator
        } else if (fieldType == YhteystietoElementtiTyyppi.PUHELIN || fieldType == YhteystietoElementtiTyyppi.FAKSI) {
            editor = new TextField();
            ((TextField)editor).setImmediate(true);
            ((TextField)editor).addListener(listener);
            ((TextField)editor).setNullRepresentation("");
            ((TextField)editor).setMaxLength(ValidationConstants.GENERIC_MAX);
            validator = new CustomBeanValidationValidator(PuhelinnumeroDTO.class, "puhelinnumero");
            property = new NestedMethodProperty((PuhelinnumeroDTO)(value.getArvo()), "puhelinnumero");
        } else if (fieldType == YhteystietoElementtiTyyppi.TEKSTI) {
            editor = new TextField();
            ((TextField)editor).setImmediate(true);
            ((TextField)editor).addListener(listener);
            ((TextField)editor).setNullRepresentation("");
            ((TextField)editor).setMaxLength(ValidationConstants.GENERIC_MAX);
        } else if (fieldType == YhteystietoElementtiTyyppi.WWW) {
            editor = new TextField();
            ((TextField)editor).setImmediate(true);
            ((TextField)editor).addListener(listener);
            ((TextField)editor).setNullRepresentation("");
            ((TextField)editor).setMaxLength(255);
            validator = new CustomBeanValidationValidator(WwwDTO.class, "wwwOsoite");
            property = new NestedMethodProperty((WwwDTO)(value.getArvo()), "wwwOsoite");
            
        } else if (fieldType == YhteystietoElementtiTyyppi.NIMI || fieldType == YhteystietoElementtiTyyppi.NIMIKE) {
            editor = new TextField();
            ((TextField)editor).setImmediate(true);
            ((TextField)editor).addListener(listener);
            ((TextField)editor).setMaxLength(ValidationConstants.GENERIC_MAX);
            
        } else {
            throw new IllegalStateException("dont know how to handle type: " + fieldType
                + " for field: " + field + ", value: " + value);
        }
        editor.setCaption(caption);
        editor.setPropertyDataSource(property);
        editor.addStyleName("equalWidthCaption");
        if (editor instanceof AbstractField) {
            AbstractField abstractField = (AbstractField) editor;
            abstractField.setImmediate(true);
        }

        if (validator != null) {
            editor.addValidator(validator);
        }

        if (field.isPakollinen()) {
            editor.setRequired(true);
        }

        // todo: if (Build.isDebug()) { ... or something
        editor.setDebugId("org_lisatieto_" + field.getOid() + System.currentTimeMillis());

        form.addField(property, editor);

        // remember what we've added to form
        
        fields.add(editor);
        fieldGroupMap.put(editor, groupCapt);
        return editor;
    }
    
    public String getGroupByField(Field field) {
        if (field != null) {
            return this.fieldGroupMap.get(field);
        }
        return "";
    }
    
    public String getGroupByArvo(YhteystietoArvoDTO arvo) {
        if (arvo != null) {
            return this.arvoGroupMap.get(arvo);
        }
        return "";
    }

    /**
     * This class holds all information required to populate editor and bind created fields to data models.
     *
     */
    public static class EditorModel {

        private List<YhteystietojenTyyppiDTO> groups;
        private Map<String, YhteystietoArvoDTO> valuesMap;

        /**
         * Create new model based on given metadata information. You will need to add related values using addValue(s) methods before passing the model to
         * editors populate method.
         *
         * @param groups
         */
        private EditorModel(List<YhteystietojenTyyppiDTO> groups) {
            this.groups = groups;
            valuesMap = new HashMap<String, YhteystietoArvoDTO>();
        }

        /**
         * 
         * @param organisaatioId id of the organization for which the values will be created for.
         * @param groups list of lisatiedot metadata
         * @param values list of current values, will be updated with newly created values
         */
        public EditorModel(String organisaatioId, List<YhteystietojenTyyppiDTO> groups, List<YhteystietoArvoDTO> values) {
            
            this(groups);

            Map<String, YhteystietoArvoDTO> tmp = YhteystietojenTyyppiEditor.valuesAsMap(values);

            // add place holder for values that are missing
            for (YhteystietojenTyyppiDTO group : groups) {
                for (YhteystietoElementtiDTO field : group.getAllLisatietokenttas()) {
                    if (field != null) {
                    YhteystietoArvoDTO value = tmp.get(field.getOid());
                    if (value == null) {
                        value = createLisatietoEmptyValue(organisaatioId, field);
                        // add value into source list
                        values.add(value);
                    }
                    // add value to local hashmap
                    addValue(value);
                    }
                }
            }

        }

        /**
         * Creates a value DTO according to given field metadata.
         *
         * @param organizationId
         * @param field
         * @return
         */
        private static YhteystietoArvoDTO createLisatietoEmptyValue(String organizationId, YhteystietoElementtiDTO field) {

            YhteystietoElementtiTyyppi type = field.getTyyppi();
            Object value;
            if (type == YhteystietoElementtiTyyppi.OSOITE || type == YhteystietoElementtiTyyppi.OSOITE_ULKOMAA) {
                value = new OsoiteDTO();
                ((OsoiteDTO)value).setOsoiteTyyppi(OsoiteTyyppi.MUU);
            } else if (type == YhteystietoElementtiTyyppi.PUHELIN) {// || type == YhteystietoElementtiTyyppi.FAKSI) {
                value = new PuhelinnumeroDTO();
                ((PuhelinnumeroDTO)value).setTyyppi(PuhelinNumeroTyyppi.PUHELIN);
            } else if (type == YhteystietoElementtiTyyppi.FAKSI) {
                value = new PuhelinnumeroDTO();
                ((PuhelinnumeroDTO)value).setTyyppi(PuhelinNumeroTyyppi.FAKSI);
            } else if (type == YhteystietoElementtiTyyppi.EMAIL) {
                value = new EmailDTO();
            } else if (type == YhteystietoElementtiTyyppi.WWW) {
                value = new WwwDTO();
            }
            else {
                value = new String();
            }
            YhteystietoArvoDTO arvo = new YhteystietoArvoDTO();
            arvo.setOrganisaatioOid(organizationId);
            arvo.setKenttaOid(field.getOid());
            arvo.setArvo(value);
            return arvo;

        }

        private final void addValue(YhteystietoArvoDTO value) {
            valuesMap.put(value.getKenttaOid(), value);
        }
    }
}