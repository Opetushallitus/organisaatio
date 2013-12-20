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

package fi.vm.sade.organisaatio.ui.map;

import com.vaadin.Application;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import fi.vm.sade.organisaatio.ui.map.exception.AddressNotFoundException;
import org.vaadin.hezamu.googlemapwidget.GoogleMap;
import org.vaadin.hezamu.googlemapwidget.overlay.BasicMarker;
import org.vaadin.hezamu.googlemapwidget.overlay.Marker;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Tuomas Katva
 */
@Deprecated // todo: poista, ei käytössä ilm, poista googlemapcomponent depsukin
public final class GoogleMapComponent extends CustomComponent {

    private VerticalLayout rootLayout = null;

    private GoogleMap mapElement = null;

    private IAddressResolver addressResolver = null;

    private List<MarkerMoveListener> callbacks = null;

    private final int defaultZoomLevel = 16;

    private final int defaultHighZoomLevel = 4;

    private final String mapElementId = "gMapComponent";

    private final double defaultLat = 60.197296142578125;

    private final double defaultLng = 24.950767517089844;

    private BasicMarker locationMarker = null;

    private GmapDTO location =  null;

    public GoogleMapComponent() {

    }

    public void initMap(GmapDTO locationParam,String widthParam,String heightParam, Application applParam) throws AddressNotFoundException {

        // Check if the location is null, then show only high level view without marker.
        if (locationParam == null) {
           GmapDTO locationPrm = new GmapDTO();
           locationPrm.setAddMarker(false);
           locationPrm.setZoomLevel(defaultHighZoomLevel);
           //Default lat and lng so that map centers on Finland
           locationPrm.setLat(defaultLat);
           locationPrm.setLng(defaultLng);
           createMap(locationPrm,widthParam,heightParam,applParam);
           //Check if lat and lng is given, if not then try resolve address using geolocation API
        } else if (locationParam.getLat() ==  null || locationParam.getLng() == null) {
            locationParam.setAddMarker(true);
            createMap(resolveAddress(locationParam),widthParam,heightParam,applParam);
        } else {
        locationParam.setAddMarker(true);
        createMap(locationParam,widthParam,heightParam,applParam);
        }
    }

    private int checkZoomLevel(GmapDTO locationParam) {
        if (locationParam.getZoomLevel() != 0) {
            return locationParam.getZoomLevel();
        } else {
            return defaultZoomLevel;
        }
    }

    private GmapDTO resolveAddress(GmapDTO mapParam) throws AddressNotFoundException {
        if (addressResolver == null) {
            throw new AddressNotFoundException("Address resolver not specified");
        }

        return addressResolver.resolveAddress(mapParam);

    }

    //Construct Google map component with or without Google API key
    private GoogleMap constructMapElement(GmapDTO mapSpecParam, Application applParam) {
        if (mapSpecParam.getGoogleApiKey() != null) {
           return new GoogleMap(getApplication(), mapSpecParam.getGoogleApiKey());
        } else {
            Application appl = getApplication();
            if (appl == null) {
                appl = applParam;
            }
            return new GoogleMap(appl);
        }
    }

    public void addMarkerMoveListener(MarkerMoveListener markerMove) {
        if (callbacks == null) {
            callbacks = new ArrayList<MarkerMoveListener>();
        }
        callbacks.add(markerMove);
    }

    public GmapDTO getCurrentLocation() {

        if (locationMarker != null) {

            location.setLat(new Float(locationMarker.getLatLng().getY()));
            location.setLng(new Float(locationMarker.getLatLng().getX()));
       }

        return location;
    }

    private void callCallbacks(GmapDTO locationParam) {

        location.setLat(locationParam.getLat());
        location.setLng(locationParam.getLng());
        if (callbacks != null) {
        for (MarkerMoveListener mml : callbacks) {
            mml.markerMoved(locationParam);
        }
        }
    }

    public void moveMarker(GmapDTO locationParam) throws AddressNotFoundException {
        //If marker is placed then remove it.
        if (locationMarker != null) {
            mapElement.removeMarker(locationMarker);
        }
        //Check if lat and lng must be resolved
        if (locationParam.getStreet() != null && locationParam.getCity() != null && locationParam.getCountryCode() != null) {
        location = resolveAddress(locationParam);
        }
        //Center map to coordinates and place marker
        mapElement.setCenter(new Point2D.Double(location.getLng(),location.getLat()));
        mapElement.setZoom(checkZoomLevel(locationParam));
        locationMarker = new BasicMarker(1L,new Point2D.Double(location.getLng(),location.getLat()),"");
        mapElement.addMarker(locationMarker);
    }

    public void createMap(GmapDTO locationParam,String widthParam,String heightParam, Application appl) {
        //If map is defined, remove it and paint it again
        if (rootLayout != null && mapElement != null) {
            rootLayout.removeComponent(mapElement);
        }
        location = locationParam;
        rootLayout = new VerticalLayout();
        setCompositionRoot(rootLayout);
        mapElement = constructMapElement(locationParam,appl);
        mapElement.setDebugId(mapElementId);
        rootLayout.addComponent(mapElement);
        mapElement.setZoom(checkZoomLevel(locationParam));
        mapElement.setCenter(new Point2D.Double(locationParam.getLng(),locationParam.getLat()));

        mapElement.setWidth(widthParam);
        mapElement.setHeight(heightParam);

        locationMarker = new BasicMarker(1L,new Point2D.Double(locationParam.getLng(),locationParam.getLat()),"");

        mapElement.addMarker(locationMarker);
        /* This must be done even when just high level view is wanted.
        * First draw the marker and then remove it, otherwise marker is not shown
        * when later trying to draw it.
        */
        if (!locationParam.isAddMarker()) {
            mapElement.removeMarker(locationMarker);
        }
        mapElement.addListener(new GoogleMap.MarkerMovedListener() {

            @Override
            public void markerMoved(Marker movedMarker) {
                GmapDTO location = getCurrentLocation();
                callCallbacks(location);
            }
        });

    }

    /**
     * @return the addressResolver
     */
    public IAddressResolver getAddressResolver() {
        return addressResolver;
    }

    /**
     * @param addressResolver the addressResolver to set
     */
    public void setAddressResolver(IAddressResolver addressResolver) {
        this.addressResolver = addressResolver;
    }
}
