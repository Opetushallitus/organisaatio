import { organisaatio } from '../support/data';
import { API_CONTEXT, BASE_PATH, PUBLIC_API_CONTEXT } from '../../src/contexts/constants';

describe('Organisaation nimenmuutosmodaali', () => {
    it('Save a new name', () => {
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio1');
        cy.get('@parentOrganisaatio1').then((organisaatio) => {
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/*`).as('getCurrent');
            cy.visit(`${BASE_PATH}/lomake/${organisaatio.body.organisaatio.oid}`);
            cy.wait(['@getCurrent'], { timeout: 10000 });
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/hae*`).as('getParents');
            cy.addNewNimi('pöllö', 'pöllö');
            cy.contains('pöllö', { timeout: 10000 }).should('exist');
        });
    });

    it('Edit name', () => {
        cy.persistOrganisaatio(organisaatio('PARENT2', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio2');
        cy.get('@parentOrganisaatio2').then((organisaatio) => {
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/*`).as('getCurrent');
            cy.visit(`${BASE_PATH}/lomake/${organisaatio.body.organisaatio.oid}`);
            cy.wait(['@getCurrent'], { timeout: 10000 });
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/hae*`).as('getParents');
            cy.editNimi('pöllö2');
            cy.contains('pöllö2', { timeout: 10000 }).should('exist');
        });
    });

    it('Schedule a name change', () => {
        cy.persistOrganisaatio(organisaatio('PARENT3', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio3');

        const getAfterOneYear = () => {
            const date = new Date();
            return `1.${date.getMonth() + 1}.${date.getFullYear() + 1}`;
        };

        cy.get('@parentOrganisaatio3').then((organisaatio) => {
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/*`).as('getCurrent');
            cy.visit(`${BASE_PATH}/lomake/${organisaatio.body.organisaatio.oid}`);
            cy.wait(['@getCurrent'], { timeout: 10000 }).then(() => {
                cy.intercept('GET', `${PUBLIC_API_CONTEXT}/hae*`).as('getParents');
                cy.addNewNimi('pöllö ajastettu', 'pöpi', getAfterOneYear());
                cy.clickAccordion('NIMIHISTORIA');
                cy.contains('pöllö ajastettu', { timeout: 10000 }).should('exist');
                cy.contains('POISTA_AJASTETTU_NIMENMUUTOS', { timeout: 10000 }).should('exist');
            });
        });
    });

    it('Delete a name change', () => {
        cy.on('window:confirm', () => true);
        cy.persistOrganisaatio(organisaatio('PARENT4', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio4');

        const getAfterOneYear = () => {
            const date = new Date();
            return `2.${date.getMonth() + 1}.${date.getFullYear() + 1}`;
        };

        cy.get('@parentOrganisaatio4').then((organisaatio) => {
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/*`).as('getCurrent');
            cy.visit(`${BASE_PATH}/lomake/${organisaatio.body.organisaatio.oid}`);
            cy.wait(['@getCurrent'], { timeout: 10000 });
            cy.addNewNimi('delete testi', 'pöllö', getAfterOneYear());
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/*`).as('getCurrent2');
            cy.intercept('POST', `${PUBLIC_API_CONTEXT}/*`).as('findByOids');
            cy.wait(['@getCurrent2', '@findByOids'], { timeout: 10000 }).then(() => {
                cy.clickAccordion('NIMIHISTORIA');
                cy.contains('delete testi', { timeout: 10000 }).should('exist');
                cy.clickButton('POISTA_AJASTETTU_NIMENMUUTOS');
                cy.contains('delete testi', { timeout: 5000 }).should('not.exist');
            });
        });
    });
});