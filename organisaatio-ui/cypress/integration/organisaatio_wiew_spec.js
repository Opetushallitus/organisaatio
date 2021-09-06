describe('Organisaatiot Page', () => {
  before(() => {
    cy.intercept('GET', '/organisaatio/lokalisointi/kieli', { fixture: 'kieli.json' })
    cy.intercept('GET', '/organisaatio/organisaatio/v4/*', { fixture: 'opetushallitusOrg.json' })
    cy.intercept('GET', '/organisaatio/organisaatio/v4/1.2.246.562.10.69981965515/historia', { fixture: 'organisaatioHistoria.json' })
    cy.intercept('GET', '/organisaatio/lokalisointi', { fixture: 'lokalisointi.json' })
    cy.intercept('GET', '/organisaatio/koodisto/KUNTA/koodi', { fixture: 'kunnat.json' })
    cy.intercept('GET', '/organisaatio/koodisto/ORGANISAATIOTYYPPI/koodi', { fixture: 'organisaatiotyypit.json' })
    cy.intercept('GET', '/organisaatio/koodisto/RYHMANTILA/koodi', { fixture: 'ryhmantilat.json' })
    cy.intercept('GET', '/organisaatio/koodisto/RYHMATYYPIT/koodi', { fixture: 'ryhmatyypit.json' })
    cy.intercept('GET', '/organisaatio/koodisto/KAYTTORYHMAT/koodi', { fixture: 'kayttoryhmat.json' })
    cy.intercept('GET', '/organisaatio/koodisto/MAATJAVALTIOT1/koodi', { fixture: 'MAATJAVALTIOT.json' })
    cy.intercept('GET', '/organisaatio/koodisto/POSTI/koodi*', { fixture: 'POSTI.json' })
    cy.intercept('GET', '/organisaatio/koodisto/OPPILAITOKSENOPETUSKIELI/koodi*', { fixture: 'OPPILAITOKSENOPETUSKIELI.json' })
    cy.intercept('POST', '/organisaatio/organisaatio/v4/findbyoids', { fixture: 'findbyOids.json' })
    cy.visit('/organisaatio/lomake/1.2.246.562.10.48587687889', { timeout: 30000});
  })
  it('Shows opetushallitus organisation', () => {
    expect(cy.get('h1').value).to.have.valueOf('Opetushallitus')
  })
  it('Can edit perustiedot', () => {
    cy.get('#accordion__heading-0 > span').click()
  })
  it('Can edit Yhteystiedot', () => {
    cy.get('#accordion__heading-1 > span').click()
  })
  it('Can edit Nimihistoria', () => {
    cy.get('#accordion__heading-2 > span').click()
  })
  it('Can edit Organisaatiohistoria', () => {
    cy.get('#accordion__heading-3 > span').click()
  })
})