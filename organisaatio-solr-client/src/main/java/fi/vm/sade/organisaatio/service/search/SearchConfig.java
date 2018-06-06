package fi.vm.sade.organisaatio.service.search;

public class SearchConfig {

    private final boolean parentsIncluded;
    private final boolean childrenIncluded;

    public boolean isParentsIncluded() {
        return parentsIncluded;
    }

    public boolean isChildrenIncluded() {
        return childrenIncluded;
    }

    public SearchConfig(boolean parentsIncluded, boolean childrenIncluded) {
        this.parentsIncluded = parentsIncluded;
        this.childrenIncluded = childrenIncluded;
    }

}
