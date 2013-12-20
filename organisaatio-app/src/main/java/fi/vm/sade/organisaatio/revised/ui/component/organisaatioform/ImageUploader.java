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
package fi.vm.sade.organisaatio.revised.ui.component.organisaatioform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.common.I18NHelper;
import fi.vm.sade.organisaatio.ui.model.OrganisaatioKuvaModel;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.vaadin.util.UiUtil;

/**
 * The image upload and view component.
 * @author Markus
 *
 */
class ImageUploader extends VerticalLayout implements Upload.SucceededListener,
                                                             Upload.FailedListener,
                                                             Upload.ProgressListener,
                                                             Upload.Receiver {
    

    private static final long serialVersionUID = 1L;
    
    private static final int MAX_SIZE = 200000; //200kB

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * The vaadin file upload component
     */
    private Upload uploader;
    
    /**
     * The panel to display the image.
     */
    private Panel imagePanel;
    
    /**
     * The stream for the file.
     */
    private ByteArrayOutputStream outStream = new ByteArrayOutputStream(10240);
    
    /**
     * The image file name
     */
    private String filename;
    
    /**
     * The mime type of the image file.
     */
    private String mimeType;
    
    /**
     * The image
     */
    private Embedded image;
    
    /**
     * The imate data
     */
    private byte[] data;
    
    /**
     * The image model.
     */
    private OrganisaatioKuvaModel model;
    
    private I18NHelper i18n = new I18NHelper(this);
    
    /**
     * The main layout to which the component is added.
     */
    private GridLayout mainLayout;
    
    private Property.ValueChangeListener changeListener;
    
    
    ImageUploader(OrganisaatioKuvaModel model, GridLayout layout, Property.ValueChangeListener listener) {
        super();
        this.changeListener = listener;
        this.mainLayout = layout;
        this.model = model;
        data = this.model.getKuva();
        filename = this.model.getFileName();
        mimeType = this.model.getMimeType();
        buildLayout();
    }
    
    
    /**
     * Attaching the image to the ui.
     */
    void attachImage() {
        if (data != null) {
            imagePanel.removeAllComponents();
            StreamResource.StreamSource source = new StreamResource.StreamSource() {

                private static final long serialVersionUID = 1L;

                @Override
                public InputStream getStream() {
                    return new ByteArrayInputStream(data);
                }
            };
            
            StreamResource resource = new StreamResource(source, filename, mainLayout.getApplication());
            
            image = new Embedded(filename);
            image.setSizeUndefined();
            image.setWidth("250px");
            image.setSource(resource);
            imagePanel.addComponent(image);
            image.requestRepaint();
        } 
    }
    
    private void clearImage() {
        imagePanel.removeAllComponents();
        imagePanel.addComponent(new Label(T("noImageYet")));
        model.setKuva(null);
        model.setMimeType(null);
        model.setFileName(null);
    }
    
    /**
     * Building the ui layout for the component.
     */
    private void buildLayout() {
       
        imagePanel = new Panel();
        imagePanel.setWidth("280px");
        imagePanel.addComponent(new Label(T("noImageYet")));
        mainLayout.addComponent(imagePanel, 1, 0);
        mainLayout.setComponentAlignment(imagePanel, Alignment.MIDDLE_LEFT);
        
        Label uploadLabel = UiUtil.label(null, T("uploadPrompt"));
        mainLayout.addComponent(uploadLabel, 0,1);
        mainLayout.setComponentAlignment(uploadLabel, Alignment.MIDDLE_RIGHT);
        HorizontalLayout hl = UiUtil.horizontalLayout();
        uploader = new Upload();
        uploader.setReceiver(this);
        uploader.addListener((Upload.SucceededListener)this);
        uploader.addListener((Upload.FailedListener)this);
        uploader.addListener((Upload.ProgressListener)this);
        uploader.setButtonCaption(T("Lataa"));
        hl.addComponent(uploader);
        
        UiUtil.buttonLink(hl, T("poistaKuva"), new Button.ClickListener() {

            private static final long serialVersionUID = -7947543772372919644L;

            @Override
            public void buttonClick(ClickEvent event) {
                Label changeLabel = new Label("val");
                changeLabel.addListener(changeListener);
                changeLabel.setValue("val1");
                clearImage();
                
            }
        });
        
        mainLayout.addComponent(hl, 1, 1);
    }
   
    private String T(String key, Object... args) {
        return i18n.getMessage(key, args);
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        log.debug("Receive upload!!!");
        this.filename = filename;
        this.mimeType = mimeType;
        outStream.reset();
        return outStream;
    }

    @Override
    public void uploadFailed(FailedEvent event) {
        log.error("Upload failed");
        addComponent(new Label(T("uploadFailed")));
    }

    /**
     * After the image has been uploaded the data is written to model,
     * and the image is displayed in the ui.
     */
    @Override
    public void uploadSucceeded(SucceededEvent event) {
        log.debug("Upload succeeded");
        model.setFileName(filename);
        model.setMimeType(mimeType);
        StreamResource.StreamSource source = new StreamResource.StreamSource() {
            
            private static final long serialVersionUID = 1L;

            @Override
            public InputStream getStream() {
                log.debug("Getting stream!!!");
                data = outStream.toByteArray();
                model.setKuva(data);
                return new ByteArrayInputStream(data);
            }
        };
        
        StreamResource resource = new StreamResource(source, filename, mainLayout.getApplication());
        if (resource.getMIMEType().equals("image/jpeg") || resource.getMIMEType().equals("image/gif") || resource.getMIMEType().equals("image/png")) {
	        imagePanel.removeAllComponents();
	        image = new Embedded(filename);
	        image.setWidth("250px");
	        image.setSource(resource);
	        imagePanel.addComponent(image);
	        image.requestRepaint();
	        Label label = new Label("val");
	        label.addListener(this.changeListener);
	        label.setValue("val1");
        } else {
        	mainLayout.getWindow().showNotification(T("fileMIMENotCorrect"), Window.Notification.TYPE_WARNING_MESSAGE);
        }
    }

    public byte[] getData() {
        return data;
    }


    @Override
    public void updateProgress(long readBytes, long contentLength) {
        if (contentLength > MAX_SIZE) {
            this.uploader.interruptUpload();
            mainLayout.getWindow().showNotification(T("tooLargeFile"), Window.Notification.TYPE_WARNING_MESSAGE);
            return;
        }
    }

}
