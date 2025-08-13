package fi.vm.sade.organisaatio.resource;

import java.util.Random;

import fi.vm.sade.oid.ExceptionMessage;
import fi.vm.sade.oid.OIDService;
import fi.vm.sade.oid.NodeClassCode;

public class OIDServiceMock implements OIDService {

    private String root = "1.2.246.562.";

    private String[] values = new String[] { "5", "6", "10", "11", "12", "13", "14", "16", "17", "18", "19", "20",
            "22", "24", "27", "28", "10" };

    private NodeClassCode[] codes = new NodeClassCode[] {
                                        NodeClassCode.TEKN_5,
                                        NodeClassCode.TEKN_6,
                                        NodeClassCode.PROD_TOIMIPAIKAT,
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
                                        NodeClassCode.ROOLI,
                                        NodeClassCode.RYHMA,
                                        NodeClassCode.TOIMIPAIKAT,
                                        };

    @Override
    public String newOidByClassValue(String nodeClassValue) throws ExceptionMessage {

        return root + nodeClassValue + "." + generateRandom();
    }

    @Override
    public String newOid(NodeClassCode nodeClass) throws ExceptionMessage {

        int valueIndex = 0;
        for (int i = 0; i < codes.length; ++i) {
            if (codes[i].equals(nodeClass)) {
                valueIndex = i;
                break;
            }
        }
        return root + values[valueIndex] + "." + generateRandom();
    }

    private String generateRandom() {

        long min = 1000000000L;
        long max = 10000000000L;

        Random r = new Random();
        long number = min + ((long) (r.nextDouble() * (max - min)));

        String n = Long.toString(number);
        n += luhnChecksum(number);
        return n;
    }

    private int luhnChecksum(Long oid) {
        String oidStr = oid.toString();

        int sum = 0;
        boolean alternate = false;

        for (int i = oidStr.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(oidStr.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return sum % 10;
    }

}
