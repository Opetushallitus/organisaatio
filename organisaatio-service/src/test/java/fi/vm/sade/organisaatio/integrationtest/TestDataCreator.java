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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dao.impl.OrganisaatioDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.OrganisaatioSuhdeDAOImpl;
import fi.vm.sade.organisaatio.model.Email;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.model.Puhelinnumero;
import fi.vm.sade.organisaatio.model.Www;
import fi.vm.sade.organisaatio.model.Yhteystieto;

/**
 *
 * @author Tuomas Katva
 */
@Component
public class TestDataCreator {

    @Autowired
    OrganisaatioDAOImpl organisaatioDAO;
    @Autowired
    OrganisaatioSuhdeDAOImpl organisaatioSuhdeDAO;

    public void createInitialTestData() {

        // create initial test organisations - TODO: for testing purposes
        Calendar futureStart = Calendar.getInstance();
        futureStart.set(2013, 5, 29);
        Calendar pastStop = Calendar.getInstance();
        pastStop.set(2011, 5, 29);

        Organisaatio rootOfAllEvil = initCreateOrLoad("opetushallitus", "6666666-6", null, null, null, null, "1.2.246.562.24.00000000001");

        Organisaatio root = initCreateOrLoad("root test koulutustoimija", "1234567-1", rootOfAllEvil, null, null, null, "1.2.2004.1");
        Organisaatio node1 = initCreateOrLoad("node1 asd", "1234567-2", root, futureStart.getTime(), null, "Ammattikorkeakoulut", "1.2.2004.2");
        Organisaatio node2 = initCreateOrLoad("node2 foo", "1234567-3", root, null, pastStop.getTime(), "Yliopistot", "1.2.2004.3");
        Organisaatio node22 = initCreateOrLoad("node22 foo bar", "1234567-4", node2, null, null, "Yliopistot", "1.2.2004.4");
        Organisaatio root2 = initCreateOrLoad("root2 test2 koulutustoimija2", "1234567-5", rootOfAllEvil, futureStart.getTime(), null, null, "1.2.2004.5");
        Organisaatio nodex = initCreateOrLoad("nodex bar", "1234567-6", root2, null, pastStop.getTime(), "Ammattikorkeakoulut", "1.2.2004.6");
    }

    private Organisaatio initCreateOrLoad(String nimi, String ytunnus, Organisaatio parent, Date startDate, Date endDate, String oppilaitostyyppi, String oid) {
        try {
            Organisaatio org = organisaatioDAO.findByOid(oid);
            if (org != null) {
                return org;
            }
            Organisaatio organisaatio = new Organisaatio();
            organisaatio.setOid(oid);
            organisaatio.setNimi(setNimiValue("fi", nimi));//((Fi(nimi);
            organisaatio.setNimihaku(nimi);
            organisaatio.setNimiLyhenne(nimi.substring(0, 8));
            organisaatio.setYtunnus(ytunnus);
            organisaatio.setKotipaikka("Helsinki");
            organisaatio.setYritysmuoto("oy");
            organisaatio.setAlkuPvm(startDate);
            organisaatio.setLakkautusPvm(endDate);
            organisaatio.setOrganisaatioPoistettu(false);
            if (parent != null) {
                if (!parent.getOid().equals("1.2.246.562.24.00000000001")) {
                organisaatio.setTyypit(Arrays.asList(new String[]{OrganisaatioTyyppi.TOIMIPISTE.value(), OrganisaatioTyyppi.OPPILAITOS.value(), OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value()}));
                organisaatio.setOppilaitosTyyppi(oppilaitostyyppi);
                } else {
                organisaatio.setTyypit(Arrays.asList(new String[]{OrganisaatioTyyppi.KOULUTUSTOIMIJA.value()}));
                }
            } else  {
                organisaatio.setTyypit(Arrays.asList(new String[]{OrganisaatioTyyppi.KOULUTUSTOIMIJA.value()}));
            }

            createYhteystietos(organisaatio);

            organisaatio = organisaatioDAO.insert(organisaatio);

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

    private MonikielinenTeksti setNimiValue(String lang, String value) {
    	MonikielinenTeksti nimi = new MonikielinenTeksti();
    	nimi.addString(lang, value);
    	return nimi;
    }

    private void createYhteystietos(Organisaatio organisaatio) {
        List<Yhteystieto> yhteystiedot = new ArrayList<Yhteystieto>();
        Osoite osoite1 = new Osoite(Osoite.TYYPPI_POSTIOSOITE, "Mannerheimintie 1", "00100", "Helsinki", null);
        osoite1.setOrganisaatio(organisaatio);
        yhteystiedot.add(osoite1);
        Osoite osoite2 = new Osoite(Osoite.TYYPPI_KAYNTIOSOITE, "Mannerheimintie 1", "00100", "Helsinki", null);
        osoite2.setOrganisaatio(organisaatio);
        yhteystiedot.add(osoite2);
        Puhelinnumero puh1 = new Puhelinnumero("12345", Puhelinnumero.TYYPPI_PUHELIN, null);
        puh1.setOrganisaatio(organisaatio);
        yhteystiedot.add(puh1);
        Puhelinnumero puh2 = new Puhelinnumero("12345", Puhelinnumero.TYYPPI_FAKSI, null);
        puh2.setOrganisaatio(organisaatio);
        yhteystiedot.add(puh2);
        Www www = new Www();
        www.setWwwOsoite("http://www.oph.fi");
        www.setOrganisaatio(organisaatio);
        yhteystiedot.add(www);
        Email email = new Email();
        email.setEmail("testi@oph.fi");
        email.setOrganisaatio(organisaatio);
        yhteystiedot.add(email);

        organisaatio.setYhteystiedot(yhteystiedot);

    }

    private void setParentPaths(Organisaatio o, String parentOid) {
        String parentOidpath = "";
        String parentIdPath = "";
        for (Organisaatio curOrg : this.organisaatioDAO.findParentsTo(parentOid)) {
            parentOidpath += "|" + curOrg.getOid();
            parentIdPath += "|" + curOrg.getId();
        }
        parentOidpath += "|";
        parentIdPath += "|";
        o.setParentOidPath(parentOidpath);
        o.setParentIdPath(parentIdPath);
    }

}
