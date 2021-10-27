import { organisaatio } from '../support/data';
import { API_CONTEXT, BASE_PATH } from '../../src/contexts/contexts';

describe('Organisaatio Rakenne', () => {
    it('shows UUDEN_TOIMIJAN_LISAAMINEN', () => {
        cy.visit(`${BASE_PATH}/`);
        cy.get('button').contains('LISAA_UUSI_TOIMIJA').click();
        expect(cy.get('h1').value).to.contain.valueOf('UUDEN_TOIMIJAN_LISAAMINEN');
    });

    it('Can add CHILD organisaatio', () => {
        cy.persistOrganisaatio(organisaatio('PARENT'), 'parentOrganisaatio');
        cy.get('@parentOrganisaatio').then((response) => {
            cy.visit(`${BASE_PATH}/lomake/${response.body.organisaatio.oid}`);
            cy.clickButton('LISAA_UUSI_TOIMIJA');
            cy.contains('UUDEN_TOIMIJAN_LISAAMINEN');
            cy.clickAccordion('PERUSTIEDOT');
            cy.enterPerustiedot('CHILD', 'Oppilaitos', true);
            cy.clickButton('JATKA');
            cy.enterAllYhteystiedot('CHILD');
            cy.intercept('POST', `${API_CONTEXT}/organisaatio/v4/findbyoids`).as('findPAth');
            cy.clickSaveButton();
            cy.wait(['@findPAth'], { timeout: 10000 });
            cy.contains('CHILD Suominimi');
        });
    });
    it('Should have parent organisation', () => {
        cy.clickAccordion('RAKENNE');
        cy.get('h2').contains('RAKENNE_YLEMMAN_TASON_OTSIKKO').parent().contains('PARENT Suominimi');
    });
});
