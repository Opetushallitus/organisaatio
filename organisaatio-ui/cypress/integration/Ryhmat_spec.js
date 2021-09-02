describe('Organisaatiot Page', () => {
  beforeEach(() => {
    cy.intercept('GET', '/organisaatio/organisaatio/v3/ryhmat*', { fixture: 'ryhmatArr.json' })
    cy.intercept('GET', '/organisaatio/lokalisointi/kieli', { fixture: 'kieli.json' })
    cy.intercept('GET', '/organisaatio/lokalisointi', { fixture: 'lokalisointi.json' })
    cy.intercept('GET', '/organisaatio/koodisto/KUNTA/koodi', { fixture: 'kunnat.json' })
    cy.intercept('GET', '/organisaatio/koodisto/ORGANISAATIOTYYPPI/koodi', { fixture: 'organisaatiotyypit.json' })
    cy.intercept('GET', '/organisaatio/koodisto/RYHMANTILA/koodi', { fixture: 'ryhmantilat.json' })
    cy.intercept('GET', '/organisaatio/koodisto/RYHMATYYPIT/koodi', { fixture: 'ryhmatyypit.json' })
    cy.intercept('GET', '/organisaatio/koodisto/KAYTTORYHMAT/koodi', { fixture: 'kayttoryhmat.json' })
    cy.intercept('GET', '/organisaatio/koodisto/MAATJAVALTIOT1/koodi', { fixture: 'MAATJAVALTIOT.json' })
    cy.intercept('GET', '/organisaatio/koodisto/POSTI/koodi*', { fixture: 'POSTI.json' })
    cy.intercept('GET', '/organisaatio/koodisto/OPPILAITOKSENOPETUSKIELI/koodi*', { fixture: 'OPPILAITOKSENOPETUSKIELI.json' })
    cy.intercept('GET', '/organisaatio/organisaatio/v4/*', { fixture: 'humakRyhma.json' })
  })
  it('Renders table of Ryhmat', () => {
    cy.visit('/ryhmat');
    cy.get('table', { timeout: 30000});
  })

  it('Finds humak from table', () => {
    cy.get('table')
      .then(($table) => {
        cy.get('input').first().type('humak, alue');
        expect(cy.get('a').value).to.have.valueOf('Humak, alueyksikkö')
      })
  })

  it('Can open humak organisation', () => {
    cy.get('table')
      .then(($table) => {
        cy.get('input').first().clear({ force: true }).type('humak, alue');
        expect(cy.get('a').value).to.have.valueOf('Humak, alueyksikkö');
        cy.get('a').click();
      })
  })
})

