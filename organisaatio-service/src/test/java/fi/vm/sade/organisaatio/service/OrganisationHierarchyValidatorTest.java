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
    private OrganisationHierarchyValidator validator;

    @Before
    public void setup() {
        root = new Organisaatio();
        root.setOid(ROOT_OID);
        validator = new OrganisationHierarchyValidator(ROOT_OID);
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

    /* child == oppisopimustoimipiste */
    @Test
    public void testToplevelOppisopimustoimipiste() {
        assertResult(
                null,
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                false,
                validator,
                validator.oppisopimustoimipisteRule
        );
    }

    @Test
    public void testOppisopimustoimipisteUnderOPT() {
        assertResult(
                root,
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                false,
                validator,
                validator.oppisopimustoimipisteRule
        );
    }

    @Test
    public void testOppisopimustoimipisteUnderKoulutustoimija() {
        assertResult(
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                true,
                validator,
                validator.oppisopimustoimipisteRule
        );
    }

    @Test
    public void testOppisopimustoimipisteUnderMuuOrganisaatio() {
        assertResult(
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                false,
                validator,
                validator.oppisopimustoimipisteRule
        );
    }

    @Test
    public void testOppisopimustoimipisteUnderTyoelamajarjesto() {
        assertResult(
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                false,
                validator,
                validator.oppisopimustoimipisteRule
        );
    }

    @Test
    public void testOppisopimustoimipisteUnderToimipiste() {
        assertResult(
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                false,
                validator,
                validator.oppisopimustoimipisteRule
        );
    }

    @Test
    public void testOppisopimustoimipisteUnderOppilaitos() {
        assertResult(
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                false,
                validator,
                validator.oppisopimustoimipisteRule
        );
    }

    @Test
    public void testOppisopimustoimipisteUnderOppisopimustoimipiste() {
        assertResult(
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                false,
                validator,
                validator.oppisopimustoimipisteRule
        );
    }

    /* child == toimipiste (toimipiste) */
    @Test
    public void testToplevelToimipiste() {
        assertResult(
                null,
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                false,
                validator,
                validator.toimipisteRule
        );
    }

    @Test
    public void testToimipisteUnderOPH() {
        assertResult(
                root,
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                false,
                validator,
                validator.toimipisteRule
        );
    }

    @Test
    public void testToimipisteUnderOppilaitos() {
        assertResult(
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                true,
                validator,
                validator.toimipisteRule
        );
    }

    @Test
    public void testToimipisteUnderMuuOrganisaatio() {
        assertResult(
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                true,
                validator,
                validator.toimipisteRule
        );
    }

    @Test
    public void testToimipisteUnderTyoelamajarjesto() {
        assertResult(
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                true,
                validator,
                validator.toimipisteRule
        );
    }

    @Test
    public void testToimipisteUnderToimipiste() {
        assertResult(
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                true,
                validator,
                validator.toimipisteRule
        );
    }

    @Test
    public void testToimipisteUnderKoulutustoimija() {
        assertResult(
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                false,
                validator,
                validator.toimipisteRule
        );
    }

    @Test
    public void testToimipisteUnderOppisopimustoimipiste() {
        assertResult(
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                false,
                validator,
                validator.toimipisteRule
        );
    }

    /* child == oppilaitos */
    @Test
    public void testToplevelOppilaitos() {
        assertResult(
                null,
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                false,
                validator,
                validator.oppilaitosRule
        );
    }

    @Test
    public void testOppilaitosUnderOPH() {
        assertResult(
                root,
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                false,
                validator,
                validator.oppilaitosRule
        );
    }

    @Test
    public void testOppilaitosUnderMuuOrganisaatio() {
        assertResult(
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                false,
                validator,
                validator.oppilaitosRule
        );
    }

    @Test
    public void testOppilaitosUnderTyoelamajarjesto() {
        assertResult(
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                false,
                validator,
                validator.oppilaitosRule
        );
    }

    @Test
    public void testOppilaitosUnderOppilaitos() {
        assertResult(
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                false,
                validator,
                validator.oppilaitosRule
        );
    }

    @Test
    public void testOppilaitosUnderToimipaikka() {
        assertResult(
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                false,
                validator,
                validator.oppilaitosRule
        );
    }

    @Test
    public void testOppilaitosUnderKoulutustoimija() {
        assertResult(
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                true,
                validator,
                validator.oppilaitosRule
        );
    }

    @Test
    public void testOppilaitosUnderOppisopimustoimipaikka() {
        assertResult(
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                false,
                validator,
                validator.oppilaitosRule
        );
    }

    /* child == koulutustoimija */
    @Test
    public void testToplevelKoulutustoimija() {
        assertResult(
                null,
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                true,
                validator,
                validator.koulutustoimijaRule
        );
    }

    @Test
    public void testKoulutustoimijaUnderOPH() {
        assertResult(
                root,
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                true,
                validator,
                validator.koulutustoimijaRule
        );
    }

    @Test
    public void testKoulutustoimijaUnderKoulutustoimija() {
        assertResult(
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                false,
                validator,
                validator.koulutustoimijaRule
        );
    }

    @Test
    public void testKoulutustoimijaUnderMuuOrganisaatio() {
        assertResult(
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                false,
                validator,
                validator.koulutustoimijaRule
        );
    }

    @Test
    public void testKoulutustoimijaUnderTyoelamajarjesto() {
        assertResult(
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                false,
                validator,
                validator.koulutustoimijaRule
        );
    }

    @Test
    public void testKoulutustoimijaUnderOppilaitos() {
        assertResult(
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                false,
                validator,
                validator.koulutustoimijaRule
        );
    }

    @Test
    public void testKoulutustoimijaUnderToimipiste() {
        assertResult(
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                false,
                validator,
                validator.koulutustoimijaRule
        );
    }

    @Test
    public void testKoulutustoimijaUnderOppisopimustoimipiste() {
        assertResult(
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                false,
                validator,
                validator.koulutustoimijaRule
        );
    }

    /* child == muu organisaatio */
    @Test
    public void testToplevelMuuOrganisaatio() {
        assertResult(
                null,
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
                true,
                validator,
                validator.muuOrgRule
        );
    }

    @Test
    public void testMuuOrganisaatioUnderOPH() {
        assertResult(
                root,
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
                true,
                validator,
                validator.muuOrgRule
        );
    }

    @Test
    public void testMuuOrganisaatioUnderKoulutustoimija() {
        assertResult(
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
                false,
                validator,
                validator.muuOrgRule
        );
    }

    @Test
    public void testMuuOrganisaatioUnderMuuOrganisaatio() {
        assertResult(
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
                true,
                validator,
                validator.muuOrgRule
        );
    }

    @Test
    public void testMuuOrganisaatioUnderTyoelamajarjesto() {
        assertResult(
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
//                false,
                true, // TODO temp remove after transfer
                validator,
                validator.muuOrgRule
        );
    }

    @Test
    public void testMuuOrganisaatioUnderOppilaitos() {
        assertResult(
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
                false,
                validator,
                validator.muuOrgRule
        );
    }

    @Test
    public void testMuuOrganisaatioUnderToimipiste() {
        assertResult(
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
                false,
                validator,
                validator.muuOrgRule
        );
    }

    @Test
    public void testMuuOrganisaatioUnderOppisopimustoimipiste() {
        assertResult(
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
                false,
                validator,
                validator.muuOrgRule
        );
    }

    /* child == työelämäjärjestö */
    @Test
    public void testToplevelTyoelamajarjesto() {
        assertResult(
                null,
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
                true,
                validator,
                validator.tyoelamajarjestoRule
        );
    }

    @Test
    public void testTyoelamajarjestoUnderOPH() {
        assertResult(
                root,
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
                true,
                validator,
                validator.tyoelamajarjestoRule
        );
    }

    @Test
    public void testTyoelamajarjestoUnderKoulutustoimija() {
        assertResult(
                getOrg(OrganisaatioTyyppi.KOULUTUSTOIMIJA),
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
                false,
                validator,
                validator.tyoelamajarjestoRule
        );
    }

    @Test
    public void testTyoelamajarjestoUnderMuuOrganisaatio() {
        assertResult(
                getOrg(OrganisaatioTyyppi.MUU_ORGANISAATIO),
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
//                false,
                true, // TODO temp remove after transfer
                validator,
                validator.tyoelamajarjestoRule
        );
    }

    @Test
    public void testTyoelamajarjestoUnderTyoelamajarjesto() {
        assertResult(
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
                true,
                validator,
                validator.tyoelamajarjestoRule
        );
    }

    @Test
    public void testTyoelamajarjestoUnderOppilaitos() {
        assertResult(
                getOrg(OrganisaatioTyyppi.OPPILAITOS),
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
                false,
                validator,
                validator.tyoelamajarjestoRule
        );
    }

    @Test
    public void testTyoelamajarjestoUnderToimipiste() {
        assertResult(
                getOrg(OrganisaatioTyyppi.TOIMIPISTE),
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
                false,
                validator,
                validator.tyoelamajarjestoRule
        );
    }

    @Test
    public void testTyoelamajarjestoUnderOppisopimustoimipiste() {
        assertResult(
                getOrg(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE),
                getOrg(OrganisaatioTyyppi.TYOELAMAJARJESTO),
                false,
                validator,
                validator.tyoelamajarjestoRule
        );
    }

}
