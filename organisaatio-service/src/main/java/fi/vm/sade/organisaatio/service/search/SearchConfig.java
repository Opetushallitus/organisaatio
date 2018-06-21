package fi.vm.sade.organisaatio.service.search;

public class SearchConfig {

    private final boolean parentsIncluded;
    private final boolean childrenIncluded;
    private final boolean countChildren;

    public boolean isParentsIncluded() {
        return parentsIncluded;
    }

    public boolean isChildrenIncluded() {
        return childrenIncluded;
    }

    public boolean isCountChildren() {
        return countChildren;
    }

    public SearchConfig(boolean parentsIncluded, boolean childrenIncluded, boolean countChildren) {
        this.parentsIncluded = parentsIncluded;
        this.childrenIncluded = childrenIncluded;
        this.countChildren = countChildren;
    }

}
