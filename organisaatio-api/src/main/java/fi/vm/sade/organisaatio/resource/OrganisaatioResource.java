/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
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
 */
package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import java.util.Date;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * REST services for Organisaatio.
 *
 * @author mlyly
 */
@Path("/organisaatio")
public interface OrganisaatioResource {

    public String OID_SEPARATOR = "/";

    @GET
    @Path("/hae")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public OrganisaatioHakutulos searchBasic(@QueryParam("")OrganisaatioSearchCriteria q);

   /**
     * NOTE: USED BY SECURITY FRAMEWORK - DON'T CHANGE
     *
     * Find oids of organisaatio's parents, result oids start from root, ends to given oid itself, and are separated by '/'.
     *
     * @param oid
     * @return oid/path/form/root
     * @throws Exception
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{oid}/parentoids")
    public String parentoids(@PathParam("oid") String oid) throws Exception;


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello")
    public String hello();

    /**
     * Get list of Organisaatio oids mathching the query.
     *
     * Search terms:
     * <ul>
     *   <li>searchTerms=type=KOULUTUSTOIMIJA / OPPILAITOS / TOIMIPISTE  == OrganisaatioTyyppi.name()</li>
     * </ul>
     *
     * @param searchTerms
     * @param count
     * @param startIndex
     * @param lastModifiedBefore
     * @param lastModifiedSince
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public List<String> search(@QueryParam("searchTerms") String searchTerms,
            @QueryParam("count") int count,
            @QueryParam("startIndex") int startIndex,
            @QueryParam("lastModifiedBefore") Date lastModifiedBefore,
            @QueryParam("lastModifiedSince") Date lastModifiedSince);

    /**
     * Organisaatio DTO as JSON.
     *
     * @param oid OID or Y-TUNNUS or VIRASTOTUNNUS or OPETUSPISTEKOODI or TOIMIPISTEKOODI
     * @return
     */
    @GET
    @Path("{oid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public OrganisaatioRDTO getOrganisaatioByOID(@PathParam("oid") String oid);
    
    


}
