package fi.vm.sade.organisaatio.model.email;
import java.util.Date;

public class ReplacementDTO extends BaseDTO {
   private static final long serialVersionUID = 8136375073148653926L;
   private String name = null;
   private String defaultValue = null;
   private boolean mandatory = false;
   private Date timestamp;
   public static final String NAME_EMAIL_SENDER_FROM = "sender-from";
   public static final String NAME_EMAIL_SENDER_NAME_PERSONAL = "sender-from-personal";
   public static final String NAME_EMAIL_REPLY_TO = "reply-to";
   public static final String NAME_EMAIL_REPLY_TO_PERSONAL = "reply-to-personal";
   public static final String NAME_EMAIL_SUBJECT = "subject";
   public static final String NAME_EMAIL_BODY = "sisalto";

   public ReplacementDTO() {
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDefaultValue() {
      return this.defaultValue;
   }

   public void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
   }

   public boolean isMandatory() {
      return this.mandatory;
   }

   public void setMandatory(boolean mandatory) {
      this.mandatory = mandatory;
   }

   public Date getTimestamp() {
      return this.timestamp;
   }

   public void setTimestamp(Date timestamp) {
      this.timestamp = timestamp;
   }

   public String toString() {
      return "ReplacementDTO [name=" + this.name + ", defaultValue=" + this.defaultValue + ", mandatory=" + this.mandatory + ", timestamp=" + this.timestamp + ", getId()=" + this.getId() + "]";
   }
}
