describe('Organisaatiot Page', () => {
    before(() => {
        cy.intercept('GET', '/organisaatio/organisaatio/v4/hierarkia/hae*', {
            fixture: 'opetushallitusOrgInArray.json',
        });
        cy.visit('/');
    });
    it('Renders table of organisations', () => {
        cy.get('table', { timeout: 30000 });
    });

    it('Finds opetushallitus from table', () => {
        cy.get('table').then(($table) => {
            cy.get('input').first().type('Opetushallitus');
            expect(cy.get('a').value).to.have.valueOf('Opetushallitus');
        });
    });
    /* TODO it('finds passive organisations too', () => {
    cy.get('table')
      .then(($table) => {
        cy.get('input').last().click();
        cy.get('table', { timeout: 30000});
      })
  })*/

    /*it('Can open Opetushallitus organisation', () => {
    cy.get('table')
      .then(($table) => {
        cy.get('input').first().clear().type('Opetushallitus');
        expect(cy.get('a').value).to.have.valueOf('Opetushallitus')
        cy.get('a').click()
      })
  })*/
});
