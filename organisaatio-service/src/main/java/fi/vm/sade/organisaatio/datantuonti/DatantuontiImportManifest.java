package fi.vm.sade.organisaatio.datantuonti;

record DatantuontiManifest(String organisaatio, String osoite) {}
record DatantuontiExportManifest(String organisaatio, String osoite, String transactionTimestamp) {}
