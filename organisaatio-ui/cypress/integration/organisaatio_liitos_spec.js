import { organisaatio } from '../support/data';

describe('Organisaatioliitos', () => {
    beforeEach(() => {});

    it('Can merge organisaatio', () => {
        // persist parents
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio1');
        cy.persistOrganisaatio(organisaatio('PARENT2', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio2');
        cy.persistOrganisaatio(organisaatio('PARENT3', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio3');
        cy.persistOrganisaatio(organisaatio('PARENT4', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio4');
        //persis child
        cy.get('@parentOrganisaatio1').then((parentOrganisaatio1) => {
            cy.persistOrganisaatio(
                organisaatio('CHILD', {
                    parentOid: parentOrganisaatio1.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_02`],
                }),
                'child'
            );
        });

        cy.get('@child').then((child) => {
            cy.get('@parentOrganisaatio3').then((parentOrganisaatio3) => {
                cy.intercept('GET', '/organisaatio/organisaatio/v4/*').as('getCurrent');
                cy.visit(`/lomake/${child.body.organisaatio.oid}`);
                cy.wait(['@getCurrent'], { timeout: 10000 });
                cy.intercept('GET', '/organisaatio/organisaatio/v4/hae*').as('getParents');
                cy.clickButton('LOMAKE_YHDISTA_ORGANISAATIO_FI');
                cy.wait(['@getParents'], { timeout: 10000 });

                cy.selectFromList(
                    'TOIMIPISTEEN_YHDISTYS_TOINEN_ORGANISAATIO',
                    parentOrganisaatio3.body.organisaatio.oid,
                    'PARENT'
                );
                cy.intercept(
                    'PUT',
                    `/organisaatio/organisaatio/v4/${child.body.organisaatio.oid}/organisaatiosuhde/*`
                ).as('merge');
                cy.intercept('GET', `/organisaatio/organisaatio/v4/${child.body.organisaatio.oid}/historia`).as(
                    'historia'
                );
                cy.clickButton('BUTTON_VAHVISTA');

                cy.wait(['@merge'], { timeout: 10000 });
                cy.contains('CHILD Suominimi');
                cy.wait(['@historia'], { timeout: 10000 });
                cy.clickAccordion('RAKENNE');
                cy.get('h2').contains('RAKENNE_YLEMMAN_TASON_OTSIKKO').parent().contains('PARENT1 Suominimi');
                cy.get('h2').contains('RAKENNE_YLEMMAN_TASON_OTSIKKO').parent().contains('PARENT3 Suominimi');
            });
        });
    });
});
