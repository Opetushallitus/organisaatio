import { organisaatio } from '../support/data';

const Y_TUNNUS = '2627679-5';
before(() => {
    cy.deleteByYTunnus(Y_TUNNUS);
});
describe('Organisaatio Rakenne', () => {
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
