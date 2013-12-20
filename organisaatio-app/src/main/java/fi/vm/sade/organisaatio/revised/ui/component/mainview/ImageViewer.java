package fi.vm.sade.organisaatio.revised.ui.component.mainview;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioKuvaTyyppi;
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
 * @author Timo Santasalo / Teknokala Ky
 */
public class ImageViewer extends CustomComponent {

	private static final long serialVersionUID = 1L;
	
	private final VerticalLayout layout = new VerticalLayout();
	
	private final Component parent;
	
	private OrganisaatioKuvaTyyppi image = null;

	public ImageViewer(Component parent) {
		super();
		setCompositionRoot(layout);
		this.parent = parent;

	}

	public void setImage(OrganisaatioKuvaTyyppi image) {
		this.image = image;
		requestRepaintAll();
	}
	
	public OrganisaatioKuvaTyyppi getImage() {
		return image;
	}
	
	@Override
	public void attach() {
		layout.removeAllComponents();

		if (image==null || image.getKuva()==null || image.getKuva().length==0) {			
			return;
		}
		
		Embedded img = new Embedded(null, new StreamResource(new StreamResource.StreamSource() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(image.getKuva());
            }
        }, image.getFileName(), parent.getApplication()));
		
		img.setSizeUndefined();
		img.setWidth("250px");
		img.setMimeType(image.getMimeType());
		
		
        
		layout.addComponent(img);
        img.requestRepaint();
        
		super.attach();	
	}
	
}
