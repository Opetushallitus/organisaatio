package fi.vm.sade.organisaatio.model.email;

public class ReportedRecipientReplacementDTO {
   private String name = null;
   private Object value;
   /** @deprecated */
   @Deprecated
   private String defaultValue = null;

   public ReportedRecipientReplacementDTO() {
   }

   public ReportedRecipientReplacementDTO(String name, Object value) {
      this.name = name;
      this.value = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Object getEffectiveValue() {
      return this.value != null ? this.value : this.defaultValue;
   }

   public Object getValue() {
      return this.value;
   }

   public void setValue(Object value) {
      if (value != null && this.defaultValue != null) {
         this.defaultValue = null;
      }

      this.value = value;
   }

   /** @deprecated */
   @Deprecated
   public String getDefaultValue() {
      return this.defaultValue;
   }

   /** @deprecated */
   @Deprecated
   public void setDefaultValue(String defaultValue) {
      if (defaultValue != null && this.value != null) {
         this.value = null;
      }

      this.defaultValue = defaultValue;
   }

   public String toString() {
      return "ReportedRecipientReplacementDTO [name=" + this.name + ", value=" + this.value + ", defaultValue=" + this.defaultValue + "]";
   }
}
