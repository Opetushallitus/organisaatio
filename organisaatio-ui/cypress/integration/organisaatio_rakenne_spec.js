import { organisaatio } from '../support/data';
import { API_CONTEXT, BASE_PATH, PUBLIC_API_CONTEXT, ROOT_OID } from '../../src/contexts/constants';

describe('Organisaatio Rakenne', () => {
    it('shows UUDEN_TOIMIJAN_LISAAMINEN', () => {
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('button').contains('LISAA_UUSI_TOIMIJA').click();
        expect(cy.get('h1').value).to.contain.valueOf('UUDEN_TOIMIJAN_LISAAMINEN');
    });

    it('Can add CHILD organisaatio', () => {
        cy.persistOrganisaatio(organisaatio('PARENT'), 'parentOrganisaatio');
        cy.get('@parentOrganisaatio').then((response) => {
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/${ROOT_OID}*`).as('getParentOrg');
            cy.visit(`${BASE_PATH}/lomake/${response.body.organisaatio.oid}`);
            cy.wait('@getParentOrg', { timeout: 10000 });
            cy.clickButton('LISAA_UUSI_TOIMIJA');
            cy.contains('UUDEN_TOIMIJAN_LISAAMINEN');
            cy.clickAccordion('PERUSTIEDOT');
            cy.enterPerustiedot('CHILD', 'Oppilaitos', true);
            cy.clickButton('JATKA');
            cy.clickButton('NAYTA_MUUT_KIELET');
            cy.enterAllYhteystiedot('CHILD');
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/${response.body.organisaatio.oid}*`).as('findPath1');
            cy.intercept('POST', `${PUBLIC_API_CONTEXT}/findbyoids`).as('findPath2');
            cy.clickSaveButton();
            cy.wait('@findPath1', { timeout: 10000 });
            cy.wait('@findPath2', { timeout: 10000 });
            cy.contains('CHILD Suominimi');
            cy.clickAccordion('RAKENNE');

            cy.get('h2').contains('RAKENNE_YLEMMAN_TASON_OTSIKKO').parent().contains('PARENT Suominimi');
        });
    });
});
