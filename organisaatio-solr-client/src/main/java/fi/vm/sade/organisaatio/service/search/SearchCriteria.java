package fi.vm.sade.organisaatio.service.search;

import java.io.Serializable;

public class SearchCriteria implements Serializable {

    private final static long serialVersionUID = 100L;
    protected int maxResults;
    protected String searchStr;

    /**
     * Default no-arg constructor
     * 
     */
    public SearchCriteria() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public SearchCriteria(final int maxResults, final String searchStr) {
        this.maxResults = maxResults;
        this.searchStr = searchStr;
    }

    /**
     * Gets the value of the maxResults property.
     * 
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Sets the value of the maxResults property.
     * 
     */
    public void setMaxResults(int value) {
        this.maxResults = value;
    }

    /**
     * Gets the value of the searchStr property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSearchStr() {
        return searchStr;
    }

    /**
     * Sets the value of the searchStr property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setSearchStr(String value) {
        this.searchStr = value;
    }

}
