import { organisaatio } from './data';

describe('Organisaatioliitos', () => {
    it('Can merge organisaatio', () => {
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio1');
        cy.get('@parentOrganisaatio1').then((parentOrganisaatio1) => {
            cy.persistOrganisaatio(
                organisaatio('CHILD', {
                    parentOid: parentOrganisaatio1.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_02`],
                }),
                'child'
            );
            cy.get('@child').then((child) => {
                cy.persistOrganisaatio(
                    organisaatio('PARENT2', { tyypit: [`organisaatiotyyppi_01`] }),
                    'parentOrganisaatio2'
                );
                cy.get('@parentOrganisaatio2').then((parentOrganisaatio2) => {
                    cy.intercept('GET', '/organisaatio/organisaatio/v4/*').as('getCurrent');
                    cy.visit(`/lomake/${child.body.organisaatio.oid}`);
                    cy.wait(['@getCurrent'], { timeout: 10000 });
                    cy.intercept('GET', '/organisaatio/organisaatio/v4/hae*').as('getParents');
                    cy.clickButton('LOMAKE_YHDISTA_ORGANISAATIO_FI');
                    cy.wait(['@getParents'], { timeout: 10000 });
                    cy.selectFromList(
                        'TOIMIPISTEEN_YHDISTYS_TOINEN_ORGANISAATIO',
                        parentOrganisaatio2.body.organisaatio.oid
                    );
                    cy.intercept(
                        'PUT',
                        `/organisaatio/organisaatio/v4/${child.body.organisaatio.oid}/organisaatiosuhde/*`
                    ).as('merge');
                    cy.clickButton('BUTTON_VAHVISTA');
                    cy.wait(['@merge'], { timeout: 10000 });
                    cy.contains('CHILD Suominimi');
                    cy.clickAccordion('RAKENNE');
                    cy.get('h2').contains('RAKENNE_YLEMMAN_TASON_OTSIKKO').parent().contains('PARENT1 Suominimi');
                    cy.get('h2').contains('RAKENNE_YLEMMAN_TASON_OTSIKKO').parent().contains('PARENT2 Suominimi');
                });
            });
        });
    });
});
