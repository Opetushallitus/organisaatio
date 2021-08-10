describe('Organisaatiot Page', () => {
  before(() => {
    cy.visit('/organisaatio/lomake/1.2.246.562.10.48587687889');
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

  /*
  it('Can save edited organisation', () => {
    cy.get('button').last().click()
  })
   */
})