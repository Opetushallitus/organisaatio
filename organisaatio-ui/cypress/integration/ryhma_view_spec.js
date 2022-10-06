import { BASE_PATH } from '../../src/contexts/constants';

describe('Ryhma view Page', () => {
    it('Can save a new ryhma', () => {
        cy.visit(`${BASE_PATH}/ryhmat/uusi`);
        cy.get('#nimiFi').type('Suominimi');
        cy.get('#nimiSv').type('Ruotsinimi');
        cy.get('#nimiEn').type('Enkkunimi');
        cy.get('#kuvaus2Fi').type('Suomi kuvaus');
        cy.get('#kuvaus2Sv').type('Ruotsi kuvaus');
        cy.get('#kuvaus2En').type('Enkku kuvaus');
        cy.get('#RYHMALOMAKE_RYHMAN_TYYPPI_SELECT input').first().type('Hakukohde{enter}{enter}', { force: true });
        cy.get('#RYHMALOMAKE_RYHMAN_KAYTTOTARKOITUS_SELECT input')
            .first()
            .type('Yleinen{enter}{enter}', { force: true });
        cy.get('button').contains('BUTTON_TALLENNA').click();
        cy.location('pathname').should('include', '/ryhmat');
    });

    it('Finds just saved Suominimi from table', () => {
        cy.visit(`${BASE_PATH}/ryhmat`);
        cy.get('table', { timeout: 30000 }).then(() => {
            cy.get('input').first().type('Suominimi');
            expect(cy.get('a').contains('Suomi').first().value).to.have.valueOf('Suominimi');
            cy.get('a').contains('Suomi').first().click();
        });
    });

    it('Shows Suominimi Ryhma', () => {
        expect(cy.get('#nimiFi', { timeout: 10000 }).value).to.have.valueOf('Suominimi');
    });

    it('will show validate error if there is no name set', () => {
        cy.get('#nimiSv').clear();
        cy.get('#nimiFi').clear();
        cy.get('#nimiEn').clear();
        cy.get('button').last().click();
        cy.get('#nimiFi')
            .should('be.visible')
            .should('have.css', 'border-color')
            .and('match', /228, 78, 78/);
    });

    it('Can edit Swedish name', () => {
        cy.get('#nimiSv').type('Paremmin ruotsiksi');
        expect(cy.get('#nimiSv').value).to.have.valueOf('Paremmin ruotsiksi');
    });
    it('Can edit Swedish kuvaus', () => {
        cy.get('#kuvaus2Sv').type('paremmin ruotsiksi');
        expect(cy.get('#kuvaus2Sv').value).to.have.valueOf('paremmin ruotsiksi');
    });

    it('Can save edited Ryhma', () => {
        cy.get('#nimiFi').type('Vielä kerran suomi');
        cy.get('#RYHMALOMAKE_RYHMAN_TYYPPI_SELECT input').first().type('Hakukohde{enter}{enter}', { force: true });
        cy.get('#RYHMALOMAKE_RYHMAN_KAYTTOTARKOITUS_SELECT input')
            .first()
            .type('Yleinen{enter}{enter}', { force: true });
        cy.get('button').contains('BUTTON_TALLENNA').click();
        cy.location('pathname').should('include', '/ryhmat');
    });
});
