describe('Organisaatiot Page', () => {
  before(() => {
    cy.visit('/ryhmat');
  })
  it('Renders table of Ryhmat', () => {
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
        cy.get('input').clear().type('humak, alue');
        expect(cy.get('a').value).to.have.valueOf('Humak, alueyksikkö')
        cy.get('a').click()
      })
  })
})

