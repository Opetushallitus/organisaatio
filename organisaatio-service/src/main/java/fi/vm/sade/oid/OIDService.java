package fi.vm.sade.oid;

import java.util.List;

public interface OIDService {

    /**
     * Generoi uuden OID:n juurisolmun ja annetun
     * 				solmuluokan arvon perusteella
     *
     */
    public String newOidByClassValue(String nodeClassValue) throws ExceptionMessage;

    /**
     * Palauttaa
     * 				kaikki solmuluokat, joille palvelu voi
     * 				generoida OID:n
     *
     */
    public List<NodeClassData> getNodeClasses() throws ExceptionMessage;

    /**
     * Generoi uuden OID:n juurisolmun ja annetun
     * 				solmuluokan enumeroidun koodin perusteella
     *
     */
    public String newOid(NodeClassCode nodeClass) throws ExceptionMessage;
}
