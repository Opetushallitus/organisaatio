package fi.vm.sade.oid;

public class NodeClassData {
    protected String nodeValue;
    protected String description;
    protected NodeClassCode classCode;

    /**
     * Gets the value of the nodeValue property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getNodeValue() {
        return nodeValue;
    }

    /**
     * Sets the value of the nodeValue property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setNodeValue(String value) {
        this.nodeValue = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the classCode property.
     *
     * @return
     *     possible object is
     *     {@link NodeClassCode }
     *
     */
    public NodeClassCode getClassCode() {
        return classCode;
    }

    /**
     * Sets the value of the classCode property.
     *
     * @param value
     *     allowed object is
     *     {@link NodeClassCode }
     *
     */
    public void setClassCode(NodeClassCode value) {
        this.classCode = value;
    }

}
