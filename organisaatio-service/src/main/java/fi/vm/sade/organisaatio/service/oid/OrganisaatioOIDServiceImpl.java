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

import fi.vm.sade.oid.ExceptionMessage;
import fi.vm.sade.oid.OIDService;
import fi.vm.sade.oid.NodeClassCode;
import fi.vm.sade.oidgenerator.OIDGenerator;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

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
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioRepository organisaatioRepository;

    @Autowired
    private YhteystietoRepository yhteystietoRepository;

    @Autowired
    private YhteystietoArvoRepository yhteystietoArvoRepository;

    @Autowired
    private YhteystietoElementtiRepository yhteystietoElementtiRepository;

    @Autowired
    private YhteystietojenTyyppiRepository yhteystietojenTyyppiRepository;

    @Value("${organisaatio.solmuluokka}")
    private String organisaatioSolmuluokka;

    @Override
    public String newOidByClassValue(String nodeClassValue) throws ExceptionMessage {
        return generateOid(nodeClassValue);
    }

    @Override
    public String newOid(NodeClassCode nodeClass) throws ExceptionMessage {
        return newOidByClassValue(mapping().get(nodeClass));
    }

    private String generateOid(String nodeClassValue) {
        LOG.debug("Generating new OID for node class: " + nodeClassValue);
        boolean generateNew = true;
        String newOid = null;

        while (generateNew) {
            newOid = OIDGenerator.generateOID(Integer.parseInt(nodeClassValue));
            if (oidAvailable(newOid, nodeClassValue)) {
                generateNew = false;
            }
        }

        LOG.debug("New oid generated: " + newOid);
        return newOid;
    }

    private boolean oidAvailable(String oid, String solmuluokka) {
        if (check(solmuluokka, NodeClassCode.TOIMIPAIKAT) || check(solmuluokka, NodeClassCode.RYHMA) || check(solmuluokka, NodeClassCode.PROD_TOIMIPAIKAT)) {
            // Organisaation ja ryhmän OID:t löytyvät organisaatio-taulusta
            Organisaatio org = organisaatioRepository.findFirstByOid(oid);

            // Jos organisaatio löytyy annetulla oidilla, niin se ei ole vapaana
            return (org == null);
        } else if (check(solmuluokka, NodeClassCode.TEKN_5)) {
            try {
                // Yhteystietoihin liittyvät OID:t löytyvät neljästä eri taulusta
                if (yhteystietoRepository.findByYhteystietoOid(oid).size() > 0) {
                    return false;
                }
                if (yhteystietoArvoRepository.findByYhteystietoArvoOid(oid).size() > 0) {
                    return false;
                }
                if (yhteystietoElementtiRepository.findByOid(oid).size() > 0) {
                    return false;
                }
                if (yhteystietojenTyyppiRepository.findByOid(oid).size() > 0) {
                    return false;
                }
            } catch (Exception ex) {
                LOG.warn("Failed to check if yhteystiedot exists by oid: " + oid, ex);
            }
            // Yhteystietoihin liittyvistä tauluista ei löytynyt oidia
            return true;
        }

        LOG.warn("Tuntematon solmuluokka: " + solmuluokka);
        return false;
    }

    private boolean check(String solmuluokka, NodeClassCode nodeClass) {
        return mapping().get(nodeClass).equals(solmuluokka);
    }

    private Map<NodeClassCode, String> mapping() {
        var map = new HashMap<NodeClassCode, String>();
        map.put(NodeClassCode.TEKN_5, "5");
        map.put(NodeClassCode.TEKN_6, "6");
        map.put(NodeClassCode.PROD_TOIMIPAIKAT, "10");
        map.put(NodeClassCode.ASIAKIRJAT, "11");
        map.put(NodeClassCode.OHJELMISTOT, "12");
        map.put(NodeClassCode.LAITTEET, "13");
        map.put(NodeClassCode.PALVELUT, "14");
        map.put(NodeClassCode.LASKUTUS, "16");
        map.put(NodeClassCode.LOGISTIIKKA, "17");
        map.put(NodeClassCode.SANOMALIIKENNE, "18");
        map.put(NodeClassCode.REKISTERINPITAJA, "19");
        map.put(NodeClassCode.NAYTETUNNISTE, "20");
        map.put(NodeClassCode.TILAP_ASIAKAS, "22");
        map.put(NodeClassCode.HENKILO, "24");
        map.put(NodeClassCode.ROOLI, "27");
        map.put(NodeClassCode.RYHMA, "28");
        map.put(NodeClassCode.TOIMIPAIKAT, organisaatioSolmuluokka);
        return map;
    }
}
