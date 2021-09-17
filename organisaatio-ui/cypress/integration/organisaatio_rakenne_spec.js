describe('Organisaatio Rakenne', () => {
    Cypress.Keyboard.defaults({
        keystrokeDelay: 1,
    });
    it('shows UUDEN_TOIMIJAN_LISAAMINEN', () => {
        cy.visit('/organisaatio/');
        cy.get('button').contains('LISAA_UUSI_TOIMIJA').click();
        expect(cy.get('h1').value).to.contain.valueOf('UUDEN_TOIMIJAN_LISAAMINEN');
    });
    it('Can add PARENT organisaatio', () => {
        //Enter data for parent
        cy.enterPerustiedot('PARENT', 'Koulutustoimija');
        cy.clickButton('JATKA');
        cy.enterAllYhteystiedot('PARENT');
        cy.clickSaveButton();
        cy.contains('PARENT Suominimi');
    });
    it('Can add CHILD organisaatio', () => {
        cy.clickButton('LISAA_UUSI_TOIMIJA');
        cy.contains('UUDEN_TOIMIJAN_LISAAMINEN');
        cy.enterPerustiedot('CHILD', 'Oppilaitos');
        cy.clickButton('JATKA');
        cy.enterAllYhteystiedot('CHILD');
        cy.clickSaveButton();
        cy.contains('CHILD Suominimi');
    });
    it('Should have parent organisation', () => {
        cy.clickAccordion('RAKENNE');
        cy.get('h2').contains('RAKENNE_YLEMMAN_TASON_OTSIKKO').parent().contains('PARENT Suominimi');
    });
});
