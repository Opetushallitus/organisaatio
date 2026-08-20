package fi.vm.sade.auditlog;

/**
 * Java enums implement name() by default so this "should just work" if used with operation-enums
 */
public interface Operation {
    public String name();
}
