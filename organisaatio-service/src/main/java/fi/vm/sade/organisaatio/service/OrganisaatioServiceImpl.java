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
package fi.vm.sade.organisaatio.service;

import fi.vm.sade.organisaatio.business.exception.LearningInstitutionExistsException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioCrudException;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import fi.vm.sade.generic.common.DateHelper;
import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.types.*;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.model.dto.OrgStructure;
import fi.vm.sade.organisaatio.model.OrganisaatioMetaData;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dao.YhteystietoArvoDAO;
import fi.vm.sade.organisaatio.dao.YhteystietoElementtiDAO;
import fi.vm.sade.organisaatio.dao.YhteystietojenTyyppiDAO;
import fi.vm.sade.organisaatio.service.converter.ConverterFactory;
import fi.vm.sade.organisaatio.service.converter.EntityToOrganisaatioKuvailevatTiedotTyyppiFunction;
import fi.vm.sade.organisaatio.service.converter.OrganisaatioBasicConverter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import javax.jws.WebParam;
import java.util.*;

/**
 * @author Antti Salonen
 */
@Transactional(rollbackFor = Throwable.class, readOnly = true)
public class OrganisaatioServiceImpl implements fi.vm.sade.organisaatio.api.model.OrganisaatioService {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioServiceImpl.class);

    @Autowired
    PermissionChecker permissionChecker;

    @Autowired
    private ConverterFactory converterFactory;

    @Autowired
    protected YhteystietoElementtiDAO yhteystietoElementtiDAO;

    @Autowired
    protected YhteystietoArvoDAO yhteystietoArvoDAO;

    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    private YhteystietojenTyyppiDAO yhteystietojenTyyppiDAO;

    @Value("${root.organisaatio.oid}")
    private String ophOid;

    private static final String parentSplitter = "\\|";

    @Autowired
    public void setDao(OrganisaatioDAO dao) {
        this.organisaatioDAO = dao;
    }

    @Override
    public FindBasicParentOrganisaatioTypesResponse findBasicParentOrganisaatios(FindBasicParentOrganisaatioTypesParameter parameters) {
        FindBasicParentOrganisaatioTypesResponse response = new FindBasicParentOrganisaatioTypesResponse();
        response.getOrganisaatioPerustiedot().addAll(OrganisaatioBasicConverter.convertToPerustietos(organisaatioDAO.findChildren(ophOid, false, true)));//findBasicOrgInfo(OPH_OID)));
        return response;
    }

    @Override
    public FindBasicOrganisaatioChildsToOidTypesResponse findBasicOrganisaatioChildsToOid(FindBasicOrganisaatioChildsToOidTypesParameter parameters) {
        LOG.debug("findBasicOrganisaatioChildsToOid({})", parameters.getOid());

        FindBasicOrganisaatioChildsToOidTypesResponse response = new FindBasicOrganisaatioChildsToOidTypesResponse();
        response.getOrganisaatioPerustiedot().addAll(OrganisaatioBasicConverter.convertToPerustietos(organisaatioDAO.findChildren(parameters.getOid(), false, true)));//findBasicOrgInfo(parameters.getOid())));
        return response;
    }

    @Override
    public List<OrganisaatioDTO> findParentsByOidList(@WebParam(name = "oids", targetNamespace = "http://model.api.organisaatio.sade.vm.fi/types") List<String> oids) {
        return convertListToDto(organisaatioDAO.findByOidList(oids, oids.size()));

    }

    public List<OrganizationStructureType> getOrganizationStructure() {
        return getOrganizationStructure(null);
    }

    private String getTekstiByLang(MonikielinenTeksti teksti, String lang) {
        String returnValue = null;

        if (teksti != null && teksti.getValues().containsKey(lang)) {
            returnValue = teksti.getValues().get(lang);
        }

        return returnValue;
    }

    @Override
    public List<OrganizationStructureType> getOrganizationStructure(@WebParam(name = "oids", targetNamespace = "http://model.api.organisaatio.sade.vm.fi/types") List<String> oids) {
        if (oids == null) {
            oids = new ArrayList<>();
        }
        List<OrganizationStructureType> r = new ArrayList<>();
        //TODO: refaktoroi konvertteriin
        for (OrgStructure o : organisaatioDAO.getOrganizationStructure(oids)) {
            OrganizationStructureType ot = new OrganizationStructureType();

            String nameFi = getTekstiByLang(o.getName(), "fi");
            String nameSv = getTekstiByLang(o.getName(), "sv");
            String nameEn = getTekstiByLang(o.getName(), "en");

            ot.setNameFi(nameFi);
            ot.setNameSv(nameSv);
            ot.setNameEn(nameEn);
            ot.setOid(o.getOid());
            ot.setLakkautusPvm(o.getLakkautusPvm() != null ? DateHelper.DateToXmlCal(o.getLakkautusPvm()) : null);
            ot.setPoistettu(o.getPoistettu());

            String parentOidPath = o.getOidPath();

            if (StringUtils.isNotBlank(parentOidPath)) {   //parse parentoid if present
                String[] ancestors = parentOidPath.split("\\|");
                if (ancestors.length > 0) {
                    String parentOid = ancestors[ancestors.length - 1];
                    ot.setParentOid(parentOid);
                }
            }
            r.add(ot);
        }
        return r;
    }

    @Override
    public OrganisaatioOidListType findChildrenOidsByOid(OrganisaatioSearchOidType parameters) {
        LOG.warn("findChildrenOidsByOid({})", parameters.getSearchOid());

        Collection<String> tmp = null;

        // Root oid?
        if (parameters.getSearchOid().trim().equalsIgnoreCase(ophOid)) {
            tmp = organisaatioDAO.findAllOids(false);
            tmp.remove(ophOid);
        } else {
            tmp = organisaatioDAO.listDescendantOids(parameters.getSearchOid(), false);
        }

        // Create result
        OrganisaatioOidListType oidList = new OrganisaatioOidListType();
        for (String oid : tmp) {
            OrganisaatioOidType oidType = new OrganisaatioOidType();
            oidType.setOrganisaatioOid(oid);
            oidList.getOrganisaatioOidList().add(oidType);
        }

        return oidList;
    }

    @Override
    public String ping(String param) throws GenericFault { // TODO: move to some testservice
        LOG.debug(" *** OrganisaatioServiceImpl.ping: " + param);

        if ("!runtimeexception".equals(param)) {
            throw new RuntimeException("ping throwing test runtime exception");
        }
        if ("!businessexception".equals(param)) {
            throw new GenericFault("ping test validation error");
        }

        if ("!!".equals(param)) {
            throw new LearningInstitutionExistsException();
        }

        return this.getClass().getName();
    }

    @Override
    public List<OrganisaatioDTO> findParentsTo(String organisaatioId) {
        LOG.debug("findParentsTo({})", organisaatioId);
        if (organisaatioId == null || organisaatioId.trim().equalsIgnoreCase(ophOid)) {
            return new ArrayList<>();
        } else {

            List<OrganisaatioDTO> path = new ArrayList<>();
            OrganisaatioDTO organisaatio = this.convertToDTO(this.organisaatioDAO.findByOid(organisaatioId));
            LOG.debug("parent path: {}", organisaatio.getParentOidPath());
            String[] oids = (organisaatio.getParentOidPath() != null) ? organisaatio.getParentOidPath().split(parentSplitter) : new String[0];
            LOG.debug("Oids length: {}", oids.length);


            for (String curOid : oids) {
                LOG.debug("Cur oid: {}", curOid);
                if (curOid != null && !curOid.isEmpty() && !curOid.equals(ophOid)) {
                    path.add(convertToDTO((this.organisaatioDAO.findByOid(curOid))));
                }
            }
            path.add(organisaatio);
            return path;

        }
    }

    /**
     * Convert list of entities to list of DTOs.
     *
     * @param organisaatios
     * @return
     */
    private List<OrganisaatioDTO> convertListToDto(List<Organisaatio> organisaatios) {
        List<OrganisaatioDTO> orgDtos = new ArrayList<>();
        for (Organisaatio org : organisaatios) {
            orgDtos.add(convertToDTO(org));
        }
        return orgDtos;
    }

    protected OrganisaatioDTO convertToFatDTO(Organisaatio entity) {
        return converterFactory.convertToFatDTO(entity);
    }

    protected OrganisaatioDTO convertToDTO(Organisaatio entity) {
        return converterFactory.convertToDTO(entity, OrganisaatioDTO.class);
    }

    protected Organisaatio convertToJPA(OrganisaatioDTO dto, boolean merge) {
        return converterFactory.convertOrganisaatioToEntity(dto, merge);
    }

    @Override
    public List<OrganisaatioDTO> searchOrganisaatios(OrganisaatioSearchCriteriaDTO searchSpec) {
        LOG.debug("searchOrganisaatios(): sspec={}", searchSpec);

        if (this.isEmptyRestrictions(searchSpec)
                && (searchSpec.getSearchStr() == null || searchSpec.getSearchStr().length() < 3)) {
            throw new OrganisaatioCrudException("organisaatioSearch.tooManyResults");
        }

        //Long startTime = System.currentTimeMillis();
        LOG.debug("Starting root expansion");
        //List<String> oidHierarchy = createOidHierarchy(searchSpec.getOidResctrictionList());
        //LOG.debug("Root expansion took: " + (System.currentTimeMillis() - startTime) + " milliseconds");
        //LOG.debug("oidHierarchySize: " + oidHierarchy.size());

        List<OrganisaatioDTO> matchingOrgs = OrganisaatioBasicConverter.convertSmallToDTOs(organisaatioDAO.findBySearchCriteriaExact(searchSpec.getOrganisaatioTyyppi(), searchSpec.getOppilaitosTyyppi(),
                searchSpec.getKunta(),
                searchSpec.getSearchStr(),
                searchSpec.isSuunnitellut(), searchSpec.isLakkautetut(),
                searchSpec.getMaxResults(), searchSpec.getOidResctrictionList()));

        return matchingOrgs;
    }

    @Override
    public OrganisaatioDTO findByOid(String oid) {
        return findByOid(oid, true);
    }

    @Override
    public List<OrganisaatioDTO> findByOidList(List<String> oids, int maxResults) {
        LOG.debug("findByOidList(oids={}, max={})", oids, maxResults);
        if (oids.contains(ophOid)) {
            //   return convertListToDto(removeRootOrganization(organisaatioDAO.findAll()));
            return convertListToDto(organisaatioDAO.findAll());
        } else {
            // Get result (max MAX RESULTS branches)
            List<Organisaatio> orgsE = Lists.newArrayList(Iterables.filter(organisaatioDAO.findDescendantsByOidList(oids, maxResults), new Predicate<Organisaatio>() {
                final HashSet<String> seen = new HashSet<>();

                @Override
                public boolean apply(Organisaatio org) {
                    if (!seen.contains(org.getOid())) {
                        seen.add(org.getOid());
                        return true;
                    }
                    return false;
                }
            }));

            return converterFactory.convertToDTO(orgsE, OrganisaatioDTO.class);
        }
    }

    @Override
    public List<OrganisaatioDTO> findChildrenTo(String organisaatioId) {
        LOG.debug("findChildrenTo({})", organisaatioId);
        return convertListToDto(organisaatioDAO.findChildren(organisaatioId, false, true));
    }

    @Override
    public List<YhteystietojenTyyppiDTO> findYhteystietoMetadataForOrganisaatio(List<String> organisaatioTyyppi) {
        LOG.debug("findYhteystietoMetadataForOrganisaatio()");

        if (organisaatioTyyppi == null || organisaatioTyyppi.isEmpty()) {
            return new ArrayList<>();
        }

        List<YhteystietojenTyyppi> entitys = yhteystietojenTyyppiDAO.findLisatietoMetadataForOrganisaatio(organisaatioTyyppi);
        if (entitys == null) {
            return null;
        }
        List<YhteystietojenTyyppiDTO> result = new ArrayList<>();
        for (YhteystietojenTyyppi entity : entitys) {
            result.add((YhteystietojenTyyppiDTO) converterFactory.convertToDTO(entity));
        }
        return result;
    }

    @Override
    public List<YhteystietoArvoDTO> findYhteystietoArvosForOrganisaatio(
            String organisaatioOid) {
        LOG.debug("findYhteystietoArvosForOrganisaatio({})", organisaatioOid);

        return converterFactory.convertToDTO(yhteystietoArvoDAO.findBy("organisaatio.oid", organisaatioOid), YhteystietoArvoDTO.class);
    }

    @Override
    public YhteystietojenTyyppiDTO readYhteystietojenTyyppi(
            String yhteystietojenTyyppiOid) {
        LOG.debug("readYhteystietojenTyyppi({})", yhteystietojenTyyppiOid);
        return converterFactory.convertToDTO(this.yhteystietojenTyyppiDAO.findBy("oid", yhteystietojenTyyppiOid).get(0));
    }

    @Override
    public List<YhteystietojenTyyppiDTO> findYhteystietojenTyyppis(
            SearchCriteriaDTO yhteystietojenTyyppiSearchCriteria) {
        LOG.debug("findYhteystietojenTyyppis()");

        List<YhteystietojenTyyppi> entitys = this.yhteystietojenTyyppiDAO.findAll();
        if (entitys == null) {
            return null;
        }
        List<YhteystietojenTyyppiDTO> result = new ArrayList<>();
        for (YhteystietojenTyyppi entity : entitys) {
            result.add((YhteystietojenTyyppiDTO) converterFactory.convertToDTO(entity));
        }
        return result;
    }

    private boolean isEmptyRestrictions(OrganisaatioSearchCriteriaDTO organisaatioSearchCriteria) {
        return organisaatioSearchCriteria.getKunta() == null
                && organisaatioSearchCriteria.getOppilaitosTyyppi() == null
                && organisaatioSearchCriteria.getOrganisaatioTyyppi() == null
                //&& !organisaatioSearchCriteria.isLakkautetut()
                //&& !organisaatioSearchCriteria.isSuunnitellut()
                && organisaatioSearchCriteria.getOidResctrictionList().isEmpty();
    }

    /*
     * Finds organisaatio by oid but does not convert the LOP tiedot
     * if the parameter withLop is false.
     */
    private OrganisaatioDTO findByOid(String oid, boolean withLop) {
        LOG.debug("findByOid({})", oid);
        //long t = System.currentTimeMillis();
        Organisaatio orgE = organisaatioDAO.findByOid(oid);
        if (orgE == null) {
            //LOG.info("findByOId({}) no result in {} ms", oid, System.currentTimeMillis()-t);
            return null;
        }

        OrganisaatioDTO dto = convertToDTO(orgE);

        if (withLop) {
            OrganisaatioMetaData metadataE = orgE.getMetadata() != null ? orgE.getMetadata() : orgE.getParentMetadata();
            dto.setKuvailevatTiedot(new EntityToOrganisaatioKuvailevatTiedotTyyppiFunction(converterFactory).apply(metadataE));
            //If organisation does not have it's own metadata, oids from the parent metadata are removed
            //because parent metadata is used as template data for the organisation.
            if (orgE.getMetadata() == null) {
                clearYtOids(dto.getKuvailevatTiedot());
            }
        }


        //LOG.info("findByOId({}) took {} ms", oid, System.currentTimeMillis()-t);
        return dto;
    }

    private void clearYtOids(OrganisaatioKuvailevatTiedotTyyppi lopTiedot) {
        if (lopTiedot != null
                && lopTiedot.getHakutoimisto() != null
                && lopTiedot.getHakutoimisto().getOpintotoimistoYhteystiedot() != null) {
            for (YhteystietoDTO curYT : lopTiedot.getHakutoimisto().getOpintotoimistoYhteystiedot()) {
                curYT.setYhteystietoOid(null);
            }
        }
    }

}
