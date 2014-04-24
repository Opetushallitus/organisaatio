/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 */
package fi.vm.sade.organisaatio.service.oid;

import fi.vm.sade.oid.service.ExceptionMessage;
import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.oid.service.types.NodeClassCode;
import fi.vm.sade.oid.service.types.NodeClassData;

import fi.vm.sade.organisaatio.dao.impl.OrganisaatioDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoArvoDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoElementtiDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietojenTyyppiDAOImpl;
import fi.vm.sade.organisaatio.model.Organisaatio;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Organisaation OID generointi toimii kuin OIDServiceMock, mutta tarkistaa ettei
 * kannassa jo ole generoitua OID:a.
 * 
 * Esimerkkejä generoiduista OID:sta:
 * <pre>
 * 1.2.246.562.5.2013052409561013582688
 * 1.2.246.562.5.2013052409562689910998
 * </pre>
 *
 * (<a>https://confluence.oph.ware.fi/confluence/display/TD/OID-palvelu</a>)
 * 
 * @author simok 
 */
public class OrganisaatioOIDServiceImpl implements OIDService {
    @Autowired
    private OrganisaatioDAOImpl organisaatioDAO;

    @Autowired
    private YhteystietoDAOImpl yhteystietoDAO;

    @Autowired
    private YhteystietoArvoDAOImpl yhteystietoArvoDAO;
    
    @Autowired
    private YhteystietoElementtiDAOImpl yhteystietoElementtiDAO;
            
    @Autowired
    private YhteystietojenTyyppiDAOImpl yhteystietojenTyyppiDAO;
            
    
    private static final Logger LOG = Logger.getAnonymousLogger();

    private final String root = "1.2.246.562.";
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

    @Override
    public String newOidByClassValue(String nodeClassValue) throws ExceptionMessage {
        return generateOid(nodeClassValue);
    }

    @Override
    public List<NodeClassData> getNodeClasses() throws ExceptionMessage {
        List<NodeClassData> list = new ArrayList<NodeClassData>();

        for (int i = 0; i < values.length; i++) {
            NodeClassData data = new NodeClassData();
            data.setClassCode(codes[i]);
            data.setNodeValue(values[i]);
            data.setDescription(i + "");
            list.add(data);
        }

        return list;
    }

    @Override
    public String newOid(NodeClassCode nodeClass) throws ExceptionMessage {

        int valueIndex = -1;
        for (int i = 0; i < codes.length; ++i) {
            if (codes[i].equals(nodeClass)) {
                valueIndex = i;
                break;
            }
        }
        if (valueIndex < 0) {
            LOG.warning("It seems that there is a new NodeClassCode defined, please update " +
                    this.getClass().getSimpleName() + "! NodeClassCode = " + nodeClass);
            // Generate TEKN_5 oid
            valueIndex = 0;
        }
        
        return generateOid(values[valueIndex]);
    }

    private String generateOid(String nodeClassValue) {
        boolean generateNew = true;
        String newOid = null;
        
        while (generateNew) {
            newOid = root + nodeClassValue + "." + generateRandom();
            
            if (oidAvailable(newOid, nodeClassValue)) {
                generateNew = false;
            }
        }
        
        return newOid;
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

    private boolean oidAvailable(String oid, String nodeClassValue) {
        NodeClassCode nodeClass = nodeClassValueToCode(nodeClassValue);

        if (nodeClassValue.equals("28") || nodeClass == NodeClassCode.TOIMIPAIKAT) {
            // Organisaation ja ryhmän OID:t löytyvät organisaatio-taulusta
            Organisaatio org = organisaatioDAO.findByOid(oid);

            // Jos organisaatio löytyy annetulla oidilla, niin se ei ole vapaana
            return (org == null) ? true : false;
        }
        else if (nodeClass == NodeClassCode.TEKN_5) {
            // Yhteystietoihin liittyvät OID:t löytyvät neljästä eri taulusta
            if (yhteystietoDAO.findBy("yhteystietooid", oid).size() > 0) {
                return false;
            }
            if (yhteystietoArvoDAO.findBy("yhteystietoarvooid", oid).size() > 0) {
                return false;
            }
            if (yhteystietoElementtiDAO.findBy("oid", oid).size() > 0) {
                return false;
            }
            if (yhteystietojenTyyppiDAO.findBy("oid", oid).size() > 0) {
                return false;
            }            
            // Yhteystietoihin liittyvistä tauluista ei löytynyt oidia
            return true;
        }
        
        LOG.log(Level.WARNING, "Unknown node class value: {0}", nodeClassValue);
        
        return false;
    }
    
    private NodeClassCode nodeClassValueToCode(String nodeClassValue) {
        int valueIndex = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(nodeClassValue)) {
                valueIndex = i;
                break;
            }
        }

        if (valueIndex >= 0) {
            return codes[valueIndex];
        }

        return null;
    }
}
