package fi.vm.sade.oid;

public class Exception {
    protected String errorCode;
    protected String explanation;

    /**
     * Gets the value of the errorCode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the value of the errorCode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setErrorCode(String value) {
        this.errorCode = value;
    }

    /**
     * Gets the value of the explanation property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Sets the value of the explanation property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setExplanation(String value) {
        this.explanation = value;
    }

}
