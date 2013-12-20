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

import fi.vm.sade.organisaatio.ui.map.exception.AddressNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 * @author Tuomas Katva
 *
 *
 * TODO 30.04.2012 : Should the Google api key be passed with this ?
 */
public class AddressResolverGoogleImpl implements IAddressResolver {

    private static final String GEOCODER_REQUEST_PREFIX_FOR_XML = "http://maps.google.com/maps/api/geocode/xml";

    @Override
    public GmapDTO resolveAddress(GmapDTO addressParam) throws AddressNotFoundException {

        try {
            // query address
            String address = addressParam.getStreet() + ", " + addressParam.getCity() + ", " + addressParam.getCountryCode();

            // prepare a URL to the geocoder
            URL url = new URL(GEOCODER_REQUEST_PREFIX_FOR_XML + "?address=" + URLEncoder.encode(address, "UTF-8") + "&sensor=false");

            // prepare an HTTP connection to the geocoder
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            Document geocoderResultDocument = null;
            try {
                // open the connection and get results as InputSource.
                conn.connect();
                InputSource geocoderResultInputSource = new InputSource(conn.getInputStream());

                // read result and parse into XML Document
                geocoderResultDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(geocoderResultInputSource);
            } finally {
                conn.disconnect();
            }

            // prepare XPath
            XPath xpath = XPathFactory.newInstance().newXPath();

            // extract the result
            NodeList resultNodeList = null;



            // c) extract the coordinates of the first result
            resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/geometry/location/*", geocoderResultDocument, XPathConstants.NODESET);
            float lat = Float.NaN;
            float lng = Float.NaN;
            for (int i = 0; i < resultNodeList.getLength(); ++i) {
                Node node = resultNodeList.item(i);
                if ("lat".equals(node.getNodeName())) {
                    lat = Float.parseFloat(node.getTextContent());
                }
                if ("lng".equals(node.getNodeName())) {
                    lng = Float.parseFloat(node.getTextContent());
                }
            }
            addressParam.setLat(lat);
            addressParam.setLng(lng);


        } catch (Exception exp) {
            throw new AddressNotFoundException(exp.toString());
        }
        return addressParam;
    }
}
