//import { organisaatio } from '../support/data';

describe('Organisaatio muokkaus Page', () => {
    /* TODO tehdään seuraavassa haarassa
    before(() => cy.persistOrganisaatio(organisaatio(), 'testiOrganisaatio'));
    describe('Perustiedot', () => {
        it('can be edited', () => {
            cy.get('@testiOrganisaatio').then((response) => {
                cy.visit(`/lomake/${response.body.organisaatio.oid}`);
                cy.enterPerustiedot('Muokattava', 'Oppilaitos');
                //cy.intercept('POST', '/organisaatio/organisaatio/v4/findbyoids').as('findPAth');
                cy.clickAccordion('YHTEYSTIEDOT');
                cy.wait(['@findPAth'], { timeout: 10000 });
                cy.contains(' Suominimi');
            });
        });
    });
    describe('Yhteystiedot', () => {
        it('can be edited', () => {
            //cy.clickAccordion('YHTEYSTIEDOT');
            cy.enterAllYhteystiedot('testiOrganisaatio');
            //cy.intercept('POST', '/organisaatio/organisaatio/v4/findbyoids').as('findPAth');
            //cy.wait(['@findPAth'], { timeout: 10000 });
            //cy.contains('CHILD Suominimi');
        });
    });

     */
});
