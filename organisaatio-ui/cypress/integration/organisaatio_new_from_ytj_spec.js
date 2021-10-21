import { organisaatio } from '../support/data';

const Y_TUNNUS = '2627679-5';
beforeEach(() => {
    cy.deleteByYTunnus(Y_TUNNUS);
});
describe('New organisaatio from YTJ', () => {
    it('shows UUDEN_TOIMIJAN_LISAAMINEN', () => {
        cy.visit('/');
        cy.get('button').contains('LISAA_UUSI_TOIMIJA').click();
        expect(cy.get('h1').value).to.contain.valueOf('UUDEN_TOIMIJAN_LISAAMINEN');
    });
    it('Can fetch from YTJ', () => {
        cy.clickRadioOrCheckbox('Koulutustoimija');
        cy.clickButton('HAE_YTJ_TIEDOT');
        cy.inputByName('ytjinput', Y_TUNNUS);
        cy.intercept('GET', '/organisaatio/ytj/*').as('findYtj');
        cy.clickButton('HAE_YTJTIEDOT');
        cy.wait(['@findYtj'], { timeout: 10000 });
        cy.clickButton('Hameen ammatti');
        cy.clickButton('BUTTON_JATKA');
        cy.clickSaveButton();
        cy.contains('Hameen ammatti-instituutti');
    });
});
describe('Edit organisaatio from YTJ', () => {
    it('Can fetch from YTJ', () => {
        cy.persistOrganisaatio(organisaatio('BERFORE_FETCH'), 'parentOrganisaatio');
        cy.get('@parentOrganisaatio').then((response) => {
            cy.visit(`/lomake/${response.body.organisaatio.oid}`);
            cy.clickRadioOrCheckbox('Koulutustoimija');
            cy.clickButton('PAIVITA_YTJ_TIEDOT');
            // TODO when muokkaus-lomake has been refactored from state to form-hooks
            // cy.inputByName('ytjinput', Y_TUNNUS);
            // cy.intercept('GET', '/organisaatio/ytj/*').as('findYtj');
            // cy.clickButton('HAE_YTJTIEDOT');
            // cy.wait(['@findYtj'], { timeout: 10000 });
            // cy.clickButton('Hameen ammatti');
            // cy.clickSaveButton();
            // cy.contains('Hameen ammatti-instituutti');
        });
    });
});
