package fi.vm.sade.organisaatio.service.oid;

import java.util.List;

import fi.vm.sade.oid.ExceptionMessage;
import fi.vm.sade.oid.NodeClassCode;
import fi.vm.sade.oid.NodeClassData;

public interface OidService {
    public String newOidByClassValue(String nodeClassValue) throws ExceptionMessage;

    public List<NodeClassData> getNodeClasses() throws ExceptionMessage;

    public String newOid(NodeClassCode nodeClass) throws ExceptionMessage;
}
