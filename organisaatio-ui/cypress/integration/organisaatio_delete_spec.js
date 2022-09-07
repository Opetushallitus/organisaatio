import { BASE_PATH } from '../../src/contexts/constants';
import { helsinki } from '../support/data';

describe('Delete organisaatio through UI', () => {
    it('should not loose information on save', () => {
        const myHel = helsinki({});
        cy.persistOrganisaatio(myHel, 'firstHel').then((firstHel) => {
            cy.visit(`${BASE_PATH}/lomake/${firstHel.body.organisaatio.oid}`);
            cy.get('h1').contains('Helsingin kaupunki', { timeout: 10000 }).should('exist');
            cy.get('button[name=LOMAKE_POISTA_ORGANISAATIO]').should('be.visible').click();
            cy.get('button[name=BUTTON_VAHVISTA]').should('be.visible').click();
            cy.get('h1').contains('Helsingin kaupunki (LABEL_POISTETTU)', { timeout: 10000 }).should('exist');
        });
    });
});
