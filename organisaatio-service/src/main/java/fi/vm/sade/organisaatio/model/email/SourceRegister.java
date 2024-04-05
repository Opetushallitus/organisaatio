package fi.vm.sade.organisaatio.model.email;

public class SourceRegister {
   private String name;

   public SourceRegister() {
   }

   public SourceRegister(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }
}
