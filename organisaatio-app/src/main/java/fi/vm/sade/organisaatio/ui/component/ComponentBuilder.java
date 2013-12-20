package fi.vm.sade.organisaatio.ui.component;

import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.*;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.component.CaptionFormatter;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.koodisto.widget.KoodistoComponent;
import fi.vm.sade.koodisto.widget.WidgetFactory;
import fi.vm.sade.organisaatio.ui.util.UiUtils;

/**
 * Build vaadin components with Builder pattern
 *
 * @author Antti
 */
public class ComponentBuilder {

    public static KoodistoComboboxBuilder koodistoCombobox(String uri) {
        return new KoodistoComboboxBuilder(uri);
    }

    static TextFieldBuilder textField() {
        return new TextFieldBuilder();
    }
    
    static OptionGroupBuilder koodistoOptionGroup(String uri) {
        return new OptionGroupBuilder(uri);
    }

    static abstract class Builder<COMP extends Component, SELF extends Builder> {
        private String width;
        String inputPromptKey;
        private String captionKey;
        private String debugId;
        private NestedMethodProperty dataSource;
        private CustomLayout layout;
        protected String location;

        public abstract COMP doBuild();

        public final COMP build() {
            COMP component = doBuild();
            if (width != null) {
                component.setWidth(width);
            }
            if (captionKey != null) {
                component.setCaption(I18N.getMessage(captionKey));
            }
            if (debugId != null) {
                getField(component).setDebugId(debugId);
            }
            if (inputPromptKey != null) {
                setInputPrompt(component);
            }
            if (layout != null && location != null) {
                layout.addComponent(component, location);
            }
            if (dataSource != null) {
                getField(component).setPropertyDataSource(dataSource);
            }
            return component;
        }

        protected abstract Field getField(COMP component);

        protected abstract void setInputPrompt(COMP component);

        public SELF withWidth(String width) {
            this.width = width;
            return (SELF) this;
        }

        public SELF withInputPrompt(String inputPromptKey) {
            this.inputPromptKey = inputPromptKey;
            return (SELF) this;
        }

        public SELF withCaption(String captionKey) {
            this.captionKey = captionKey;
            return (SELF) this;
        }

        public SELF withDebugId(String debugId) {
            this.debugId = debugId;
            return (SELF) this;
        }

        public SELF withDataSource(Object object, String property) {
            dataSource = new NestedMethodProperty(object, property);
            return (SELF) this;
        }

        public SELF toLayout(CustomLayout layout, String location) {
            this.layout = layout;
            this.location = location;
            return (SELF) this;
        }
    }

    public static class KoodistoComboboxBuilder extends Builder<KoodistoComponent, KoodistoComboboxBuilder> {
        private final String uri;
        private CaptionFormatter captionFormatter;
        private boolean immediate;

        public KoodistoComboboxBuilder(String uri) {
            this.uri = uri;
        }

        public KoodistoComponent doBuild() {
            KoodistoComponent component = WidgetFactory.create(uri);
            if (captionFormatter != null) {
                component.setCaptionFormatter(captionFormatter);
            }
            ComboBox combo = new ComboBox();
            combo.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
            component.setField(combo);
            if (immediate) {
                combo.setImmediate(true);
            }
            
            return component;
        }

        @Override
        protected Field getField(KoodistoComponent component) {
            return component.getField();
        }

        public KoodistoComboboxBuilder withArvoCaption() {
            captionFormatter = new CaptionFormatter<KoodiType>() {
                @Override
                public String formatCaption(KoodiType koodiDTO) {
                    return koodiDTO.getKoodiArvo();
                }
            };
            return this;
        }

        @Override
        protected void setInputPrompt(KoodistoComponent component) {
            if (component.getField() instanceof ComboBox) {
                ((ComboBox) component.getField()).setInputPrompt(UiUtils.getText(inputPromptKey));
            }
        }

        public KoodistoComboboxBuilder withImmediate(boolean immediate) {
            this.immediate = immediate;
            return this;
        }

    }

    public static class TextFieldBuilder extends Builder<TextField, TextFieldBuilder> {
        @Override
        public TextField doBuild() {
            TextField field = new TextField();
            field.setNullRepresentation("");
            return field;
        }

        @Override
        protected void setInputPrompt(TextField component) {
            component.setInputPrompt(UiUtils.getText(inputPromptKey));
        }

        @Override
        protected Field getField(TextField component) {
            return component;
        }
    }
    
    public static class OptionGroupBuilder extends Builder<KoodistoComponent, OptionGroupBuilder> {
        
        private String uri;
        private CaptionFormatter captionFormatter;
        
        public OptionGroupBuilder(String uri) {
            this.uri = uri;
        }
        
        @Override    
        public KoodistoComponent doBuild() {
            KoodistoComponent component = WidgetFactory.create(uri, true);
            component.setReadOnly(false);
            if (captionFormatter != null) {
                component.setCaptionFormatter(captionFormatter);
            }
            OptionGroup checkboxes = new OptionGroup("");
            checkboxes.setMultiSelect(true);
            checkboxes.setEnabled(true);
            checkboxes.setReadOnly(false);
            component.setField(checkboxes);
            
            return component;
        }
        
        @Override
        protected void setInputPrompt(KoodistoComponent component) {
            if (component.getField() instanceof ComboBox) {
                ((ComboBox) component.getField()).setInputPrompt(UiUtils.getText(inputPromptKey));
            }
        }
        
        @Override
        protected Field getField(KoodistoComponent component) {
            return component.getField();
        }
    }
}
