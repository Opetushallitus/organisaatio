package fi.vm.sade.organisaatio.model.email;

import java.util.LinkedList;
import java.util.List;

public class EmailData {
   private List<EmailRecipient> recipient = new LinkedList<>();
   private List<ReplacementDTO> replacements = new LinkedList<>();
   private EmailMessage email = new EmailMessage();

   public EmailData() {
   }

   public EmailData(List<EmailRecipient> recipient, EmailMessage email) {
      this.recipient = recipient;
      this.email = email;
   }

   public EmailData(List<EmailRecipient> recipient, List<ReplacementDTO> replacements, EmailMessage email) {
      this.recipient = recipient;
      this.replacements = replacements;
      this.email = email;
   }

   public List<ReplacementDTO> getReplacements() {
      return this.replacements;
   }

   public void setReplacements(List<ReplacementDTO> replacements) {
      this.replacements = replacements;
   }

   public List<EmailRecipient> getRecipient() {
      return this.recipient;
   }

   public void setRecipient(List<EmailRecipient> recipient) {
      this.recipient = recipient;
   }

   public EmailMessage getEmail() {
      return this.email;
   }

   public void setEmail(EmailMessage email) {
      this.email = email;
   }

   public void setSenderOid(String senderOid) {
      this.email.setSenderOid(senderOid);
   }

   public String toString() {
      return "EmailData [recipient=" + this.recipient + ", replacements=" + this.replacements + ", email=" + this.email + "]";
   }
}
