import { BASE_PATH, PUBLIC_API_CONTEXT, LISATIEDOT_EXTERNAL_URI } from '../../src/contexts/constants';

describe('Organisaatiot Page', () => {
    it('Renders table of organisations', () => {
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('table', { timeout: 30000 });
    });

    it('Finds opetushallitus from table', () => {
        cy.intercept('GET', `${PUBLIC_API_CONTEXT}/hierarkia/hae*`, {
            fixture: 'opetushallitusOrgInArray.json',
        });
        cy.get('table').then(($table) => {
            cy.get('input').first().type('Opetushallitus{enter}');
            expect(cy.get('a').value).to.have.valueOf('Opetushallitus');
        });
    });
    it('Shows lisätiedot link with correct uri as href', () => {
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('table', { timeout: 30000 });
        cy.contains('?').should('have.attr', 'href').should('not.be.empty').and('contain', LISATIEDOT_EXTERNAL_URI);
    });
});
