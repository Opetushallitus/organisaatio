package fi.vm.sade.oid;

public interface OIDService {
    String newOidByClassValue(String nodeClassValue) throws ExceptionMessage;
    String newOid(NodeClassCode nodeClass) throws ExceptionMessage;
}
