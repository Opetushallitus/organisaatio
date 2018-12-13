package fi.vm.sade.organisaatio.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        OrganisationHierarchyValidatorTopLevelTest.class,
        OrganisationHierarchyValidatorSecondLevelTest.class,
        OrganisationHierarchyValidatorTypeTest.class,
})
public class OrganisationHierarchyValidatorTest {
}
