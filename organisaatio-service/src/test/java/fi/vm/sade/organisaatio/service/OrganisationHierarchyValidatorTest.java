package fi.vm.sade.organisaatio.service;

import java.util.List;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;

public class OrganisationHierarchyValidatorTest {

    private static final String ROOT_OID = "1";
    private Organisaatio root;

    @Before
    public void setup() {
        root = new Organisaatio();
        root.setOid(ROOT_OID);
    }

    @Test
    public void testKoulutustoimija() {
        final OrganisationHierarchyValidator validator = new OrganisationHierarchyValidator(ROOT_OID);

        final Predicate<Entry<Organisaatio, Organisaatio>> validator2 = validator.koulutustoimijaRule;
        final Organisaatio koulutustoimija = getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA);
        
        //TODO this passes now but should it???
        assertResult(null, koulutustoimija, true, validator, validator2);

        // ok
        assertResult(root, koulutustoimija, true, validator, validator2);

        // ok
        assertResult(getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO), koulutustoimija, true, validator, validator2);

        // !ok
        assertResult(getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA), koulutustoimija, false, validator, validator2);

        // !ok
        assertResult(getOrg(OrganisaatioTyyppi.OPETUSPISTE), koulutustoimija, false, validator, validator2);

        // !ok
        assertResult(getOrg(OrganisaatioTyyppi.OPPILAITOS), koulutustoimija, false, validator, validator2);    
    }

    @Test
    public void testMuuOrganisaatio() {
        final OrganisationHierarchyValidator validator = new OrganisationHierarchyValidator(ROOT_OID);

        final Predicate<Entry<Organisaatio, Organisaatio>> validator2 = validator.muuOrgRule;
        final Organisaatio muuOrganisaatio = getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO);
 
        //TODO this passes now but should it?
        assertResult(null, muuOrganisaatio, true, validator, validator2);

        // ok
        assertResult(root, muuOrganisaatio, true, validator, validator2);

        // ok
        assertResult(getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO), muuOrganisaatio, true, validator, validator2);

        // !ok
        assertResult(getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA), muuOrganisaatio, false, validator, validator2);

        // !ok
        assertResult(getOrg(OrganisaatioTyyppi.OPETUSPISTE), muuOrganisaatio, false, validator, validator2);

        // !ok
        assertResult(getOrg(OrganisaatioTyyppi.OPPILAITOS), muuOrganisaatio, false, validator, validator2);
    }

    @Test
    public void testOppilaitos() {
        final OrganisationHierarchyValidator validator = new OrganisationHierarchyValidator(ROOT_OID);

        final Predicate<Entry<Organisaatio, Organisaatio>> validator2 = validator.oppilaitosRule;

        final Organisaatio oppilaitos = getOrg(OrganisaatioTyyppi.OPPILAITOS);

        // !ok
        assertResult(null, oppilaitos, false, validator, validator2);

        // !ok
        assertResult(root, oppilaitos, false, validator, validator2);

        // !ok
        assertResult(getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO), oppilaitos, false, validator, validator2);

        // ok
        assertResult(getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA), oppilaitos, true, validator, validator2);

        // !ok
        assertResult(getOrg(OrganisaatioTyyppi.OPETUSPISTE), oppilaitos, false, validator, validator2);

        // !ok
        assertResult(getOrg(OrganisaatioTyyppi.OPPILAITOS), oppilaitos, false, validator, validator2);
    }

    @Test
    public void testToimipiste() {
        final OrganisationHierarchyValidator validator = new OrganisationHierarchyValidator(ROOT_OID);

        final Predicate<Entry<Organisaatio, Organisaatio>> validator2 = validator.toimipisteRule;


        // !ok
        final Organisaatio toimipiste = getOrg(OrganisaatioTyyppi.OPETUSPISTE);
        assertResult(root, toimipiste, false, validator, validator2);

        // !ok
        assertResult(getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO), toimipiste, false, validator, validator2);

        // ok
        assertResult(getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA), toimipiste, false, validator, validator2);

        // ok
        assertResult(getOrg(OrganisaatioTyyppi.OPETUSPISTE), toimipiste, true, validator, validator2);

        // ok
        assertResult(getOrg(OrganisaatioTyyppi.OPPILAITOS), toimipiste, true, validator, validator2);
    }

    private void assertResult(Organisaatio parent, Organisaatio organisaatio, boolean expected, Predicate<Entry<Organisaatio, Organisaatio>> validator,
            Predicate<Entry<Organisaatio, Organisaatio>> validator2) {
        Entry<Organisaatio, Organisaatio> parentChild = Maps.immutableEntry(parent,  organisaatio);
        Assert.assertEquals(expected, validator.apply(parentChild));
        Assert.assertEquals(expected, validator2.apply(parentChild));
    }

    private Organisaatio getOrg(OrganisaatioTyyppi... tyypit) {
        final Organisaatio org = new Organisaatio();
        List<String> orgTyypit = Lists.newArrayList(org.getTyypit());
        for (OrganisaatioTyyppi tyyppi : tyypit) {
            orgTyypit.add(tyyppi.value());
        }
        org.setTyypit(orgTyypit);
        return org;
    }

}
