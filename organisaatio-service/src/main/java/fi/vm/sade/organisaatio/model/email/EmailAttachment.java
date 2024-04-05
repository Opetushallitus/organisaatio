package fi.vm.sade.organisaatio.model.email;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

public class EmailAttachment {
   private byte[] data;
   private String name;
   private String contentType;

   public EmailAttachment() {
   }

   public EmailAttachment(String attachmentFile) {
      this.name = attachmentFile;
      DataSource ds = new FileDataSource(attachmentFile);
      this.contentType = ds.getContentType();
   }

   public String getName() {
      return this.name;
   }

   public String getContentType() {
      return this.contentType;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setContentType(String contentType) {
      this.contentType = contentType;
   }

   public byte[] getData() {
      return this.data;
   }

   public void setData(byte[] data) {
      this.data = data;
   }

   public String toString() {
      return "EmailAttachment [name=" + this.name + ", contentType=" + this.contentType + "]";
   }
}
