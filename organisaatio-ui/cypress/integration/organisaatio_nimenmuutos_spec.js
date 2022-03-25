import { organisaatio } from '../support/data';
import { BASE_PATH } from '../../src/contexts/constants';

const getAfterOneYear = () => {
    const date = new Date();
    return `2.${date.getMonth() + 1}.${date.getFullYear() + 1}`;
};

describe('Organisaation nimenmuutosmodaali', () => {
    it('Save a new name', () => {
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio1');
        cy.get('@parentOrganisaatio1').then((organisaatio) => {
            cy.visit(`${BASE_PATH}/lomake/${organisaatio.body.organisaatio.oid}`);
            cy.contains('PARENT1', { timeout: 10000 }).should('exist');
            cy.addNewNimi('pöllö', 'pöllö');
            cy.contains('pöllö', { timeout: 10000 }).should('exist');
        });
    });

    it('Edit name', () => {
        cy.persistOrganisaatio(organisaatio('PARENT2', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio2');
        cy.get('@parentOrganisaatio2').then((organisaatio) => {
            cy.visit(`${BASE_PATH}/lomake/${organisaatio.body.organisaatio.oid}`);
            cy.contains('PARENT2', { timeout: 10000 }).should('exist');
            cy.editNimi('pöllö2');
            cy.get('h1').contains('pöllö2', { timeout: 10000 }).should('exist');
            cy.editNimiWithCopy('huuhkaja');
            cy.get('span')
                .contains(
                    'huuhkaja Suominimi kopioitava [fi], huuhkaja Suominimi kopioitava [sv], huuhkaja Suominimi kopioitava [en]',
                    { timeout: 10000 }
                )
                .should('exist');
        });
    });

    it('Schedule a name change', () => {
        cy.persistOrganisaatio(organisaatio('PARENT3', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio3');
        cy.get('@parentOrganisaatio3').then((organisaatio) => {
            cy.visit(`${BASE_PATH}/lomake/${organisaatio.body.organisaatio.oid}`);
            cy.contains('PARENT3', { timeout: 10000 }).should('exist');
            cy.addNewNimi('pöllö ajastettu', 'pöpi', getAfterOneYear());
            cy.clickAccordion('NIMIHISTORIA');
            cy.contains('pöllö ajastettu', { timeout: 10000 }).should('exist');
            cy.contains('POISTA_AJASTETTU_NIMENMUUTOS', { timeout: 10000 }).should('exist');
        });
    });

    it('Delete a name change', () => {
        cy.on('window:confirm', () => true);
        cy.persistOrganisaatio(organisaatio('PARENT4', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio4');
        cy.get('@parentOrganisaatio4').then((organisaatio) => {
            cy.visit(`${BASE_PATH}/lomake/${organisaatio.body.organisaatio.oid}`);
            cy.contains('PARENT4', { timeout: 10000 }).should('exist');
            cy.addNewNimi('delete testi', 'pöllö', getAfterOneYear());
            cy.contains('PARENT4', { timeout: 10000 }).should('exist');
            cy.clickAccordion('NIMIHISTORIA');
            cy.contains('delete testi', { timeout: 10000 }).should('exist');
            cy.clickButtonByName('POISTA_AJASTETTU_NIMENMUUTOS');
            cy.contains('delete testi', { timeout: 5000 }).should('not.exist');
        });
    });
});
