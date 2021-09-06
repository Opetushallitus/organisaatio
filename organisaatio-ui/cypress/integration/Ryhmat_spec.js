describe('Ryhmat Page', () => {
  beforeEach(() => {
    cy.intercept('GET', '/organisaatio/organisaatio/v3/ryhmat*', { fixture: 'ryhmatArr.json' });
    cy.intercept('GET', '/organisaatio/lokalisointi/kieli', { fixture: 'kieli.json' });
    cy.intercept('GET', '/organisaatio/lokalisointi', { fixture: 'lokalisointi.json' });
    cy.intercept('GET', '/organisaatio/koodisto/KUNTA/koodi', { fixture: 'kunnat.json' });
    cy.intercept('GET', '/organisaatio/koodisto/ORGANISAATIOTYYPPI/koodi', { fixture: 'organisaatiotyypit.json' });
    cy.intercept('GET', '/organisaatio/koodisto/RYHMANTILA/koodi', { fixture: 'ryhmantilat.json' });
    cy.intercept('GET', '/organisaatio/koodisto/RYHMATYYPIT/koodi', { fixture: 'ryhmatyypit.json' });
    cy.intercept('GET', '/organisaatio/koodisto/KAYTTORYHMAT/koodi', { fixture: 'kayttoryhmat.json' });
    cy.intercept('GET', '/organisaatio/koodisto/MAATJAVALTIOT1/koodi', { fixture: 'MAATJAVALTIOT.json' });
    cy.intercept('GET', '/organisaatio/koodisto/POSTI/koodi*', { fixture: 'POSTI.json' });
    cy.intercept('GET', '/organisaatio/koodisto/OPPILAITOKSENOPETUSKIELI/koodi*', { fixture: 'OPPILAITOKSENOPETUSKIELI.json' });
    cy.intercept('GET', '/organisaatio/organisaatio/v4/*', { fixture: 'humakRyhma.json' });
  })
  it('Renders table of Ryhmat', () => {
    cy.visit('/ryhmat');
    cy.get('table', { timeout: 30000});
  })

  it('Can use table filters', () => {
    cy.get('table')
      .then(() => {
        cy.get('#react-select-3-input').type('koulutus{enter}{enter}', {force: true});
        expect(cy.get('a').first().value).to.have.valueOf('Avoin AMK');
        cy.get('#react-select-3-input').type('{backspace}{enter}', {force: true});

        cy.get('#react-select-5-input').type('Priorisoiva{enter}{enter}', {force: true});
        expect(cy.get('a').first().value).to.have.valueOf('Arcada 2nd Application');
        cy.get('#react-select-5-input').type('{backspace}{enter}', {force: true});

        cy.get('#react-select-7-input').type('Passiivinen{enter}{enter}', {force: true});
        cy.get('tbody').children().should('have.length', 0);
        cy.get('#react-select-7-input').type('{backspace}{enter}', {force: true});
      })
  })

  it('Can use table pagination', () => {
    cy.get('table')
      .then(() => {
        cy.get('tbody').children().should('have.length', 10);
        cy.get('button').contains('2').should('have.attr', 'color', 'secondary').click();
        cy.get('tbody').children().should('have.length', 10);
        cy.get('button').contains('2').should('have.attr', 'color', 'primary')
      })
  })

  it('Can use table näyta sivulla', () => {
    cy.get('table')
      .then(() => {
        cy.get('tbody').children().should('have.length', 10);
        cy.get('select').last().select('30');
        cy.get('tbody').children().should('have.length', 30);
      })
  })

  it('Finds humak from table', () => {
    cy.get('table')
      .then(() => {
        cy.get('input').first().type('humak, alue');
        expect(cy.get('a').value).to.have.valueOf('Humak, alueyksikkö');
      })
  })

  it('Can open humak organisation', () => {
    cy.get('table')
      .then(() => {
        expect(cy.get('a').value).to.have.valueOf('Humak, alueyksikkö');
        cy.get('a').click();
        expect(cy.get('h1').value).to.have.valueOf('Humak, alueyksikkö');
      })
  })
  it('Can transition to create a new ryhma organisation', () => {
      cy.visit('/ryhmat');
      cy.get('table', { timeout: 30000}).then(() => {
        cy.get('button').first().click();
        expect(cy.get('h1').value).to.have.valueOf('');
      });
  })
})