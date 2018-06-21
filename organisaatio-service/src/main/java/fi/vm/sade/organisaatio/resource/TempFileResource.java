/*
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
package fi.vm.sade.organisaatio.resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * "Tilapäistiedostojen käsittely (IE9:lle)"
 */
@Path("/tempfile")
@Api(value = "/tempfile", description = "Tilapäistiedostojen käsittely (IE9:lle)")
@Component("tempfileResource")
public class TempFileResource {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private Map<String, FileItem> data = null;

    public TempFileResource() {
        data = new HashMap<>();
    }

    @POST
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Secured({"ROLE_APP_ORGANISAATIOHALLINTA"})
    @ApiOperation(value = "Lisää uuden kuvan palvelimelle.", notes = "Lisää uuden kuvan palvelimelle.",
            response = String.class)
    public String addImage(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        LOG.info("Adding attachment "+request.getMethod());
        Map<String, String> result = null;

        try {
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (isMultipart) {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);

                for (FileItem item : upload.parseRequest(request)) {
                    if (item.getName() != null) {
                        result = storeAttachment(item);
                    }
                }
            } else {
                response.setStatus(400);
                response.getWriter().append("Not a multipart request");
            }
            LOG.info("Added attachment: " + result);
            JSONObject json = new JSONObject(result);
            return json.toString();
        } catch (Exception e) {
            return "organisaatio.fileupload.error";
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{img}")
    @Secured({"ROLE_APP_ORGANISAATIOHALLINTA"})
    @ApiOperation(value = "Hakee id:tä vastaavan kuvan.", notes = "Hakee id:tä vastaavan kuvan.", response = String.class)
    public String getImage(@PathParam("img") String img) {
        LOG.info("getting " + getUser() + img);
        FileItem imgFile = data.get(getUser() + img);
        byte[] imgData = imgFile.get();
        return Base64.getEncoder().encodeToString(imgData);
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{img}")
    @Secured({"ROLE_APP_ORGANISAATIOHALLINTA"})
    @ApiOperation(value = "Poistaa id:tä vastaavan kuvan.", notes = "Poistaa id:tä vastaavan kuvan.", response = String.class)
    public String deleteImage(@PathParam("img") String img) {
        String path = getUser() + img;
        FileItem imgFile = data.get(path);
        if (imgFile != null) {
            imgFile.delete();
            data.remove(path);
            return "ok";
        } else {
            return "organisaatio.fileupload.deletefail";
        }

    }

    private Map<String, String> storeAttachment(FileItem item) {
        Map<String, String> result = new HashMap<String, String>();
        String itemName = fixItemName(item.getName());
        result.put("name", itemName);
        LOG.info("storing " + getUser() + itemName);
        data.put(getUser() + itemName, item);
        return result;
    }

    private String getUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String fixItemName(String name) {
        if (name.contains("\\")) {
            String[] parts = name.split("\\\\");
            return parts[parts.length - 1];
        } else {
            return name;
        }
    }

}
