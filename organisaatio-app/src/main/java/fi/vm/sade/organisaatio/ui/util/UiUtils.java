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
package fi.vm.sade.organisaatio.ui.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.component.CaptionFormatter;
import fi.vm.sade.koodisto.service.types.common.KieliType;
import fi.vm.sade.koodisto.service.types.common.KoodiMetadataType;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.koodisto.util.KoodistoHelper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.formbinder.PropertyId;

import com.vaadin.Application;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.Window;

import fi.vm.sade.generic.common.ClassUtils;
import fi.vm.sade.generic.ui.component.DebugId;
import fi.vm.sade.organisaatio.ui.factory.FieldFactory;
import fi.vm.sade.organisaatio.ui.factory.FieldUiDefinition;
import fi.vm.sade.organisaatio.ui.factory.ListAdapter;
import fi.vm.sade.vaadin.util.UiUtil;

/**
 * @author Antti Salonen
 */
public final class UiUtils extends UiUtil {

    private static final Logger LOG = LoggerFactory.getLogger(UiUtils.class);
    private static ListAdapter emptyListAdapter = new ListAdapter() {
        @Override
        public Collection findObjects(Class parentType, Class forType, String field) {
            return new ArrayList();
        }
    };

    private UiUtils() {
    }

    public static String getKoodiNameForLanguage(KoodiType koodiType,Locale locale) {
        KoodiMetadataType kmdt = KoodistoHelper.getKoodiMetadataForLanguage(koodiType, KoodistoHelper.getKieliForLocale(locale));
        if (kmdt == null) {
            kmdt = KoodistoHelper.getKoodiMetadataForLanguage(koodiType, KieliType.FI);
        }
        return kmdt.getNimi() != null ? kmdt.getNimi() : null;
    }

    public static CaptionFormatter getDefaultCaptionFormatter(final Locale locale) {
      return  new CaptionFormatter() {
            @Override
            public String formatCaption(Object dto) {
                if (dto instanceof KoodiType) {
                    KoodiType koodi = (KoodiType)dto;
                    return UiUtils.getKoodiNameForLanguage(koodi, locale);
                } else {
                    return null;
                }
            }
        } ;
    }

    public static void processDebugIds(Object object, String debugIdPrefix) {
        if (debugIdPrefix == null) {
            debugIdPrefix = "";
        }
        List<Field> fields = ClassUtils.getDeclaredFields(object.getClass());
        for (Field field : fields) {
            DebugId debugId = field.getAnnotation(DebugId.class);
            if (debugId != null) {
                field.setAccessible(true);
                try {
                    Component component = (Component) field.get(object);
                    String id = debugIdPrefix + debugId.id();
                    component.setDebugId(id);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Show new window as modal "dialog". Window is shown as a child of
     * applications main window.
     *
     * @param showFrom who is creating this window
     * @param dialogCaption
     * @param component
     * @return newly created window
     */
    public static Window showDialog(Component showFrom, String dialogCaption, Component component) {
        return showDialog(showFrom.getApplication(), dialogCaption, component);
    }

    /**
     * Show new window as modal "dialog". Window is shown as a child of
     * applications main window.
     *
     * @param application
     * @param dialogCaption
     * @param component
     * @return newly created window
     */
    public static Window showDialog(Application application, String dialogCaption, Component component) {
        return showDialog(application.getMainWindow().getWindow(), dialogCaption, component);
    }

    private static Window showDialog(Window window, String dialogCaption, Component component) {
        final Window dialog = new Window(dialogCaption);
        dialog.setModal(true);
        dialog.setWidth("90%");
//        dialog.setHeight("90%");
        window.addWindow(dialog);
        // create and customize form
        dialog.addComponent(component);
        return dialog;
    }

    public static Form setFormCustomLayout(Form form, boolean separateCaptionForFields, boolean recurseToSubForms, CustomLayout layout, String parentLocation) {
        Collection<?> propertyIds = form.getItemPropertyIds();
        form.getLayout().removeAllComponents();
        if (layout.getParent() == null) { // set to main form
            form.setLayout(layout);
        }
        for (Object propertyId : propertyIds) {
            com.vaadin.ui.Field field = form.getField(propertyId);
            String location = "" + propertyId;
            if (parentLocation != null) {
                location = parentLocation + "." + location;
            }
//            String location = debugId != null ? debugId : ""+propertyId;
            //DEBUGSAWAY:LOG.debug("adding component to custom layout, id: " + debugId + ", prop: " + propertyId + ", location: " + location);
            layout.addComponent(field, location);
//            field.setSizeUndefined();

            // add also separate caption
            Label label = new Label(field.getCaption());
            layout.addComponent(label, location + ".caption");
            // move caption from field to separate label
            if (separateCaptionForFields) {
                field.setCaption(null);
            }
            label.setSizeUndefined();

            if (recurseToSubForms && field instanceof Form) {
                setFormCustomLayout((Form) field, separateCaptionForFields, recurseToSubForms, layout, location);
            }

        }
        return form;
    }

    public static CustomLayout getCustomLayout(String layoutPath) {
        CustomLayout customLayout = null;
        try {
            customLayout = new CustomLayout(Thread.currentThread().getContextClassLoader().getResourceAsStream(layoutPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return customLayout;
    }

    public static com.vaadin.ui.Field getField(Form form, String propertyIdPath) {
        String[] propIdPath = propertyIdPath.split("\\.");
        com.vaadin.ui.Field current = form;
        for (String propId : propIdPath) {
            current = ((Form) current).getField(propId);
        }
        return current;
    }

    public static void detachField(Form form, com.vaadin.ui.Field field) {
        // not sure if these all are needed
        if (field.getValidators() != null) {
            for (Validator validator : new ArrayList<Validator>(field.getValidators())) {
                form.removeValidator(validator);
                field.removeValidator(validator);
            }
        }
        form.removeItemProperty(field.getPropertyDataSource());
        field.detach();
        field.setRequired(false);
    }

    /**
     * Scans formContainer object's java fields, and if field has no value, and
     * has
     *
     * @PropertyId annotation, creates a vaadin Field component and puts it in
     * place.
     *
     * Also sets general features to vaadin Field such as: debugId, caption,
     * validator (beanvalidator based on datasource dto property annotations),
     * etc.
     *
     * @param formContainer might be any object as long as it contains vaadin
     * components as java fields
     * @param beanItem form's datasource
     * @param modelClass root model class related to field
     * @PropertyId annotations
     */
    public static void createFieldsBasedOnAnnotations(Object formContainer, BeanItem beanItem, Class modelClass) {
        List<Field> javaFields = ClassUtils.getDeclaredFields(formContainer.getClass());
        for (Field javaField : javaFields) {
            Class<?> fieldType = javaField.getType();
            if (com.vaadin.ui.Field.class.isAssignableFrom(fieldType)) {
                PropertyId propertyId = javaField.getAnnotation(PropertyId.class);
                if (propertyId != null) {
                    javaField.setAccessible(true);
                    try {
                        com.vaadin.ui.Field vaadinField = (com.vaadin.ui.Field) javaField.get(formContainer);
                        if (vaadinField == null) {
                            // javafield is a vaadin field + it is not set + it has @PropertyId annotation
                            // --> create field
                            Field modelField = null;
                            try {
                                modelField = getJavaField(modelClass, propertyId.value());
                                modelField.setAccessible(true);
                            } catch (Exception e) {
                                // quite usual case if superform has vaadin fields, but child form's model doesn't have them
                                LOG.warn("WARNING - createFieldsBasedOnAnnotations: " + e);
                            }
                            if (modelField != null) {

                                FieldUiDefinition fieldUiDefinition = new FieldUiDefinition();
                                fieldUiDefinition.setFieldClass((Class<? extends com.vaadin.ui.Field>) javaField.getType());

                                vaadinField = FieldFactory.createFieldComponent(modelField, fieldUiDefinition, propertyId.value(),
                                        emptyListAdapter);

                                LOG.info("create field based on annotation, javaField: " + javaField);//javaField.getDeclaringClass().getSimpleName()+"."+javaField.getName()+
                                //", vaadinField: "+vaadinField.getClass().getSimpleName()+" (debugId: "+vaadinField.getDebugId()+")");

                                NestedMethodProperty nestedMethodProperty = new NestedMethodProperty(beanItem.getBean(), propertyId.value());
                                beanItem.addItemProperty(propertyId.value(), nestedMethodProperty);

                                //DEBUGSAWAY:LOG.debug("\nBEANITEM PROPERTY ({}): {}; NMP: {}; bean: {}",
//                                        new Object[]{beanItem.getItemPropertyIds().size(), propertyId.value(),
//                                            nestedMethodProperty, beanItem.getBean()
//                                        });

                                javaField.set(formContainer, vaadinField);
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static Field getJavaField(Class modelClass, String propertyIdPath) {
        String[] propIdPath = propertyIdPath.split("\\.");
        Class current = modelClass;
        Field currentField = null;
        for (String propId : propIdPath) {
            currentField = ClassUtils.getDeclaredField(current, propId);
            current = currentField.getType();
        }
        return currentField;
    }

    public static void addAnnotatedFieldsToCustomLayout(Object formContainer, Form form, CustomLayout layout) {
        List<Field> javaFields = ClassUtils.getDeclaredFields(formContainer.getClass());
        for (Field javaField : javaFields) {
            Class<?> fieldType = javaField.getType();
            if (com.vaadin.ui.Field.class.isAssignableFrom(fieldType)) {
                PropertyId propertyId = javaField.getAnnotation(PropertyId.class);
                if (propertyId != null) {
                    javaField.setAccessible(true);
                    try {
                        com.vaadin.ui.Field vaadinField = (com.vaadin.ui.Field) javaField.get(formContainer);
                        if (vaadinField != null) {
                            LOG.info("add annotated field to custom layout, propertyId: "
                                    + propertyId.value() + ", field: " + vaadinField.getClass().getSimpleName());
                            layout.addComponent(vaadinField, propertyId.value());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }


    public static void configureOptions(final Select select, Field field, final Class targetClazzCandidate,
            final String targetProperty, final String itemProperty, boolean dynamic, final ListAdapter listAdapter1) {

        final String fieldName = field.getName();
        final Class declaringClass = field.getDeclaringClass();

        boolean targetClassDefined = targetClazzCandidate != Object.class;
        final Class targetClazz = targetClassDefined ? targetClazzCandidate : field.getType();

        boolean targetPropertyDefined = !targetProperty.equals("");
        final boolean useObjectAsOptionValue = !targetPropertyDefined;

        // configure selectbox options, dynamic or static
        if (!dynamic) {
            configureSelectOptions(select, fieldName, listAdapter1, targetClazz, targetProperty, itemProperty, declaringClass,
                    useObjectAsOptionValue);
        } else {
            // set selectbox to load options when repainting
            select.addListener(new FieldEvents.FocusListener() {
                @Override
                public void focus(FieldEvents.FocusEvent focusEvent) {
                    configureSelectOptions(select, fieldName, listAdapter1, targetClazz, targetProperty, itemProperty, declaringClass,
                            useObjectAsOptionValue);
                }
            });
        }
    }

    public static void configureSelectOptions(Select select, String fieldName, ListAdapter listAdapter1, Class targetClazz,
            String targetProperty, String itemProperty, Class<?> declaringClass, boolean useObjectAsOptionValue) {
        select.removeAllItems();

        //DEBUGSAWAY:LOG.debug(" *** declaringClass: " + declaringClass + ", targetClass: " + targetClazz + ", useObjectAsOptionValue: " + useObjectAsOptionValue
//                + ", targetProp: " + targetProperty + ", targetProp: " + targetProperty + ", itemProp: " + itemProperty + " ,fieldName: " + fieldName);

        Collection options = listAdapter1.findObjects(declaringClass, targetClazz, fieldName);
        //DEBUGSAWAY:LOG.debug("options: " + options);

        // add options and their captions
        for (Object option : options) {
            Object optionId = option;
            if (!useObjectAsOptionValue) { // if field value is not same as option (eg value is Long id, and option is custom object)
                try {
                    optionId = PropertyUtils.getProperty(option, targetProperty);
                    //DEBUGSAWAY:LOG.debug("    OPTIONID: " + optionId + ", option: " + option);
                } catch (Exception e) {
                    e.printStackTrace(); // TODO: poikkarit
                }
            }
            select.addItem(optionId);
            // set caption
            if (!"".equals(itemProperty)) {
                try {
                    String itemCaption = BeanUtils.getProperty(option, itemProperty);
                    select.setItemCaption(optionId, itemCaption);
                } catch (Exception e) {
                    e.printStackTrace(); // TODO: logging, ei kuitenkaan runtimee kannattane heittää
                }
            }
        }
    }

    public static void configureSelect(final Select select, boolean required, boolean autoComplete) {

        // set options filtering if autocomplete=true
        if (autoComplete) {
            select.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
            select.setImmediate(true);
        }

        // deny null selection if field is required
        select.setNullSelectionAllowed(!required);
    }

    public static String getText(String keyPostfix) {
//        String key = baseTextKey+"."+keyPostfix;
        String key = keyPostfix;
        try {
//            String text = helper.getText(key);
            String text = fi.vm.sade.generic.common.I18N.getMessage(key);
            if (text == null) {
                LOG.warn("WARNING - GuiFactory.getText got null text, key: " + key);
                return keyPostfix;
            } else {
                return text;
            }
        } catch (Exception e) {
            LOG.warn("WARNING - GuiFactory.getText failed to get text, key: " + key + ", exception: " + e);
            return keyPostfix;
        }
    }

    public static boolean classResourceExists(String name) {
        ClassLoader loader = UiUtils.class.getClassLoader();
        return (loader.getResource(name) != null);
    }

    public static CssLayout newPopupCssLayout(Component layout) {
        CssLayout cssLayout = new CssLayout();
        cssLayout.setMargin(true);
        cssLayout.setSizeFull();
        cssLayout.addComponent(layout);
        return cssLayout;
    }
}
