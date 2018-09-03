package fi.vm.sade.organisaatio.resource.impl.v4;

import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioDTOV4ModelMapper;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.dto.v4.*;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import fi.vm.sade.organisaatio.resource.v3.OrganisaatioResourceV3;
import fi.vm.sade.organisaatio.resource.v4.OrganisaatioResourceV4;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@CrossOriginResourceSharing(allowAllOrigins = true)
public class OrganisaatioResourceImplV4 implements OrganisaatioResourceV4 {

    private final OrganisaatioResourceV2 organisaatioResourceV2;
    private final OrganisaatioResourceV3 organisaatioResourceV3;

    private final OrganisaatioDTOV4ModelMapper organisaatioDTOV4ModelMapper;

    @Autowired
    public OrganisaatioResourceImplV4(OrganisaatioResourceV2 organisaatioResourceV2,
                                      OrganisaatioResourceV3 organisaatioResourceV3,
                                      OrganisaatioDTOV4ModelMapper organisaatioDTOV4ModelMapper) {
        this.organisaatioResourceV2 = organisaatioResourceV2;
        this.organisaatioResourceV3 = organisaatioResourceV3;
        this.organisaatioDTOV4ModelMapper = organisaatioDTOV4ModelMapper;
    }

    // POST //organisaatio/v4/findbyoids
    @Override
    public List<OrganisaatioRDTOV4> findByOids(List<String> oids){
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV3.findByOids(oids), new TypeToken<List<OrganisaatioRDTOV4>>() {}.getType());
    }

    // GET /organisaatio/v4/{oid}/children
    @Override
    public List<OrganisaatioRDTOV4> children(String oid, boolean includeImage) throws Exception {
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV3.children(oid, includeImage), new TypeToken<List<OrganisaatioRDTOV4>>() {}.getType());
    }

    // GET /organisaatio/v4/{oid}
    @Override
    public OrganisaatioRDTOV4 getOrganisaatioByOID(String oid, boolean includeImage) {
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV3.getOrganisaatioByOID(oid, includeImage), OrganisaatioRDTOV4.class);
    }

    // PUT /organisaatio/v4/{oid}
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public ResultRDTOV4 updateOrganisaatio(String oid, OrganisaatioRDTOV4 ordto) {
        OrganisaatioRDTOV3 organisaatioRDTOV3 = this.organisaatioDTOV4ModelMapper.map(ordto, OrganisaatioRDTOV3.class);
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV3.updateOrganisaatio(oid, organisaatioRDTOV3), ResultRDTOV4.class);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public String deleteOrganisaatio(String oid) {
        return this.organisaatioResourceV3.deleteOrganisaatio(oid);
    }

    // POST /organisaatio/v4/
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public ResultRDTOV4 newOrganisaatio(OrganisaatioRDTOV4 ordto) {
        OrganisaatioRDTOV3 organisaatioRDTOV3 = this.organisaatioDTOV4ModelMapper.map(ordto, OrganisaatioRDTOV3.class);
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV3.newOrganisaatio(organisaatioRDTOV3), ResultRDTOV4.class);
    }

    // GET /organisaatio/v4/muutetut
    @Override
    public List<OrganisaatioRDTOV4> haeMuutetut(DateParam lastModifiedSince, boolean includeImage) {
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV3.haeMuutetut(lastModifiedSince, includeImage), new TypeToken<List<OrganisaatioRDTOV4>>() {}.getType());
    }

    @Override
    public OrganisaatioHistoriaRDTOV4 getOrganizationHistory(String oid) throws Exception {
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV2.getOrganizationHistory(oid), OrganisaatioHistoriaRDTOV4.class);
    }

    @Override
    public OrganisaatioHakutulosV4 searchOrganisaatiot(OrganisaatioSearchCriteriaDTOV4 hakuEhdot) {
        OrganisaatioSearchCriteriaDTOV2 organisaatioSearchCriteriaDTOV2 = this.organisaatioDTOV4ModelMapper.map(hakuEhdot, OrganisaatioSearchCriteriaDTOV2.class);
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV2.searchOrganisaatiot(organisaatioSearchCriteriaDTOV2), OrganisaatioHakutulosV4.class);
    }

    @Override
    public OrganisaatioHakutulosV4 searchOrganisaatioHierarkia(OrganisaatioSearchCriteriaDTOV4 hakuEhdot) {
        OrganisaatioSearchCriteriaDTOV2 organisaatioSearchCriteriaDTOV2 = this.organisaatioDTOV4ModelMapper.map(hakuEhdot, OrganisaatioSearchCriteriaDTOV2.class);
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV2.searchOrganisaatioHierarkia(organisaatioSearchCriteriaDTOV2), OrganisaatioHakutulosV4.class);
    }
}
