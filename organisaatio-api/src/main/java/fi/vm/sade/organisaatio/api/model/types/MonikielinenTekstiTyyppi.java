
package fi.vm.sade.organisaatio.api.model.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class MonikielinenTekstiTyyppi implements Serializable  {

    private static final long serialVersionUID = 100L;
    private List<Teksti> teksti;

    /**
     * Gets the value of the teksti property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the teksti property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTeksti().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Teksti }
     * 
     * 
     */
    public List<Teksti> getTeksti() {
        if (teksti == null) {
            teksti = new ArrayList<>();
        }
        return this.teksti;
    }


    public static class Teksti implements Serializable {
        private static final long serialVersionUID = 100L;
        protected String value;
        protected String kieliKoodi;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the kieliKoodi property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getKieliKoodi() {
            return kieliKoodi;
        }

        /**
         * Sets the value of the kieliKoodi property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setKieliKoodi(String value) {
            this.kieliKoodi = value;
        }

    }

}
