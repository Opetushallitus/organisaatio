import { BASE_PATH, LEGACY_API_CONTEXT, PUBLIC_API_CONTEXT } from '../../src/contexts/constants';
import { organisaatio } from '../support/data';

describe('Restrict buttons by roles', () => {
    it('Does not show buttons without required roles', () => {
        cy.intercept('GET', `/kayttooikeus-service/cas/me`, { fixture: 'noRoles.json' });
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('button').contains('TAULUKKO_LISAA_UUSI').should('not.exist');

        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio1');
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
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/${child.body.organisaatio.oid}*`).as('getCurrent');
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/${child.body.organisaatio.parentOid}*`).as('getParent');
            cy.visit(`${BASE_PATH}/lomake/${child.body.organisaatio.oid}`);
            cy.wait(['@getCurrent'], { timeout: 10000 });
            cy.wait(['@getParent'], { timeout: 10000 });
            cy.get('h1').contains('CHILD').should('exist');
            cy.get('button').contains('SIIRRA').should('not.exist');
            cy.get('button').contains('YHDISTA').should('not.exist');
            cy.get('button').contains('LISAA_UUSI').should('not.exist');
        });
    });
    it('Shows buttons with OPH roles', () => {
        cy.intercept('GET', `/kayttooikeus-service/cas/me`, { fixture: 'OPHRoles.json' });
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.contains('TAULUKKO_LISAA_UUSI').should('exist');
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio1');
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
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/${child.body.organisaatio.oid}*`).as('getCurrent');
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/${child.body.organisaatio.parentOid}*`).as('getParent');
            cy.visit(`${BASE_PATH}/lomake/${child.body.organisaatio.oid}`);
            cy.wait(['@getCurrent'], { timeout: 10000 });
            cy.wait(['@getParent'], { timeout: 10000 });
            cy.get('h1').contains('CHILD').should('exist');
            cy.get('button').contains('SIIRRA').should('exist');
            cy.get('button').contains('YHDISTA').should('exist');
            cy.get('button').contains('LISAA_UUSI').should('exist');
            cy.get('button').contains('TALLENNA').should('exist');
        });
    });
    it('Shows buttons with CRUD roles', () => {
        cy.intercept('GET', `/kayttooikeus-service/cas/me`, { fixture: 'CRUDRoles.json' });
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.contains('TAULUKKO_LISAA_UUSI').should('not.exist');
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio1');
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
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/${child.body.organisaatio.oid}*`).as('getCurrent');
            cy.intercept('GET', `${PUBLIC_API_CONTEXT}/${child.body.organisaatio.parentOid}*`).as('getParent');
            cy.visit(`${BASE_PATH}/lomake/${child.body.organisaatio.oid}`);
            cy.wait(['@getCurrent'], { timeout: 10000 });
            cy.wait(['@getParent'], { timeout: 10000 });
            cy.get('h1').contains('CHILD').should('exist');
            cy.get('button').contains('SIIRRA').should('not.exist');
            cy.get('button').contains('YHDISTA').should('not.exist');
            cy.get('button').contains('LISAA_UUSI').should('exist');
            cy.get('button').contains('TALLENNA').should('exist');
        });
    });
});
