import { API_CONTEXT, BASE_PATH, LEGACY_API_CONTEXT } from '../../src/contexts/contexts';

describe('Ryhmat Page', () => {
    beforeEach(() => {});
    it('Renders table of Ryhmat', () => {
        cy.intercept('GET', `${LEGACY_API_CONTEXT}/organisaatio/v3/ryhmat*`, { fixture: 'ryhmatArr.json' }); // tarvitaan mockattuja tuloksia.
        cy.intercept('GET', `${LEGACY_API_CONTEXT}/koodisto/POSTI/koodi*`).as('postikoodit');
        cy.visit(`${BASE_PATH}/ryhmat`);
        cy.wait('@postikoodit', { timeout: 10000 });
        cy.get('table', { timeout: 30000 });
    });

    it('Can use table filters', () => {
        cy.get('table').then(() => {
            cy.get('#RYHMAN_TYYPPI_SELECT input').type('yleinen{enter}{enter}', { force: true });
            expect(cy.get('a').first().value).to.have.valueOf('Vielä kerran suomi');
            cy.get('#RYHMAN_TYYPPI_SELECT input').type('{backspace}{enter}', { force: true });

            cy.get('#RYHMAN_KAYTTOTARKOITUS_SELECT input').type('Hakukohde{enter}{enter}', { force: true });
            expect(cy.get('a').first().value).to.have.valueOf('Vielä kerran suomi');
            cy.get('#RYHMAN_KAYTTOTARKOITUS_SELECT input').type('{backspace}{enter}', { force: true });

            cy.get('#RYHMAN_TILA_SELECT input').type('Passiivinen{enter}{enter}', { force: true });
            cy.get('tbody').children().should('have.length', 0);
            cy.get('#RYHMAN_TILA_SELECT input').type('{backspace}{enter}', { force: true });
        });
    });

    it('Can use table pagination', () => {
        cy.intercept('GET', `${LEGACY_API_CONTEXT}/organisaatio/v3/ryhmat*`, { fixture: 'ryhmatArr.json' }); // tarvitaan mockattuja tuloksia.
        cy.intercept('GET', `${LEGACY_API_CONTEXT}/koodisto/POSTI/koodi*`).as('postikoodit');
        cy.visit(`${BASE_PATH}/ryhmat`);
        cy.wait('@postikoodit', { timeout: 10000 });
        cy.get('table').then(() => {
            cy.get('tbody').children().should('have.length', 10);
            cy.get('button').contains('2').should('have.attr', 'color', 'secondary').click();
            cy.get('tbody').children().should('have.length', 10);
            cy.get('button').contains('2').should('have.attr', 'color', 'primary');
        });
    });

    it('Can use table näyta sivulla', () => {
        cy.get('table').then(() => {
            cy.get('tbody').children().should('have.length', 10);
            cy.get('select').last().select('30');
            cy.get('tbody').children().should('have.length', 30);
        });
    });

    it('Can transition to create a new ryhma organisation', () => {
        cy.intercept('GET', `${LEGACY_API_CONTEXT}/organisaatio/v3/ryhmat*`, { fixture: 'ryhmatArr.json' }); // tarvitaan mockattuja tuloksia.
        cy.intercept('GET', `${LEGACY_API_CONTEXT}/koodisto/POSTI/koodi*`).as('postikoodit');
        cy.visit(`${BASE_PATH}/ryhmat`);
        cy.wait('@postikoodit', { timeout: 10000 });
        cy.get('table', { timeout: 30000 }).then(() => {
            cy.get('button').first().click();
            expect(cy.get('h1').value).to.have.valueOf('');
        });
    });
});
