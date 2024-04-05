package fi.vm.sade.organisaatio.model.email;

import java.util.HashMap;
import java.util.Map;

public class AttachmentResponse {
   private String uuid;
   private String fileName;
   private String contentType;
   private int fileSize;

   public AttachmentResponse() {
   }

   public String getUuid() {
      return this.uuid;
   }

   public void setUuid(String uuid) {
      this.uuid = uuid;
   }

   public String getFileName() {
      return this.fileName;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }

   public String getContentType() {
      return this.contentType;
   }

   public void setContentType(String contentType) {
      this.contentType = contentType;
   }

   public int getFileSize() {
      return this.fileSize;
   }

   public void setFileSize(int fileSize) {
      this.fileSize = fileSize;
   }

   public Map<String, String> toMap() {
      Map<String, String> result = new HashMap<>();
      result.put("uuid", this.uuid);
      result.put("fileName", this.fileName);
      result.put("contentType", this.contentType);
      result.put("fileSize", "" + this.fileSize);
      return result;
   }

   public String toString() {
      return "AttachmentResponse [uuid=" + this.uuid + ", fileName=" + this.fileName + ", contentType=" + this.contentType + ", fileSize=" + this.fileSize + "]";
   }
}
