import { organisaatio } from '../support/data';
import { BASE_PATH, PUBLIC_API_CONTEXT } from '../../src/contexts/constants';

describe('Organisaatioyhdistys', () => {
    beforeEach(() => {});

    it('Can merge organisaatio', () => {
        // persist parents
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio1');
        cy.persistOrganisaatio(organisaatio('PARENT2', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio2');
        cy.persistOrganisaatio(organisaatio('PARENT3', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio3');
        cy.persistOrganisaatio(organisaatio('PARENT4', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio4');

        //persis child
        cy.get('@parentOrganisaatio1').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('CHILD', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_02`],
                }),
                'child'
            );
        });
        cy.get('@parentOrganisaatio2').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('CHILD2', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_02`],
                }),
                'child2'
            );
        });
        cy.get('@parentOrganisaatio3').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('CHILD3', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_02`],
                }),
                'child3'
            );
        });

        cy.get('@child2').then((child2) => {
            cy.get('@child3').then((child3) => {
                cy.intercept('GET', `${PUBLIC_API_CONTEXT}/*`).as('getCurrent');
                cy.visit(`${BASE_PATH}/lomake/${child2.body.organisaatio.oid}`);
                cy.wait(['@getCurrent'], { timeout: 10000 });

                cy.intercept('GET', `${PUBLIC_API_CONTEXT}/hae*`).as('getParents');
                cy.clickButton('LOMAKE_YHDISTA_ORGANISAATIO');
                cy.wait(['@getParents'], { timeout: 10000 });
                cy.selectFromList(
                    'ORGANISAATIO_YHDISTYS_TOINEN_ORGANISAATIO',
                    child3.body.organisaatio.ytunnus,
                    'CHILD'
                );

                cy.intercept('PUT', `${PUBLIC_API_CONTEXT}/${child2.body.organisaatio.oid}/organisaatiosuhde/*`).as(
                    'merge'
                );
                cy.intercept('GET', `${PUBLIC_API_CONTEXT}/${child2.body.organisaatio.oid}/historia`).as('historia');
                cy.clickButton('BUTTON_VAHVISTA');
                cy.contains('TOIMIPISTEEN_YHDISTYS_VAHVISTUS');
                cy.clickButton('BUTTON_VAHVISTA');
                cy.wait(['@merge'], { timeout: 10000 });
                cy.wait(['@historia'], { timeout: 10000 });
                cy.contains('CHILD2 Suominimi');
                cy.clickAccordion('RAKENNE');
                cy.get('h2').contains('RAKENNE_LIITOKSET_OTSIKKO').parent().contains('CHILD3 Suominimi');
                cy.get('h2').contains('RAKENNE_YLEMMAN_TASON_OTSIKKO').parent().contains('PARENT2 Suominimi');
            });
        });
    });
});
