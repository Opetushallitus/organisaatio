package fi.vm.sade.organisaatio.model.email;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EmailRecipient {
   private String oid;
   private String oidType;
   private String email;
   private String languageCode;
   private String name;
   private List<ReportedRecipientReplacementDTO> recipientReplacements;
   private List<EmailAttachment> attachments;
   private List<AttachmentResponse> attachInfo;

   public void setOid(String oid) {
      this.oid = oid;
   }

   public void setOidType(String oidType) {
      this.oidType = oidType;
   }

   public void setLanguageCode(String languageCode) {
      this.languageCode = languageCode;
   }

   public EmailRecipient() {
      this.oid = "";
      this.oidType = "";
      this.email = "";
      this.languageCode = "FI";
      this.attachments = new LinkedList<>();
      this.attachInfo = new LinkedList<>();
   }

   public EmailRecipient(String oid) {
      this.oid = "";
      this.oidType = "";
      this.email = "";
      this.languageCode = "FI";
      this.attachments = new LinkedList<>();
      this.attachInfo = new LinkedList<>();
      this.oid = oid;
   }

   public EmailRecipient(String oid, String email) {
      this.oid = "";
      this.oidType = "";
      this.email = "";
      this.languageCode = "FI";
      this.attachments = new LinkedList<>();
      this.attachInfo = new LinkedList<>();
      this.oid = oid;
      this.email = email;
   }

   public EmailRecipient(String oid, String oidType, String email, String languageCode) {
      this.oid = "";
      this.oidType = "";
      this.email = "";
      this.languageCode = "FI";
      this.attachments = new LinkedList<>();
      this.attachInfo = new LinkedList<>();
      this.oid = oid;
      this.oidType = oidType;
      this.email = email;
      this.languageCode = languageCode;
   }

   public EmailRecipient(String oid, String oidType, String email, String languageCode, List<ReportedRecipientReplacementDTO> recipientReplacements) {
      this(oid, oidType, email, languageCode);
      this.recipientReplacements = recipientReplacements;
   }

   public String getOid() {
      return this.oid;
   }

   public String getOidType() {
      return this.oidType;
   }

   public String getLanguageCode() {
      return this.languageCode;
   }

   public String getEmail() {
      return this.email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public List<ReportedRecipientReplacementDTO> getRecipientReplacements() {
      return this.recipientReplacements;
   }

   public void setRecipientReplacements(List<ReportedRecipientReplacementDTO> recipientReplacements) {
      this.recipientReplacements = recipientReplacements;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public List<EmailAttachment> getAttachments() {
      return this.attachments;
   }

   public void addAttachInfo(AttachmentResponse attachInfo) {
      if (this.attachInfo == null) {
         this.attachInfo = new ArrayList<>();
      }

      this.attachInfo.add(attachInfo);
   }

   public void setAttachments(List<EmailAttachment> attachments) {
      this.attachments = attachments;
   }

   public List<AttachmentResponse> getAttachInfo() {
      return this.attachInfo;
   }

   public void setAttachInfo(List<AttachmentResponse> attachInfo) {
      this.attachInfo = attachInfo;
   }

   public String toString() {
      return "EmailRecipient [oid=" + this.oid + ", oidType=" + this.oidType + ", email=" + this.email + ", languageCode=" + this.languageCode + ", name=" + this.name + ", recipientReplacements=" + this.recipientReplacements + "]";
   }
}
