package fi.vm.sade.organisaatio;

import fi.vm.sade.oid.ExceptionMessage;
import fi.vm.sade.oid.NodeClassCode;
import fi.vm.sade.oid.NodeClassData;
import fi.vm.sade.oidgenerator.OIDGenerator;
import fi.vm.sade.organisaatio.service.oid.OidService;

import java.util.ArrayList;
 import java.util.List;

 public class OIDServiceMock implements OidService {
     private final String[] values = new String[]{"5", "6", "10", "11", "12", "13", "14", "16", "17", "18", "19", "20",
         "22", "24", "27"};
     private final NodeClassCode[] codes = new NodeClassCode[] {
                                         NodeClassCode.TEKN_5,
                                         NodeClassCode.TEKN_6,
                                         NodeClassCode.TOIMIPAIKAT,
                                         NodeClassCode.ASIAKIRJAT,
                                         NodeClassCode.OHJELMISTOT,
                                         NodeClassCode.LAITTEET,
                                         NodeClassCode.PALVELUT,
                                         NodeClassCode.LASKUTUS,
                                         NodeClassCode.LOGISTIIKKA,
                                         NodeClassCode.SANOMALIIKENNE,
                                         NodeClassCode.REKISTERINPITAJA,
                                         NodeClassCode.NAYTETUNNISTE,
                                         NodeClassCode.TILAP_ASIAKAS,
                                         NodeClassCode.HENKILO,
                                         NodeClassCode.ROOLI
                                         };

     public String newOidByClassValue(String nodeClassValue) throws ExceptionMessage {
         return generateOid(nodeClassValue);
     }

     public List<NodeClassData> getNodeClasses() throws ExceptionMessage {
         List<NodeClassData> list = new ArrayList<>();

         for (int i = 0; i < values.length; i++) {
             NodeClassData data = new NodeClassData();
             data.setClassCode(codes[i]);
             data.setNodeValue(values[i]);
             data.setDescription(i + "");
             list.add(data);
         }

         return list;
     }

     public String newOid(NodeClassCode nodeClass) throws ExceptionMessage {

         int valueIndex = -1;
         for (int i = 0; i < codes.length; ++i) {
             if (codes[i].equals(nodeClass)) {
                 valueIndex = i;
                 break;
             }
         }
         if (valueIndex < 0) {
             // Generate TEKN_5 oid
             valueIndex = 0;
         }

         return generateOid(values[valueIndex]);
     }

     private String generateOid(String nodeClassValue) {
         String newOid = OIDGenerator.generateOID(Integer.parseInt(nodeClassValue));
         return newOid;
     }
 }
