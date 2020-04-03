/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
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
 * European Union Public Licence for more details.
 */

package fi.vm.sade.organisaatio.integrationtest;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dao.OrganisaatioNimiDAO;
import fi.vm.sade.organisaatio.dao.impl.OrganisaatioDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.OrganisaatioSuhdeDAOImpl;
import fi.vm.sade.organisaatio.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Tuomas Katva
 */
@Component
@Transactional
public class TestDataCreator {

    @Autowired
    OrganisaatioDAOImpl organisaatioDAO;
    @Autowired
    OrganisaatioSuhdeDAOImpl organisaatioSuhdeDAO;
    @Autowired
    OrganisaatioNimiDAO organisaatioNimiDAO;

    public void createInitialTestData() {

        // create initial test organisations - TODO: for testing purposes
        Calendar futureStart = Calendar.getInstance();
        futureStart.set(2013, 5, 29);
        Calendar pastStop = Calendar.getInstance();
        pastStop.set(2011, 5, 29);

        Organisaatio rootOfAllEvil = initCreateOrLoad("opetushallitus", "6666666-6", null, null, null, null, "1.2.246.562.24.00000000001");

        Organisaatio root = initCreateOrLoad("root test koulutustoimija", "1234567-1", rootOfAllEvil, null, null, null, "1.2.2004.1");
        Organisaatio node1 = initCreateOrLoad("node1 asd", "1234567-2", root, futureStart.getTime(), null, "oppilaitostyyppi_41#1", "1.2.2004.2");
        Organisaatio node2 = initCreateOrLoad("node2 foo", "1234567-3", root, null, pastStop.getTime(), "oppilaitostyyppi_42#1", "1.2.2004.3");
        Organisaatio node22 = initCreateOrLoad("node22 foo bar", "1234567-4", node2, null, null, "oppilaitostyyppi_42#1", "1.2.2004.4");
        Organisaatio node23 = initCreateOrLoad("node23 foo bar", "1234568-4", node2, null, null, "oppilaitostyyppi_42#1", "1.2.2005.4");
        Organisaatio root2 = initCreateOrLoad("root2 test2 koulutustoimija2", "1234567-5", rootOfAllEvil, futureStart.getTime(), null, null, "1.2.2004.5");
        Organisaatio nodex = initCreateOrLoad("nodex bar", "1234567-6", root2, null, pastStop.getTime(), "oppilaitostyyppi_41#1", "1.2.2004.6");
        Organisaatio nodey = initCreateOrLoad("nodey bar", "1234567-7", rootOfAllEvil, null, pastStop.getTime(), null, "1.2.2004.7");
    }

    private Organisaatio initCreateOrLoad(String nimi, String ytunnus, Organisaatio parent, Date startDate, Date endDate, String oppilaitostyyppi, String oid) {
        try {
            Organisaatio org = organisaatioDAO.findByOid(oid);
            if (org != null) {
                return org;
            }
            Organisaatio organisaatio = new Organisaatio();
            organisaatio.setOid(oid);
            OrganisaatioNimi orgNimi = createNimi("fi", nimi);
            organisaatio.setNimi(orgNimi.getNimi());//((Fi(nimi);
            organisaatio.setNimihaku(nimi);
            organisaatio.setYtunnus(ytunnus);
            organisaatio.setKotipaikka("Helsinki");
            organisaatio.setYritysmuoto("oy");
            organisaatio.setAlkuPvm(startDate);
            organisaatio.setLakkautusPvm(endDate);
            organisaatio.setOrganisaatioPoistettu(false);
            if (parent != null) {
                if (!parent.getOid().equals("1.2.246.562.24.00000000001")) {
                    organisaatio.setTyypit(new HashSet<>(Arrays.asList(OrganisaatioTyyppi.TOIMIPISTE.koodiValue(), OrganisaatioTyyppi.OPPILAITOS.koodiValue(), OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.koodiValue())));
                    organisaatio.setOppilaitosTyyppi(oppilaitostyyppi);
                } else if (organisaatio.getOid().equals("1.2.2004.7")) {
                    organisaatio.setTyypit(Collections.singleton(OrganisaatioTyyppi.TYOELAMAJARJESTO.koodiValue()));
                } else {
                    organisaatio.setTyypit(Collections.singleton(OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue()));
                }
            } else {
                organisaatio.setTyypit(Collections.singleton(OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue()));
            }

            createYhteystietos(organisaatio);

            organisaatio = organisaatioDAO.insert(organisaatio);

            orgNimi.setOrganisaatio(organisaatio);
            organisaatioNimiDAO.insert(orgNimi);

            if (parent != null) {
                OrganisaatioSuhde suhde = organisaatioSuhdeDAO.addChild(parent.getId(), organisaatio.getId(), Calendar.getInstance().getTime(), null);
                organisaatio.getParentSuhteet().add(suhde); //organisaatioDAO.findByOid(organisaatio.getOid());
                setParentPaths(organisaatio, parent.getOid());
            }

            return organisaatio;
        } catch (Exception exp) {
            exp.printStackTrace();
            return new Organisaatio();
        }
    }

    private OrganisaatioNimi createNimi(String lang, String nimi) {
        MonikielinenTeksti monikielinenTeksti = setNimiValue(lang, nimi);
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.setNimi(monikielinenTeksti);
        organisaatioNimi.setAlkuPvm(new Date(0));
        return organisaatioNimi;
    }

    private MonikielinenTeksti setNimiValue(String lang, String value) {
        MonikielinenTeksti nimi = new MonikielinenTeksti();
        nimi.addString(lang, value);

        return nimi;
    }

    private void createYhteystietos(Organisaatio organisaatio) {
        Set<Yhteystieto> yhteystiedot = new HashSet<>();
        Osoite osoite1 = new Osoite(Osoite.TYYPPI_POSTIOSOITE, "Mannerheimintie 1", "00100", "Helsinki", null);
        osoite1.setOrganisaatio(organisaatio);
        yhteystiedot.add(osoite1);
        Osoite osoite2 = new Osoite(Osoite.TYYPPI_KAYNTIOSOITE, "Mannerheimintie 1", "00100", "Helsinki", null);
        osoite2.setOrganisaatio(organisaatio);
        yhteystiedot.add(osoite2);
        Puhelinnumero puh1 = new Puhelinnumero("12345", Puhelinnumero.TYYPPI_PUHELIN, null);
        puh1.setOrganisaatio(organisaatio);
        yhteystiedot.add(puh1);
        Www www = new Www();
        www.setWwwOsoite("http://www.oph.fi");
        www.setOrganisaatio(organisaatio);
        yhteystiedot.add(www);
        Email email = new Email();
        email.setEmail("testi@oph.fi");
        email.setOrganisaatio(organisaatio);
        yhteystiedot.add(email);

        for (Yhteystieto yt : yhteystiedot) {
            yt.setKieli("kieli_fi#1");
        }
        organisaatio.setYhteystiedot(yhteystiedot);

    }

    private void setParentPaths(Organisaatio o, String parentOid) {
        String parentIdPath = "";
        List<Organisaatio> parents = this.organisaatioDAO.findParentsTo(parentOid);
        for (Organisaatio curOrg : parents) {
            parentIdPath += "|" + curOrg.getId();
        }
        parentIdPath += "|";
        o.setParentIdPath(parentIdPath);
        List<String> parentOids = parents.stream().map(Organisaatio::getOid).collect(Collectors.collectingAndThen(
                Collectors.toList(), strings -> {
                    Collections.reverse(strings);
                    return strings;
                }
        ));
        o.setParentOids(parentOids);
    }

}
