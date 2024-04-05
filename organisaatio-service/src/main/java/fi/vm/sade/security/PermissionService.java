package fi.vm.sade.security;

public interface PermissionService {
    public boolean userCanRead();

    public boolean userCanReadAndUpdate();

    public boolean userCanCreateReadUpdateAndDelete();
}
