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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.service.exception.SadeBusinessException;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioDeleteBusinessService;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.dto.mapping.SearchCriteriaModelMapper;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioResult;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.resource.dto.ResultRDTO;
import fi.vm.sade.organisaatio.resource.dto.YhteystietojenTyyppiRDTO;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import fi.vm.sade.organisaatio.service.util.OrganisaatioPerustietoUtil;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.dao.YhteystietojenTyyppiDAO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.validation.ValidationException;
import javax.ws.rs.core.Response;

/**
 * @author Antti Salonen
 * @author mlyly
 * @author simok
 */
@Component
@Transactional(readOnly = true)
@CrossOriginResourceSharing(allowAllOrigins = true)
public class OrganisaatioResourceImpl implements OrganisaatioResource {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioResourceImpl.class);
    @Autowired
    private OrganisaatioBusinessService organisaatioBusinessService;
    @Autowired
    private OrganisaatioDeleteBusinessService organisaatioDeleteBusinessService;
    @Autowired
    private OrganisaatioFindBusinessService organisaatioFindBusinessService;
    @Autowired
    private OrganisaatioSearchService organisaatioSearchService;
    @Autowired
    private YhteystietojenTyyppiDAO yhteystietojenTyyppiDAO;
    @Autowired
    private ConversionService conversionService;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    @Autowired
    PermissionChecker permissionChecker;

    @Autowired
    private SearchCriteriaModelMapper searchCriteriaModelMapper;

    // GET /organisaatio/hae
    @Override
    public OrganisaatioHakutulos searchHierarchy(OrganisaatioSearchCriteria s) {
        final OrganisaatioHakutulos tulos = new OrganisaatioHakutulos();

        if (s.getOppilaitosTyyppi() != null && s.getOppilaitosTyyppi().isEmpty()) {
            s.setOppilaitosTyyppi(null);
        }

        if (s.getOrganisaatioTyyppi() != null && s.getOrganisaatioTyyppi().length() == 0) {
            s.setOrganisaatioTyyppi(null);
        }

        // Map api search criteria to solr search criteria
        SearchCriteria searchCriteria = searchCriteriaModelMapper.map(s, SearchCriteria.class);

        List<OrganisaatioPerustieto> organisaatiot = organisaatioSearchService.searchHierarchy(searchCriteria);

        //sorttaa
        final Ordering<OrganisaatioPerustieto> ordering = Ordering.natural().nullsFirst().onResultOf(new Function<OrganisaatioPerustieto, Comparable<String>>() {
            @Override
            public Comparable<String> apply(OrganisaatioPerustieto input) {
                return OrganisaatioDisplayHelper.getClosestBasic(I18N.getLocale(), input);

            }
        });

        organisaatiot = ordering.immutableSortedCopy(organisaatiot);

        //rakenna hierarkia
        tulos.setOrganisaatiot(OrganisaatioPerustietoUtil.createHierarchy(organisaatiot));

        tulos.setNumHits(organisaatiot.size());
        return tulos;
    }

    // GET /organisaatio/{oid}/children
    @Override
    public List<OrganisaatioRDTO> children(String oid, boolean includeImage) throws Exception {
        Preconditions.checkNotNull(oid);
        Organisaatio parentOrg = organisaatioFindBusinessService.findById(oid);
        List<OrganisaatioRDTO> childList = new LinkedList<>();
        if (parentOrg != null) {
            for (Organisaatio child : parentOrg.getChildren(true)) {
                // Jätetään kuva pois, jos sitä ei haluta
                if (child.getMetadata() != null) {
                    child.getMetadata().setIncludeImage(includeImage);
                }

                childList.add(conversionService.convert(child, OrganisaatioRDTO.class));
            }
        }
        return childList;
    }

    // GET /organisaatio/{oid}/childoids
    @Override
    public String childoids(String oid) throws Exception {
        Preconditions.checkNotNull(oid);
        Organisaatio parentOrg = organisaatioFindBusinessService.findById(oid);
        List<String> childOidList = new LinkedList<>();
        if (parentOrg != null) {
            for (Organisaatio child : parentOrg.getChildren(true)) {
                childOidList.add("\"" + child.getOid() + "\"");
            }
        }
        return "{ \"oids\": [" + Joiner.on(",").join(childOidList) + "]}";
    }

    // GET /organisaatio/{oid}/parentoids - used for security purposes
    @Override
    public String parentoids(String oid) throws Exception {
        Preconditions.checkNotNull(oid);
        // find parents
        final List<String> parentOidList = organisaatioSearchService.findParentOids(oid);
        Collections.reverse(parentOidList);

        // NOTE - this assumes everything is under one "root", ie. "OPH"
        if (!parentOidList.contains(rootOrganisaatioOid)) {
            parentOidList.add(0, rootOrganisaatioOid); // add root organisaatio if needed
        }
        if (!parentOidList.contains(oid)) {
            parentOidList.add(oid); // add self if needed
        }
        return Joiner.on(OID_SEPARATOR).join(parentOidList);
    }

    // GET /organisaatio/hello
    @Override
    public String hello() {
        return "Well Hello! " + new Date();
    }

    // GET /organisaatio?searchTerms=x&count=10&startIndex=100&lastModifiedBefore=X&lastModifiedSince=Y
    @Override
    public List<String> search(String searchTerms, int count, int startIndex, Date lastModifiedBefore, Date lastModifiedSince) {
        LOG.debug("search({}, {}, {}, {}, {})", new Object[]{searchTerms, count, startIndex, lastModifiedBefore, lastModifiedSince});

        // Check the type spesified search
        OrganisaatioTyyppi type = null;
        if (searchTerms != null) {
            for (OrganisaatioTyyppi organisaatioTyyppi : OrganisaatioTyyppi.values()) {
                if (searchTerms.contains("type=" + organisaatioTyyppi.name())) {
                    type = organisaatioTyyppi;
                    break;
                }
            }
        }

        // lastModifiedBefore ja lastModifiedSince jätetään pois --> muutetut organisaatiot rajapinta palauttaa nuo
        List<String> result = organisaatioFindBusinessService.findOidsBy(searchTerms, count, startIndex, type);
        LOG.debug("  result.size = {}", result.size());
        return result;
    }

    // GET /organisaatio/{oid}
    @Override
    public OrganisaatioRDTO getOrganisaatioByOID(final String oid, boolean includeImage) {
        LOG.debug("/organisaatio/{} -- getOrganisaatioByOID()", oid);

        Organisaatio o = organisaatioFindBusinessService.findById(oid);

        if (o == null) {
            LOG.info("Failed to find organisaatio by: " + oid);
            throw new OrganisaatioResourceException(404, "organisaatio.exception.organisaatio.not.found");
        }

        // Jätetään kuva pois, jos sitä ei haluta
        if (o.getMetadata() != null) {
            o.getMetadata().setIncludeImage(includeImage);
        }

        OrganisaatioRDTO result = conversionService.convert(o, OrganisaatioRDTO.class);

        LOG.debug("  result={}", result);
        return result;
    }

    // POST /organisaatio/{oid}
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public ResultRDTO updateOrganisaatio(String oid, OrganisaatioRDTO ordto) {
        LOG.info("Saving " + oid);
        try {
            permissionChecker.checkSaveOrganisation(ordto, true);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to update organisation: " + oid);
            throw new OrganisaatioResourceException(nae);
        }

        try {
            OrganisaatioResult result = organisaatioBusinessService.save(ordto, true);
            return new ResultRDTO(conversionService.convert(result.getOrganisaatio(), OrganisaatioRDTO.class),
                    result.getInfo()==null ? ResultRDTO.ResultStatus.OK : ResultRDTO.ResultStatus.WARNING, result.getInfo());
        } catch (ValidationException ex) {
            LOG.warn("Error saving " + oid, ex);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
                    ex.getMessage(), "organisaatio.validointi.virhe");
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error saving " + oid, sbe);
            throw new OrganisaatioResourceException(sbe);
        } catch (OrganisaatioResourceException ore) {
            LOG.warn("Error saving " + oid, ore);
            throw ore;
        } catch (Throwable t) {
            LOG.error("Error saving " + oid, t);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
                    t.getMessage(), "generic.error");
        }
    }

    // DELETE /organisaatio/{oid}
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public String deleteOrganisaatio(String oid) {
        try {
            permissionChecker.checkRemoveOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to delete organisation: " + oid);
            throw new OrganisaatioResourceException(nae);
        }

        try {
            Organisaatio parent = organisaatioDeleteBusinessService.deleteOrganisaatio(oid);
            LOG.info("Deleted organisaatio: " + oid +" under parent: " + parent.getOid());
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error deleting org", sbe);
            throw new OrganisaatioResourceException(sbe);
        }

        return "{\"message\": \"deleted\"}";
    }

    // PUT /organisaatio/
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public ResultRDTO newOrganisaatio(OrganisaatioRDTO ordto) {
        try {
            permissionChecker.checkSaveOrganisation(ordto, false);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to create child organisation for: " + ordto.getParentOid());
            throw new OrganisaatioResourceException(nae);
        }
        try {
            OrganisaatioResult result = organisaatioBusinessService.save(ordto, false);
            return new ResultRDTO(conversionService.convert(result.getOrganisaatio(), OrganisaatioRDTO.class),
                    result.getInfo()==null ? ResultRDTO.ResultStatus.OK : ResultRDTO.ResultStatus.WARNING, result.getInfo());
        } catch (ValidationException ex) {
            LOG.warn("Error saving new org", ex);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
                    ex.getMessage(), "organisaatio.validointi.virhe");
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error saving new org", sbe);
            throw new OrganisaatioResourceException(sbe);
        } catch (Throwable t) {
            LOG.warn("Error saving new org", t);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
                    t.getMessage(), "generic.error");
        }
    }

    // GET /organisaatio/yhteystietometadata
    @Override
    @Transactional(readOnly = true)
    public List<YhteystietojenTyyppiRDTO> getYhteystietoMetadata(List<String> organisaatioTyyppi) {
        if (organisaatioTyyppi == null || organisaatioTyyppi.isEmpty()) {
            return new ArrayList<>();
        }
        List<YhteystietojenTyyppi> entitys = yhteystietojenTyyppiDAO.findLisatietoMetadataForOrganisaatio(organisaatioTyyppi);
        if (entitys == null) {
            return null;
        }
        List<YhteystietojenTyyppiRDTO> result = new ArrayList<>();
        for (YhteystietojenTyyppi entity : entitys) {
            result.add(conversionService.convert(entity, YhteystietojenTyyppiRDTO.class));
        }
        return result;
    }

    // GET /organisaatio/auth
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public String authHello() {
        return "{\"message\": \"Well Hello! " + new Date() + "\"}";
    }

    // GET /organisaatio/{oid}/ryhmat
    @Override
    public List<OrganisaatioRDTO> groups(String oid, boolean includeImage) throws Exception {
        Preconditions.checkNotNull(oid);
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> entitys = organisaatioFindBusinessService.findGroups();
        if (entitys == null) {
            return null;
        }

        LOG.debug("Ryhmien haku {} ms", System.currentTimeMillis() - qstarted);
        long qstarted2 = System.currentTimeMillis();

        List<OrganisaatioRDTO> groupList = new ArrayList<>();
        for (Organisaatio entity : entitys) {
            // Jätetään kuva pois, jos sitä ei haluta
            if (entity.getMetadata() != null) {
                entity.getMetadata().setIncludeImage(includeImage);
            }

            groupList.add(conversionService.convert(entity, OrganisaatioRDTO.class));
        }

        LOG.debug("Ryhmien convertointi {} ms --> yhteensä {} ms", System.currentTimeMillis() - qstarted2, System.currentTimeMillis() - qstarted);

        return groupList;
    }
}
