package fi.vm.sade.organisaatio.model.email;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EmailMessage {
   private String callingProcess;
   private String from;
   private String sender;
   private String replyTo;
   private String senderOid;
   private String organizationOid;
   private String subject;
   private String body;
   private boolean html;
   private String charset;
   private List<EmailAttachment> attachments;
   private List<AttachmentResponse> attachInfo;
   private boolean isValid;
   private String templateName;
   private String templateId;
   private Long letterId;
   private String languageCode;
   private List<SourceRegister> sourceRegister;
   private String hakuOid;

   public EmailMessage() {
      this.callingProcess = "";
      this.html = false;
      this.charset = "UTF-8";
      this.attachments = new LinkedList<>();
      this.attachInfo = new LinkedList<>();
      this.isValid = true;
   }

   public EmailMessage(String callingProcess, String from, String replyTo, String subject, String body) {
      this.callingProcess = "";
      this.html = false;
      this.charset = "UTF-8";
      this.attachments = new LinkedList<>();
      this.attachInfo = new LinkedList<>();
      this.isValid = true;
      this.callingProcess = callingProcess;
      this.from = from;
      this.replyTo = replyTo;
      this.subject = subject;
      this.body = body;
   }

   public EmailMessage(String callingProcess, String from, String replyTo, String subject, String templateName, String languageCode, List<SourceRegister> sourceRegister) {
      this(callingProcess, from, replyTo, subject, "");
      this.templateName = templateName;
      this.languageCode = languageCode;
      this.sourceRegister = sourceRegister;
   }

   public void setBody(String body) {
      this.body = body;
   }

   public String getCallingProcess() {
      return this.callingProcess;
   }

   public String getFrom() {
      return this.from;
   }

   public void setFrom(String from) {
      this.from = from;
   }

   public String getSender() {
      return this.sender;
   }

   public void setSender(String sender) {
      this.sender = sender;
   }

   public String getReplyTo() {
      return this.replyTo;
   }

   public void setReplyTo(String replyTo) {
      this.replyTo = replyTo;
   }

   public String getBody() {
      return this.body;
   }

   public void setSubject(String subject) {
      this.subject = subject;
   }

   public String getSubject() {
      return this.subject;
   }

   public String getSenderOid() {
      return this.senderOid;
   }

   public void setSenderOid(String senderOid) {
      this.senderOid = senderOid;
   }

   public String getOrganizationOid() {
      return this.organizationOid;
   }

   public void setOrganizationOid(String organizationOid) {
      this.organizationOid = organizationOid;
   }

   public boolean isHtml() {
      return this.html;
   }

   public void setHtml(boolean html) {
      this.html = html;
   }

   public String getCharset() {
      return this.charset;
   }

   public void setCharset(String charset) {
      this.charset = charset;
   }

   public void addEmailAttachement(EmailAttachment attachment) {
      if (this.attachments == null) {
         this.attachments = new ArrayList<>();
      }

      this.attachments.add(attachment);
   }

   public void setAttachments(List<EmailAttachment> attachments) {
      this.attachments = attachments;
   }

   public List<EmailAttachment> getAttachments() {
      return this.attachments;
   }

   public void addAttachInfo(AttachmentResponse attachInfo) {
      if (this.attachInfo == null) {
         this.attachInfo = new LinkedList<>();
      }

      this.attachInfo.add(attachInfo);
   }

   public List<AttachmentResponse> getAttachInfo() {
      return this.attachInfo;
   }

   public void setAttachInfo(List<AttachmentResponse> attachInfo) {
      this.attachInfo = attachInfo;
   }

   public void setCallingProcess(String callingProcess) {
      this.callingProcess = callingProcess;
   }

   public String getHakuOid() {
      return this.hakuOid;
   }

   public void setHakuOid(String hakuOid) {
      this.hakuOid = hakuOid;
   }

   public String getTemplateId() {
      return this.templateId;
   }

   public void setTemplateId(String templateId) {
      this.templateId = templateId;
   }

   public Long getLetterId() {
      return this.letterId;
   }

   public void setLetterId(Long letterId) {
      this.letterId = letterId;
   }

   public String getTemplateName() {
      return this.templateName;
   }

   public void setTemplateName(String templateName) {
      this.templateName = templateName;
   }

   public String getLanguageCode() {
      return this.languageCode;
   }

   public void setLanguageCode(String languageCode) {
      this.languageCode = languageCode;
   }

   public boolean isValid() {
      return this.isValid;
   }

   public void setInvalid() {
      this.isValid = false;
   }

   public List<SourceRegister> getSourceRegister() {
      return this.sourceRegister;
   }

   public void setSourceRegister(List<SourceRegister> sourceRegister) {
      this.sourceRegister = sourceRegister;
   }

   public String toString() {
      return "EmailMessage [callingProcess=" + this.callingProcess + ", from=" + this.from + ", sender=" + this.sender + ", replyTo=" + this.replyTo + ", senderOid=" + this.senderOid + ", organizationOid=" + this.organizationOid + ", subject=" + this.subject + ", body=" + this.body + ", html=" + this.html + ", charset=" + this.charset + ", attachments=" + this.attachments + ", attachInfo=" + this.attachInfo + ", isValid=" + this.isValid + ", templateName=" + this.templateName + ", templateId=" + this.templateId + ", languageCode=" + this.languageCode + ", sourceRegister=" + this.sourceRegister + ", hakuOid=" + this.hakuOid + "]";
   }
}
