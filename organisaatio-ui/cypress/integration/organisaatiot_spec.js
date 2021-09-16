describe('Organisaatiot Page', () => {
    before(() => {
        cy.intercept('GET', '/organisaatio/config/frontproperties', { fixture: 'front.json' });
        cy.intercept('GET', 'http://localhost:9000/kayttooikeus-service/cas/me', { fixture: 'me.json' });
        cy.intercept('GET', '/organisaatio/organisaatio/v4/hierarkia/hae*', {
            fixture: 'opetushallitusOrgInArray.json',
        });
        cy.intercept('GET', '/organisaatio/lokalisointi/kieli', { fixture: 'kieli.json' });
        cy.intercept('GET', '/organisaatio/lokalisointi', { fixture: 'lokalisointi.json' });
        cy.intercept('GET', '/organisaatio/koodisto/KUNTA/koodi', { fixture: 'kunnat.json' });
        cy.intercept('GET', '/organisaatio/koodisto/ORGANISAATIOTYYPPI/koodi', { fixture: 'organisaatiotyypit.json' });
        cy.intercept('GET', '/organisaatio/koodisto/RYHMANTILA/koodi', { fixture: 'ryhmantilat.json' });
        cy.intercept('GET', '/organisaatio/koodisto/RYHMATYYPIT/koodi', { fixture: 'ryhmatyypit.json' });
        cy.intercept('GET', '/organisaatio/koodisto/KAYTTORYHMAT/koodi', { fixture: 'kayttoryhmat.json' });
        cy.intercept('GET', '/organisaatio/koodisto/MAATJAVALTIOT1/koodi', { fixture: 'MAATJAVALTIOT.json' });
        cy.intercept('GET', '/organisaatio/koodisto/POSTI/koodi*', { fixture: 'POSTI.json' });
        cy.intercept('GET', '/organisaatio/koodisto/OPPILAITOKSENOPETUSKIELI/koodi*', {
            fixture: 'OPPILAITOKSENOPETUSKIELI.json',
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
