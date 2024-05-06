import { organisaatio } from '../support/data';
import { BASE_PATH, PUBLIC_API_CONTEXT } from '../../src/contexts/constants';

describe('Organisaatiosiirto', () => {
    it('Can move organisaatio', () => {
        // persist parents
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio1');
        cy.persistOrganisaatio(organisaatio('PARENT2', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio2');
        cy.persistOrganisaatio(organisaatio('PARENT3', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio3');
        cy.persistOrganisaatio(organisaatio('PARENT4', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio4');

        //persist child
        cy.get('@parentOrganisaatio1').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('CHILD', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_02`],
                }),
                'child'
            );
        });

        cy.get('@child').then((child) => {
            cy.get('@parentOrganisaatio3').then((parentOrganisaatio3) => {
                cy.intercept('GET', `${PUBLIC_API_CONTEXT}/*`).as('getCurrent');
                cy.visit(`${BASE_PATH}/lomake/${child.body.organisaatio.oid}`);
                cy.wait(['@getCurrent'], { timeout: 10000 });
                cy.intercept('GET', `${PUBLIC_API_CONTEXT}/hae*`).as('getParents');
                cy.clickButton('LOMAKE_SIIRRA_ORGANISAATIO');
                cy.wait(['@getParents'], { timeout: 10000 });
                cy.contains('label', 'ORGANISAATIO_SIIRTO_TOINEN_ORGANISAATIO', { timeout: 10000 }).should(
                    'be.visible'
                );
                cy.selectFromList(
                    'ORGANISAATIO_SIIRTO_TOINEN_ORGANISAATIO',
                    parentOrganisaatio3.body.organisaatio.ytunnus,
                    'PARENT',
                    false
                );
                cy.intercept('PUT', `${PUBLIC_API_CONTEXT}/${child.body.organisaatio.oid}/organisaatiosuhde/*`).as(
                    'merge'
                );
                cy.intercept('GET', `${PUBLIC_API_CONTEXT}/${child.body.organisaatio.oid}/historia`).as('historia');
                cy.clickButton('BUTTON_VAHVISTA');
                cy.contains('Siirretäänkö CHILD Suominimi');
                cy.clickButton('BUTTON_VAHVISTA');

                cy.wait(['@merge'], { timeout: 10000 });
                cy.contains('CHILD Suominimi');
                cy.wait(['@historia'], { timeout: 10000 });
                cy.clickAccordion('RAKENNE');
                cy.get('h2')
                    .contains('RAKENNE_YLEMMAN_TASON_OTSIKKO')
                    .parent()
                    .contains('PARENT1 Suominimi')
                    .should('be.visible');
                cy.get('h2')
                    .contains('RAKENNE_YLEMMAN_TASON_OTSIKKO')
                    .parent()
                    .contains('PARENT3 Suominimi')
                    .should('be.visible');
            });
        });
    });
});
