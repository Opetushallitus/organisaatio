import { organisaatio } from '../support/data';
import { BASE_PATH, LEGACY_API_CONTEXT, PUBLIC_API_CONTEXT, ROOT_OID } from '../../src/contexts/constants';

const Y_TUNNUS = '1572860-0';
beforeEach(() => {
    cy.deleteByYTunnus(Y_TUNNUS);
});
describe('New organisaatio from YTJ', () => {
    it('shows UUDEN_TOIMIJAN_LISAAMINEN', () => {
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('button').contains('LISAA_UUSI_TOIMIJA').click();
        cy.get('h1').contains('UUDEN_TOIMIJAN_LISAAMINEN').should('exist');
        cy.clickRadioOrCheckbox('Koulutustoimija');
        cy.clickButton('HAE_YTJ_TIEDOT');
        cy.get('button').contains('HAE_YTJTIEDOT').should('exist');
        cy.inputByName('ytjinput', Y_TUNNUS);
        cy.intercept('GET', `${LEGACY_API_CONTEXT}/ytj/${Y_TUNNUS}`, { fixture: 'ytjHameen.json' });
        cy.clickButton('HAE_YTJTIEDOT');
        cy.clickButton('Hameen ammatti');
        cy.clickButton('BUTTON_JATKA');
        cy.clickSaveButton();
        cy.get('h1').contains('Hameen ammatti-instituutti', { timeout: 10000 }).should('exist');
    });
});
describe('Edit organisaatio from YTJ', () => {
    it('Can fetch from YTJ', () => {
        cy.persistOrganisaatio(organisaatio('BERFORE_FETCH'), 'parentOrganisaatio');
        cy.get('@parentOrganisaatio').then((response) => {
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/${ROOT_OID}*`).as('getParentOrg');
            cy.visit(`${BASE_PATH}/lomake/${response.body.organisaatio.oid}`);
            cy.wait(['@getParentOrg'], { timeout: 10000 });
            cy.clickButton('PAIVITA_YTJ_TIEDOT');
            cy.inputByName('ytjinput', Y_TUNNUS);
            cy.intercept('GET', `${LEGACY_API_CONTEXT}/ytj/${Y_TUNNUS}`, { fixture: 'ytjHameen.json' }).as('findYtj');
            cy.clickButton('HAE_YTJTIEDOT');
            cy.wait(['@findYtj'], { timeout: 10000 });
            cy.clickButton('Hameen ammatti');
            cy.clickSaveButton();
            cy.get('h1').contains('Hameen ammatti-instituutti', { timeout: 10000 }).should('exist');
        });
    });
});
