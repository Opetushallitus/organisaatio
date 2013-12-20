package fi.vm.sade.organisaatio.revised.ui.event;

import com.vaadin.ui.Component;

public class KuvausEvent extends Component.Event {
    
    public static final String SAVE = "save";
    
    private static final long serialVersionUID = -5378924367098852613L;

    private String type;
    
    public KuvausEvent(Component source, String type) {
        super(source);
        this.setType(type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

   
    
}
